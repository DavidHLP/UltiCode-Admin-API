package com.david.redis.commons.aspect.chain.cache;

import com.david.log.commons.LogUtils;
import com.david.redis.commons.annotation.RedisCacheable;
import com.david.redis.commons.aspect.chain.AbstractAspectHandler;
import com.david.redis.commons.aspect.chain.AspectChain;
import com.david.redis.commons.aspect.chain.AspectContext;
import com.david.redis.commons.aspect.chain.AspectType;
import com.david.redis.commons.enums.WarmUpPriority;
import com.david.redis.commons.manager.CacheWarmUpManager;

import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;

/**
 * 缓存预热处理器
 *
 * <p>
 * 负责触发缓存预热操作，提升缓存命中率。
 * 在缓存存储成功之后，通过注解配置触发缓存预热逻辑。
 *
 * @author David
 */
@Component
public class CacheWarmUpHandler extends AbstractAspectHandler {

	private final CacheWarmUpManager warmUpManager;

	public CacheWarmUpHandler(LogUtils logUtils, CacheWarmUpManager warmUpManager) {
		super(logUtils);
		this.warmUpManager = warmUpManager;
	}

	/**
	 * 获取该处理器支持的切面类型
	 *
	 * @return 支持的切面类型集合，这里仅支持缓存相关切面
	 */
	@Override
	protected Set<AspectType> getSupportedAspectTypes() {
		return Set.of(AspectType.CACHE);
	}

	/**
	 * 获取处理器执行顺序
	 *
	 * @return 顺序值，值越小优先级越高
	 * 这里设置为80，表示在缓存存储之后执行
	 */
	@Override
	public int getOrder() {
		return 80;
	}

	/**
	 * 判断该处理器是否可以处理当前切面上下文
	 *
	 * @param context 当前切面上下文
	 * @return true 如果满足以下条件：
	 *         1. 上层 canHandle 条件通过
	 *         2. 方法上标注了 @RedisCacheable 并开启 warmUp
	 *         3. 方法已经执行完成
	 *         4. 方法执行没有异常
	 */
	@Override
	public boolean canHandle(AspectContext context) {
		return super.canHandle(context)
				&& Optional.ofNullable(context.getMethod())
				.map(method -> method.getAnnotation(RedisCacheable.class))
				.map(RedisCacheable::warmUp)
				.orElse(false)
				&& context.isMethodExecuted()
				&& !context.hasException();
	}

	/**
	 * 执行缓存预热逻辑
	 *
	 * <p>
	 * 1. 调用 triggerWarmUp 触发缓存预热
	 * 2. 捕获异常并记录日志，预热失败不影响业务逻辑
	 *
	 * @param context 当前切面上下文
	 * @param chain   切面链对象
	 * @return 切面链继续执行的返回值
	 * @throws Throwable 方法执行过程中可能抛出的异常
	 */
	@Override
	public Object handle(AspectContext context, AspectChain chain) throws Throwable {
		try {
			triggerWarmUp(context);
		} catch (Exception e) {
			logException(context, "cache_warmup", e, "缓存预热异常: " + Optional.ofNullable(context.getMethod())
					.map(Method::getName)
					.orElse("unknown"));
			// 预热失败不影响业务逻辑
		}
		return chain.proceed(context);
	}

	/**
	 * 触发缓存预热
	 *
	 * <p>
	 * 根据方法和缓存键信息，调用 CacheWarmUpManager 执行预热操作。
	 * 如果触发失败会记录日志，但不影响正常业务逻辑。
	 *
	 * @param context 当前切面上下文
	 */
	private void triggerWarmUp(AspectContext context) {
		Optional.ofNullable(context.getMethod())
				.flatMap(method -> Optional.ofNullable(context.getAttribute(CacheKeyGenerationHandler.CACHE_KEY_ATTR , RedisCacheable.class))
						.map(cacheKey -> new Object[]{method, cacheKey}))
				.ifPresent(pair -> {
					try {
						var method = (java.lang.reflect.Method) pair[0];
						var cacheKey = (String) pair[1];

						Optional.ofNullable(method.getAnnotation(RedisCacheable.class))
								.ifPresent(annotation -> {
									String pattern = extractKeyPattern(cacheKey);
									WarmUpPriority priority = annotation.warmUpPriority();
									warmUpManager.triggerWarmUp(pattern, priority);
								});

					} catch (Exception e) {
						logException(context, "cache_warmup", e, "缓存预热失败: " + pair[1]);
					}
				});
	}

	/**
	 * 从缓存键中提取模式
	 *
	 * <p>
	 * 将缓存键中的具体 ID 或字符串替换为通配符，用于缓存批量预热
	 * 例如 key:user:123 -> key:user:*
	 *
	 * @param cacheKey 原始缓存键
	 * @return 替换后的键模式
	 */
	private String extractKeyPattern(String cacheKey) {
		return cacheKey.replaceAll(":\\d+", ":*").replaceAll(":[a-zA-Z0-9]+$", ":*");
	}
}
