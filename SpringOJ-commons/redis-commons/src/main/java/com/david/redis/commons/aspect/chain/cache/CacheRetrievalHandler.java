package com.david.redis.commons.aspect.chain.cache;

import com.david.log.commons.core.LogUtils;
import com.david.redis.commons.annotation.RedisCacheable;
import com.david.redis.commons.aspect.chain.AbstractAspectHandler;
import com.david.redis.commons.aspect.chain.AspectChain;
import com.david.redis.commons.aspect.chain.AspectContext;
import com.david.redis.commons.aspect.chain.AspectType;
import com.david.redis.commons.core.RedisUtils;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 缓存获取处理器
 *
 * <p>负责从 Redis 中获取缓存数据，处理反序列化异常。
 *
 * @author David
 */
@Component
public class CacheRetrievalHandler extends AbstractAspectHandler {

    public static final String CACHED_VALUE_ATTR = "cache.value";
    public static final String CACHE_HIT_ATTR = "cache.hit";

    private final RedisUtils redisUtils;

    /**
     * 构造器注入依赖
     *
     * @param logUtils 日志工具类，用于记录操作日志和异常信息
     * @param redisUtils Redis工具类，用于执行Redis相关操作
     */
    public CacheRetrievalHandler(LogUtils logUtils, RedisUtils redisUtils) {
        super(logUtils);
        this.redisUtils = redisUtils;
    }

    /**
     * 获取当前处理器支持的切面类型
     *
     * @return 支持的切面类型集合，此处返回缓存类型
     */
    @Override
    protected Set<AspectType> getSupportedAspectTypes() {
        return Set.of(AspectType.CACHE);
    }

    /**
     * 获取处理器在切面链中的执行顺序
     *
     * @return 执行顺序值，数值越小越先执行。此处返回20，表示在键生成之后，方法执行之前执行
     */
    @Override
    public int getOrder() {
        return 20; // 在键生成之后，方法执行之前
    }

    /**
     * 判断当前处理器是否能够处理给定的切面上下文
     *
     * @param context 切面上下文，包含方法调用的相关信息
     * @return true表示可以处理，false表示不能处理 需要满足以下条件： 1. 父类的canHandle方法返回true 2. 缓存条件已满足 3. 缓存键已生成
     */
    @Override
    public boolean canHandle(AspectContext context) {
        return super.canHandle(context)
                && context.getAttribute(CacheConditionHandler.CACHE_CONDITION_MET_ATTR, false)
                && context.getAttribute(CacheKeyGenerationHandler.CACHE_KEY_ATTR) != null;
    }

    /**
     * 处理缓存获取逻辑
     *
     * @param context 切面上下文，包含方法调用信息和缓存相关属性
     * @param chain 切面处理链，用于继续执行后续处理器
     * @return 处理结果，如果缓存命中则返回缓存值，否则继续执行链
     * @throws Throwable 处理过程中可能抛出的异常
     *     <p>主要功能： 1. 从上下文获取缓存键 2. 安全地从Redis获取缓存值 3. 设置缓存命中标识和缓存值到上下文 4. 如果缓存命中，设置返回结果并记录日志 5.
     *     继续执行切面链
     */
    @Override
    public Object handle(AspectContext context, AspectChain chain) throws Throwable {
        var cacheKey = context.<String>getAttribute(CacheKeyGenerationHandler.CACHE_KEY_ATTR);

        var cachedValue = getCachedValueSafely(context, cacheKey);
        var cacheHit = cachedValue.isPresent();

        // 设置缓存相关属性
        context.setAttribute(CACHED_VALUE_ATTR, cachedValue.orElse(null));
        context.setAttribute(CACHE_HIT_ATTR, cacheHit);

        if (cacheHit) {
            context.setResult(cachedValue.get());
            logExecution(context, "cache_hit", "缓存命中: " + cacheKey);
        }

        // 继续执行链，无论是否命中缓存
        return chain.proceed(context);
    }

