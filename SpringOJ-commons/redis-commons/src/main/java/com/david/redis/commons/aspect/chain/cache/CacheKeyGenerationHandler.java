package com.david.redis.commons.aspect.chain.cache;

import com.david.log.commons.LogUtils;
import com.david.redis.commons.annotation.RedisCacheable;
import com.david.redis.commons.annotation.RedisEvict;
import com.david.redis.commons.aspect.chain.AbstractAspectHandler;
import com.david.redis.commons.aspect.chain.AspectChain;
import com.david.redis.commons.aspect.chain.AspectContext;
import com.david.redis.commons.aspect.chain.AspectType;
import com.david.redis.commons.aspect.chain.utils.CacheKeyGenerator;
import com.david.redis.commons.properties.RedisCommonsProperties;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.Set;

/**
 * 缓存键生成处理器
 *
 * <p>
 * 负责生成缓存键，包括键表达式解析和前缀处理。
 *
 * @author David
 */
@Component
public class CacheKeyGenerationHandler extends AbstractAspectHandler {

	public static final String CACHE_KEY_ATTR = "cache.key";
	public static final String CACHE_KEY_PREFIX_ATTR = "cache.key.prefix";

	private final CacheKeyGenerator keyGenerator;
	private final RedisCommonsProperties properties;

	/**
	 * 构造方法，初始化缓存键生成处理器
	 *
	 * @param logUtils 日志工具类，用于记录操作日志和异常信息
	 * @param keyGenerator 缓存键生成器，负责根据表达式和方法参数生成具体的缓存键
	 * @param properties Redis配置属性，包含默认的键前缀等配置信息
	 */
	public CacheKeyGenerationHandler(LogUtils logUtils, CacheKeyGenerator keyGenerator,
	                                 RedisCommonsProperties properties) {
		super(logUtils);
		this.keyGenerator = keyGenerator;
		this.properties = properties;
	}

	/**
	 * 获取当前处理器支持的切面类型集合
	 *
	 * <p>此方法定义了处理器能够处理的切面类型，用于框架判断是否需要调用此处理器</p>
	 *
	 * @return 支持的切面类型集合，包括CACHE（缓存）和CACHE_EVICT（缓存清除）
	 */
	@Override
	protected Set<AspectType> getSupportedAspectTypes() {
		return Set.of(AspectType.CACHE, AspectType.CACHE_EVICT);
	}

	/**
	 * 获取处理器在责任链中的执行顺序
	 *
	 * <p>返回值越小，执行优先级越高。此处理器设置为10，确保在条件判断之后、缓存获取之前执行</p>
	 *
	 * @return 执行顺序值，当前设置为10
	 */
	@Override
	public int getOrder() {
		return 10; // 在条件判断之后，在缓存获取之前
	}

	/**
	 * 处理缓存键生成的核心方法
	 *
	 * <p>此方法是责任链模式的主要入口点，负责生成缓存键并将其存储到上下文中，然后继续执行责任链</p>
	 *
	 * @param context 切面上下文，包含方法信息、参数、注解等数据
	 * @param chain 责任链对象，用于继续执行下一个处理器
	 * @return 责任链执行的结果
	 * @throws Throwable 执行过程中可能抛出的任何异常
	 */
	@Override
	public Object handle(AspectContext context, AspectChain chain) throws Throwable {
		try {
			var cacheKey = generateCacheKey(context);
			context.setAttribute(CACHE_KEY_ATTR, cacheKey);

			return chain.proceed(context);

		} catch (Exception e) {
			logException(context, "cache_key_generation", e,
					String.format("缓存键生成失败: %s", context.getMethod().getName()));
			throw e;
		}
	}

	/**
	 * 生成完整的缓存键
	 *
	 * <p>此方法结合键表达式和键前缀生成最终的缓存键，是缓存键生成的核心逻辑</p>
	 *
	 * @param context 切面上下文，包含方法信息和参数
	 * @return 生成的完整缓存键（包含前缀）
	 */
	private String generateCacheKey(AspectContext context) {
		var keyExpression = getKeyExpression(context);
		var generatedKey = keyGenerator.generateKey(keyExpression, context.getMethod(), context.getArgs());

		// 添加键前缀
		var keyPrefix = getKeyPrefix(context);
		var fullKey = keyPrefix + generatedKey;

		context.setAttribute(CACHE_KEY_PREFIX_ATTR, keyPrefix);

		return fullKey;
	}

	/**
	 * 从注解中提取键表达式
	 *
	 * <p>根据不同的切面类型（CACHE或CACHE_EVICT），从相应的注解中获取键表达式。
	 * 键表达式通常包含SpEL表达式，用于动态生成缓存键</p>
	 *
	 * @param context 切面上下文，用于获取方法和切面类型信息
	 * @return 键表达式字符串，如果未找到则返回空字符串
	 */
	private String getKeyExpression(AspectContext context) {
		return Optional.ofNullable(context.getMethod())
				.map(method -> switch (context.getAspectType()) {
					case CACHE -> Optional.ofNullable(method.getAnnotation(RedisCacheable.class))
							.map(RedisCacheable::key)
							.orElse("");
					case CACHE_EVICT -> Optional.ofNullable(method.getAnnotation(RedisEvict.class))
							.filter(annotation -> annotation.keys().length > 0)
							.map(annotation -> annotation.keys()[0])
							.orElse("");
					default -> "";
				})
				.orElse(""); // 虚拟上下文返回空键表达式
	}

	/**
	 * 获取缓存键前缀
	 *
	 * <p>优先使用注解中指定的键前缀，如果注解中未指定或为空，则使用配置文件中的默认键前缀</p>
	 *
	 * @param context 切面上下文，用于获取注解信息
	 * @return 键前缀字符串
	 */
	private String getKeyPrefix(AspectContext context) {
		return getAnnotationKeyPrefix(context)
				.filter(StringUtils::hasText)
				.orElse(properties.getCache().getKeyPrefix());
	}

	/**
	 * 从注解中获取键前缀
	 *
	 * <p>根据切面类型从相应的注解（RedisCacheable或RedisEvict）中提取keyPrefix属性值</p>
	 *
	 * @param context 切面上下文，包含方法和切面类型信息
	 * @return Optional包装的键前缀，如果注解中未指定则返回empty
	 */
	private Optional<String> getAnnotationKeyPrefix(AspectContext context) {
		return Optional.ofNullable(context.getMethod())
				.flatMap(method -> switch (context.getAspectType()) {
					case CACHE -> Optional.ofNullable(method.getAnnotation(RedisCacheable.class))
							.map(RedisCacheable::keyPrefix);
					case CACHE_EVICT -> Optional.ofNullable(method.getAnnotation(RedisEvict.class))
							.map(RedisEvict::keyPrefix);
					default -> Optional.empty();
				});
	}
}