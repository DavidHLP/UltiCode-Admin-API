package com.david.redis.commons.aspect.chain.cache;

import com.david.log.commons.core.LogUtils;
import com.david.redis.commons.annotation.RedisCacheable;
import com.david.redis.commons.aspect.chain.AbstractAspectHandler;
import com.david.redis.commons.aspect.chain.AspectChain;
import com.david.redis.commons.aspect.chain.AspectContext;
import com.david.redis.commons.aspect.chain.AspectType;
import com.david.redis.commons.core.RedisUtils;
import com.david.redis.commons.aspect.chain.utils.CacheConditionEvaluator;
import com.david.redis.commons.properties.RedisCommonsProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

/**
 * 缓存存储处理器
 *
 * <p>
 * 负责将方法执行结果存储到 Redis 缓存中。
 *
 * @author David
 */
@Component
public class CacheStorageHandler extends AbstractAspectHandler {

    private final RedisUtils redisUtils;
    private final CacheConditionEvaluator conditionEvaluator;
    private final RedisCommonsProperties properties;

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

    @Override
    protected Set<AspectType> getSupportedAspectTypes() {
        return Set.of(AspectType.CACHE);
    }

    @Override
    public int getOrder() {
        return 60; // 在方法执行之后
    }

    @Override
    public boolean canHandle(AspectContext context) {
        if (!super.canHandle(context)) {
            return false;
        }

        // 只有在缓存未命中且方法已执行时才需要存储
        Boolean cacheHit = context.getAttribute(CacheRetrievalHandler.CACHE_HIT_ATTR, false);
        Boolean conditionMet = context.getAttribute(CacheConditionHandler.CACHE_CONDITION_MET_ATTR, false);

        return !cacheHit && conditionMet && context.isMethodExecuted() && !context.hasException();
    }

    @Override
    public Object handle(AspectContext context, AspectChain chain) throws Throwable {
        String cacheKey = context.getAttribute(CacheKeyGenerationHandler.CACHE_KEY_ATTR);
        Object result = context.getResult();

        try {
            if (shouldCache(context, result)) {
                cacheResult(context, cacheKey, result);
                logExecution(context, "cache_stored", "缓存存储: " + cacheKey);
            }

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
     * @param context 切面上下文
     * @param result  方法执行结果
     * @return true 如果应该缓存
     */
    private boolean shouldCache(AspectContext context, Object result) {
        if (context.getMethod() == null) {
            return false; // 虚拟上下文不缓存
        }

        RedisCacheable annotation = context.getMethod().getAnnotation(RedisCacheable.class);
        if (annotation == null) {
            return false;
        }

        // 检查是否缓存null值
        if (result == null && !annotation.cacheNullValues()) {
            return false;
        }

        // 评估缓存条件（基于结果的条件）
        return conditionEvaluator.evaluateCondition(
                annotation.condition(), context.getMethod(), context.getArgs(), result);
    }

    /**
     * 缓存结果
     *
     * @param context  切面上下文
     * @param cacheKey 缓存键
     * @param result   缓存值
     */
    private void cacheResult(AspectContext context, String cacheKey, Object result) {
        if (context.getMethod() == null) {
            return; // 虚拟上下文不缓存
        }

        try {
            RedisCacheable annotation = context.getMethod().getAnnotation(RedisCacheable.class);
            if (annotation == null) {
                return;
            }

            long ttlSeconds = annotation.ttl();
            Duration defaultTtl = properties.getCache().getDefaultTtl();
            if (ttlSeconds > 0) {
                redisUtils.strings().set(cacheKey, result, Duration.ofSeconds(ttlSeconds));
            } else {
                // 使用默认TTL
                redisUtils.strings().set(cacheKey, result, defaultTtl);
            }

            // 缓存存储成功，在上层已记录

        } catch (Exception e) {
            logException(context, "cache_storage", e, "缓存存储失败: " + cacheKey);
            throw e;
        }
    }
}