    /**
     * 安全地从缓存获取数据
     *
     * @param context 切面上下文，包含方法信息
     * @param cacheKey 缓存键
     * @return Optional包装的缓存值，如果获取失败或异常则返回空Optional
     *     <p>功能说明： 1. 从方法中获取RedisCacheable注解 2. 调用retrieveFromRedis方法从Redis获取数据 3.
     *     如果获取失败，记录异常日志并返回空Optional 4. 使用Optional链式调用确保空安全
     */
    private Optional<Object> getCachedValueSafely(AspectContext context, String cacheKey) {
        return Optional.ofNullable(context.getMethod())
                .map(method -> method.getAnnotation(RedisCacheable.class))
                .flatMap(annotation -> retrieveFromRedis(context, cacheKey, annotation))
                .or(
                        () -> {
                            logExecution(context, "cache_get_fail", "缓存获取异常: " + cacheKey);
                            return Optional.empty();
                        });
    }

    /**
     * 从Redis检索数据
     *
     * @param context 切面上下文
     * @param cacheKey 缓存键
     * @param annotation RedisCacheable注解，包含缓存配置信息
     * @return Optional包装的缓存值
     *     <p>功能说明： 1. 根据注解中的type属性判断返回类型 2. 如果type不是Object.class，使用强类型获取方法 3.
     *     如果type是Object.class，使用字符串获取方法 4. 捕获异常并调用异常处理方法 5. 将结果包装在Optional中返回
     */
    private Optional<Object> retrieveFromRedis(
            AspectContext context, String cacheKey, RedisCacheable annotation) {
        try {
            var cacheValue =
                    annotation.type() != Object.class
                            ? redisUtils.strings().get(cacheKey, annotation.type())
                            : redisUtils.strings().getString(cacheKey);

            return Optional.ofNullable(cacheValue);

        } catch (Exception e) {
            return handleRetrievalException(context, cacheKey, e);
        }
    }

    /**
     * 处理缓存获取过程中的异常
     *
     * @param context 切面上下文
     * @param cacheKey 缓存键
     * @param e 捕获到的异常
     * @return 空的Optional，表示获取失败
     *     <p>异常处理逻辑： 1. 判断是否为序列化异常 - 如果是序列化异常：记录反序列化失败日志，删除损坏的缓存 2. 判断是否为Redis操作异常 -
     *     如果是Redis操作异常：记录缓存操作失败日志 3. 其他异常：记录详细的异常信息 4. 所有情况都返回空Optional
     */
    private Optional<Object> handleRetrievalException(
            AspectContext context, String cacheKey, Exception e) {
        var isSerializationException = isSerializationException().test(e);

        if (isSerializationException) {
            logExecution(context, "cache_deserialize_fail", "反序列化失败: " + cacheKey);
            deleteCorruptedCacheSafely(cacheKey);
        } else if (e instanceof com.david.redis.commons.exception.RedisOperationException) {
            logExecution(context, "cache_op_fail", "缓存操作失败: " + cacheKey);
        } else {
            logException(context, "cache_retrieval", e, "缓存获取失败: " + cacheKey);
        }

        return Optional.empty();
    }

    /**
     * 创建用于检查是否为序列化异常的谓词函数
     *
     * @return Predicate函数，用于判断异常是否为序列化相关异常
     *     <p>判断逻辑： 1. 直接是Spring的SerializationException 2.
     *     或者是RedisOperationException且其cause是SerializationException
     *     <p>这种设计模式使得异常判断逻辑可以复用，提高代码的可读性和可维护性
     */
    private Predicate<Exception> isSerializationException() {
        return e ->
                e instanceof org.springframework.data.redis.serializer.SerializationException
                        || (e instanceof com.david.redis.commons.exception.RedisOperationException
                                && e.getCause()
                                        instanceof
                                        org.springframework.data.redis.serializer
                                                .SerializationException);
    }

    /**
     * 安全地删除损坏的缓存数据
     *
     * @param cacheKey 要删除的缓存键
     *     <p>功能说明： 1. 尝试删除指定的缓存键对应的数据 2. 如果删除操作失败，捕获异常并记录日志 3. 不向上抛出异常，保证程序的健壮性 4.
     *     主要用于清理因反序列化失败而损坏的缓存数据
     *     <p>设计原则： - 防御性编程：即使删除失败也不影响主流程 - 日志记录：确保删除失败的情况能够被监控到
     */
    private void deleteCorruptedCacheSafely(String cacheKey) {
        try {
            redisUtils.strings().delete(cacheKey);
        } catch (Exception deleteEx) {
            logUtils.exception()
                    .business(
                            "cache_delete_corrupted_failed",
                            deleteEx,
                            "删除损坏缓存失败",
                            "cacheKey: " + cacheKey);
        }
    }
}
