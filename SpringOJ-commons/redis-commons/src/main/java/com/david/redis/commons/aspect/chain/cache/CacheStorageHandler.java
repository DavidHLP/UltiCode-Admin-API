package com.david.redis.commons.aspect.chain.cache;

import com.david.log.commons.core.LogUtils;
import com.david.redis.commons.annotation.RedisCacheable;
import com.david.redis.commons.aspect.chain.AbstractAspectHandler;
import com.david.redis.commons.aspect.chain.AspectChain;
import com.david.redis.commons.aspect.chain.AspectContext;
import com.david.redis.commons.aspect.chain.AspectType;
import com.david.redis.commons.aspect.chain.utils.CacheConditionEvaluator;
import com.david.redis.commons.core.RedisUtils;
import com.david.redis.commons.properties.RedisCommonsProperties;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

/**
 * 缓存存储处理器
 *
 * <p>负责将方法执行结果存储到 Redis 缓存中。
 *
 * @author David
 */
@Component
public class CacheStorageHandler extends AbstractAspectHandler {

	private final RedisUtils redisUtils;
	private final CacheConditionEvaluator conditionEvaluator;
	private final RedisCommonsProperties properties;

	/**
	 * 构造函数 - 初始化缓存存储处理器
	 *
	 * <p>通过依赖注入方式获取所需的工具类和配置信息，用于后续的缓存存储操作。
	 * 包括日志工具、Redis操作工具、缓存条件评估器和Redis配置属性。
	 *
	 * @param logUtils 日志工具类，用于记录操作日志和异常信息
	 * @param redisUtils Redis操作工具类，提供Redis的读写操作
	 * @param conditionEvaluator 缓存条件评估器，用于判断是否满足缓存条件
	 * @param properties Redis通用配置属性，包含默认TTL等配置信息
	 */
	public CacheStorageHandler(
			LogUtils logUtils,
			RedisUtils redisUtils,
			CacheConditionEvaluator conditionEvaluator,
			RedisCommonsProperties properties) {
		super(logUtils);
		this.redisUtils = redisUtils;
		this.conditionEvaluator = conditionEvaluator;
		this.properties = properties;
	}

	/**
	 * 获取支持的切面类型
	 *
	 * <p>定义当前处理器支持处理的切面类型，用于框架识别和分发请求。
	 * 当前处理器只支持CACHE类型的切面操作。
	 *
	 * @return 支持的切面类型集合，包含AspectType.CACHE
	 */
	@Override
	protected Set<AspectType> getSupportedAspectTypes() {
		return Set.of(AspectType.CACHE);
	}

	/**
	 * 获取处理器执行顺序
	 *
	 * <p>定义在切面链中的执行优先级，数值越大执行越晚。
	 * 设置为60确保在方法执行完成后进行缓存存储操作。
	 *
	 * @return 执行顺序值，60表示在方法执行后执行
	 */
	@Override
	public int getOrder() {
		return 60; // 在方法执行之后
	}

	/**
	 * 判断是否可以处理当前请求
	 *
	 * <p>根据多个条件判断当前处理器是否应该处理这个请求：
	 * 1. 首先调用父类的canHandle方法进行基础检查
	 * 2. 检查缓存是否已命中（如果已命中则无需存储）
	 * 3. 检查缓存条件是否满足（通过条件评估器判断）
	 * 4. 检查方法是否已执行完成
	 * 5. 检查方法执行是否无异常
	 * <p>
	 * 只有在缓存未命中、满足缓存条件、方法执行成功的情况下才进行缓存存储。
	 *
	 * @param context 切面上下文，包含方法执行的所有信息和状态
	 * @return true表示可以处理，false表示跳过当前处理器
	 */
	@Override
	public boolean canHandle(AspectContext context) {
		if (!super.canHandle(context)) {
			return false;
		}

		// 使用解构模式和更清晰的条件判断
		var cacheHit = context.getAttribute(CacheRetrievalHandler.CACHE_HIT_ATTR, false);
		var conditionMet =
				context.getAttribute(CacheConditionHandler.CACHE_CONDITION_MET_ATTR, false);

		return !cacheHit && conditionMet && context.isMethodExecuted() && !context.hasException();
	}

	/**
	 * 处理缓存存储逻辑
	 *
	 * <p>这是处理器的核心方法，负责执行实际的缓存存储操作：
	 * 1. 从上下文中获取缓存键和方法执行结果
	 * 2. 判断是否应该缓存该结果（考虑null值处理和缓存条件）
	 * 3. 如果满足缓存条件，则将结果存储到Redis中
	 * 4. 记录缓存存储的操作日志
	 * 5. 即使缓存存储失败也不影响业务逻辑，继续执行后续处理器
	 *
	 * @param context 切面上下文，包含方法执行信息和缓存键等数据
	 * @param chain 切面处理链，用于继续执行后续处理器
	 * @return 方法执行结果（透传）
	 * @throws Throwable 方法执行过程中可能抛出的异常
	 */
	@Override
	public Object handle(AspectContext context, AspectChain chain) throws Throwable {
		var cacheKey = context.<String>getAttribute(CacheKeyGenerationHandler.CACHE_KEY_ATTR);
		var result = context.getResult();

		try {
			// 使用流式API处理缓存逻辑
			Optional.of(context)
					.filter(ctx -> shouldCache(ctx, result))
					.ifPresent(
							ctx -> {
								cacheResult(ctx, cacheKey, result);
								logExecution(ctx, "cache_stored", "缓存存储: " + cacheKey);
							});

			return chain.proceed(context);

		} catch (Exception e) {
			logException(context, "cache_storage", e, "缓存存储异常: " + cacheKey);
			// 缓存存储失败不影响业务逻辑，继续执行
			return chain.proceed(context);
		}
	}

	/**
	 * 判断是否应该缓存结果
	 *
	 * <p>通过多层条件检查来判断当前方法执行结果是否应该被缓存：
	 * 1. 获取方法上的@RedisCacheable注解
	 * 2. 检查是否允许缓存null值或结果不为null
	 * 3. 使用条件评估器检查自定义缓存条件是否满足
	 * <p>
	 * 使用Optional链式调用提高代码可读性并避免空指针异常。
	 *
	 * @param context 切面上下文，用于获取方法信息和执行状态
	 * @param result 方法执行结果，需要判断是否应该被缓存
	 * @return true表示应该缓存，false表示跳过缓存
	 */
	private boolean shouldCache(AspectContext context, Object result) {
		return Optional.ofNullable(context.getMethod())
				.map(method -> method.getAnnotation(RedisCacheable.class))
				.filter(annotation -> shouldCacheWithNullCheck(annotation, result))
				.map(annotation -> evaluateCacheCondition(context, annotation, result))
				.orElse(false);
	}

	/**
	 * 检查是否应该缓存（考虑null值处理）
	 *
	 * <p>根据注解配置和方法执行结果判断是否进行缓存：
	 * - 如果结果不为null，则可以缓存
	 * - 如果结果为null，则检查注解是否允许缓存null值（cacheNullValues=true）
	 * <p>
	 * 这样可以避免缓存不必要的null值，同时支持有意义的null结果缓存。
	 *
	 * @param annotation RedisCacheable注解实例，包含缓存配置信息
	 * @param result 方法执行结果，可能为null
	 * @return true表示应该缓存该结果，false表示不应该缓存
	 */
	private boolean shouldCacheWithNullCheck(RedisCacheable annotation, Object result) {
		return result != null || annotation.cacheNullValues();
	}

	/**
	 * 评估缓存条件
	 *
	 * <p>使用缓存条件评估器来检查注解中定义的自定义缓存条件是否满足。
	 * 条件表达式通常使用SpEL（Spring Expression Language）编写，
	 * 可以基于方法参数、执行结果等进行复杂的条件判断。
	 *
	 * @param context 切面上下文，提供方法和参数信息
	 * @param annotation RedisCacheable注解，包含条件表达式
	 * @param result 方法执行结果，可以在条件表达式中引用
	 * @return true表示满足缓存条件，false表示不满足条件
	 */
	private boolean evaluateCacheCondition(
			AspectContext context, RedisCacheable annotation, Object result) {
		return conditionEvaluator.evaluateCondition(
				annotation.condition(), context.getMethod(), context.getArgs(), result);
	}

	/**
	 * 缓存结果到Redis
	 *
	 * <p>获取方法上的@RedisCacheable注解，并执行实际的缓存存储操作。
	 * 使用Optional模式安全地获取注解信息，避免空指针异常。
	 * 如果注解存在，则调用performCacheStorage方法执行具体的存储逻辑。
	 *
	 * @param context 切面上下文，用于获取方法信息
	 * @param cacheKey 缓存键，用作Redis中的key
	 * @param result 需要缓存的值，将作为Redis中的value
	 */
	private void cacheResult(AspectContext context, String cacheKey, Object result) {
		Optional.ofNullable(context.getMethod())
				.map(method -> method.getAnnotation(RedisCacheable.class))
				.ifPresent(annotation -> performCacheStorage(cacheKey, result, annotation));
	}

	/**
	 * 执行实际的缓存存储操作
	 *
	 * <p>根据注解配置将数据存储到Redis中：
	 * 1. 通过determineTtl方法计算TTL（生存时间）
	 * 2. 使用RedisUtils的字符串操作接口存储数据
	 * 3. 如果存储过程发生异常，包装为RuntimeException重新抛出
	 * <p>
	 * 这样设计可以让上层调用者统一处理缓存存储异常。
	 *
	 * @param cacheKey 缓存键，Redis中的key
	 * @param result 缓存值，Redis中的value
	 * @param annotation RedisCacheable注解，包含TTL等配置信息
	 * @throws RuntimeException 当缓存存储失败时抛出，包装原始异常信息
	 */
	private void performCacheStorage(String cacheKey, Object result, RedisCacheable annotation) {
		try {
			var ttl = determineTtl(annotation);
			redisUtils.strings().set(cacheKey, result, ttl);
		} catch (Exception e) {
			// 重新抛出异常，让调用者处理
			throw new RuntimeException("缓存存储失败: " + cacheKey, e);
		}
	}

	/**
	 * 确定缓存的TTL（生存时间）
	 *
	 * <p>根据注解配置计算缓存数据的过期时间：
	 * - 如果注解中指定了TTL值（大于0），则使用指定的秒数
	 * - 如果注解中未指定TTL或值为0/负数，则使用全局默认TTL配置
	 * <p>
	 * 这种设计既支持方法级别的精细化TTL控制，也提供合理地默认值。
	 *
	 * @param annotation RedisCacheable注解，包含TTL配置
	 * @return Duration对象，表示缓存过期时间间隔
	 */
	private Duration determineTtl(RedisCacheable annotation) {
		var ttlSeconds = annotation.ttl();
		return ttlSeconds > 0
				? Duration.ofSeconds(ttlSeconds)
				: properties.getCache().getDefaultTtl();
	}
}