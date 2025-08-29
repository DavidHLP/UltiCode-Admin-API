package com.david.redis.commons.aspect;

import com.david.log.commons.core.LogUtils;
import com.david.redis.commons.annotation.RedisCacheable;
import com.david.redis.commons.annotation.RedisEvict;
import com.david.redis.commons.core.RedisUtils;
import com.david.redis.commons.core.cache.CacheConditionEvaluator;
import com.david.redis.commons.core.cache.CacheKeyGenerator;
import com.david.redis.commons.core.cache.CacheOperationContext;
import com.david.redis.commons.enums.WarmUpPriority;
import com.david.redis.commons.manager.BatchOperationManager;
import com.david.redis.commons.manager.CacheWarmUpManager;
import com.david.redis.commons.monitor.CacheMetricsCollector;
import com.david.redis.commons.properties.RedisCommonsProperties;

import lombok.RequiredArgsConstructor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Redis缓存切面，处理@RedisCacheable和@RedisEvict注解
 *
 * @author David
 */
@Aspect
@Component
@RequiredArgsConstructor
public class CacheAspect {

    private static final Logger log = LoggerFactory.getLogger(CacheAspect.class);

    private final RedisUtils redisUtils;
    private final CacheKeyGenerator keyGenerator;
    private final CacheConditionEvaluator conditionEvaluator;
    private final RedisCommonsProperties properties;
    private final BatchOperationManager batchManager;
    private final CacheWarmUpManager warmUpManager;
    private final CacheMetricsCollector metricsCollector;
    private final LogUtils logUtils;

    /**
     * 处理@RedisCacheable注解的方法
     */
    @Around("@annotation(redisCacheable)")
    public Object handleCacheable(ProceedingJoinPoint joinPoint, RedisCacheable redisCacheable) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        Object target = joinPoint.getTarget();

        // 创建缓存操作上下文
        CacheOperationContext context = new CacheOperationContext(method, args, target);
        long startTime = System.currentTimeMillis();

        try {
            // 生成缓存键
            String cacheKey = generateCacheKey(redisCacheable, method, args);
            logUtils.business().trace("cache_aspect", "cacheable", "process", "method: " + context.getMethodSignature(), "cacheKey: " + cacheKey);

            // 处理批量操作
            if (redisCacheable.batchSize() > 1) {
                return handleBatchCacheable(joinPoint, redisCacheable, cacheKey, method, args);
            }

            // 尝试从缓存获取数据
            Object cachedValue = getCachedValue(cacheKey, redisCacheable.type());
            long responseTime = System.currentTimeMillis() - startTime;

            if (cachedValue != null) {
                // 记录缓存命中指标
                if (redisCacheable.enableMetrics()) {
                    metricsCollector.recordHit(cacheKey, responseTime);
                }

                logUtils.business().trace("cache_aspect", "cacheable", "hit", "cacheKey: " + cacheKey, "valueType: " + cachedValue.getClass().getSimpleName());

                // 检查是否需要刷新缓存
                if (shouldRefreshCache(cacheKey, redisCacheable)) {
                    CompletableFuture.runAsync(() -> refreshCacheAsync(joinPoint, cacheKey, redisCacheable));
                }

                return cachedValue;
            }

            // 记录缓存未命中指标
            if (redisCacheable.enableMetrics()) {
                metricsCollector.recordMiss(cacheKey, responseTime);
            }

            logUtils.business().trace("cache_aspect", "cacheable", "miss", "cacheKey: " + cacheKey, "executing original method");

            // 执行原方法
            Object result = joinPoint.proceed();
            context.setResult(result);

            // 评估缓存条件
            if (shouldCache(redisCacheable, method, args, result)) {
                // 缓存结果
                cacheResult(cacheKey, result, redisCacheable);

                // 记录缓存写入指标
                if (redisCacheable.enableMetrics()) {
                    long writeTime = System.currentTimeMillis() - startTime;
                    metricsCollector.recordSet(cacheKey, writeTime);
                }

                logUtils.business().trace("cache_aspect", "cacheable", "cached", "cacheKey: " + cacheKey, "ttl: " + redisCacheable.ttl() + " seconds");

                // 触发预热
                if (redisCacheable.warmUp()) {
                    triggerWarmUp(cacheKey, redisCacheable);
                }
            } else {
                logUtils.business().trace("cache_aspect", "cacheable", "condition_not_met", "cacheKey: " + cacheKey);
            }

            return result;

        } catch (Exception e) {
            long errorTime = System.currentTimeMillis() - startTime;
            if (redisCacheable.enableMetrics()) {
                metricsCollector.recordError("CACHE_GET", errorTime);
            }

            logUtils.exception().business("cache_operation_failed", e, "缓存操作失败", "method: " + context.getMethodSignature(), "args: " + context.getArgsString());

            // 缓存失败时仍然执行原方法
            return joinPoint.proceed();
        }
    }

    /**
     * 处理@RedisEvict注解的方法
     */
    @Around("@annotation(redisEvict)")
    public Object handleEvict(ProceedingJoinPoint joinPoint, RedisEvict redisEvict) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        Object target = joinPoint.getTarget();

        // 创建缓存操作上下文
        CacheOperationContext context = new CacheOperationContext(method, args, target);

        try {
            Object result = null;

            // 如果需要在方法执行前驱逐缓存
            if (redisEvict.beforeInvocation()) {
                logUtils.business().trace("cache_aspect", "evict", "before_invocation", "method: " + context.getMethodSignature());
                evictCache(redisEvict, method, args, null);
            }

            // 执行原方法
            result = joinPoint.proceed();
            context.setResult(result);

            // 如果需要在方法执行后驱逐缓存（默认行为）
            if (!redisEvict.beforeInvocation()) {
                logUtils.business().trace("cache_aspect", "evict", "after_invocation", "method: " + context.getMethodSignature());
                evictCache(redisEvict, method, args, result);
            }

            return result;

        } catch (Exception e) {
            logUtils.exception().business("cache_evict_failed", e, "缓存驱逐操作失败", "method: " + context.getMethodSignature(), "args: " + context.getArgsString());
            throw e;
        }
    }

    /**
     * 生成缓存键
     */
    private String generateCacheKey(RedisCacheable redisCacheable, Method method, Object[] args) {
        String keyExpression = redisCacheable.key();
        String generatedKey = keyGenerator.generateKey(keyExpression, method, args);

        // 添加键前缀
        String keyPrefix = StringUtils.hasText(redisCacheable.keyPrefix())
                ? redisCacheable.keyPrefix()
                : properties.getCache().getKeyPrefix();

        return keyPrefix + generatedKey;
    }

    /**
     * 从缓存获取数据
     */
    private Object getCachedValue(String cacheKey, Class<?> type) {
        try {
            if (type != Object.class) {
                // 如果指定了具体类型，直接使用类型化的get方法
                return redisUtils.strings().get(cacheKey, type);
            } else {
                // 使用字符串方法获取原始值
                return redisUtils.strings().getString(cacheKey);
            }
        } catch (org.springframework.data.redis.serializer.SerializationException e) {
            // 反序列化失败，可能是数据格式不兼容，删除损坏的缓存
            logUtils.business().trace("cache_aspect", "deserialization_failed", "corrupted_cache", "cacheKey: " + cacheKey, "error: " + e.getMessage());
            try {
                redisUtils.strings().delete(cacheKey);
            } catch (Exception deleteEx) {
                logUtils.exception().business("cache_delete_corrupted_failed", deleteEx, "high", "删除损坏缓存失败", "cacheKey: " + cacheKey);
            }
            return null;
        } catch (com.david.redis.commons.exception.RedisOperationException e) {
            // Redis操作异常，可能包含反序列化错误
            if (e.getCause() instanceof org.springframework.data.redis.serializer.SerializationException) {
                logUtils.business().trace("cache_aspect", "deserialization_failed", "redis_operation_exception", "cacheKey: " + cacheKey, "error: " + e.getMessage());
                try {
                    redisUtils.strings().delete(cacheKey);
                } catch (Exception deleteEx) {
                    logUtils.exception().business("cache_delete_corrupted_failed", deleteEx, "删除损坏缓存失败", "cacheKey: " + cacheKey);
                }
                return null;
            }
            logUtils.business().trace("cache_aspect", "get_cache_failed", "Redis操作异常", "cacheKey: " + cacheKey, "error: " + e.getMessage());
            return null;
        } catch (Exception e) {
            logUtils.business().trace("cache_aspect", "get_cache_failed", "获取缓存异常", "cacheKey: " + cacheKey, "error: " + e.getMessage());
            return null;
        }
    }

    /**
     * 判断是否应该缓存结果
     */
    private boolean shouldCache(RedisCacheable redisCacheable, Method method, Object[] args, Object result) {
        // 检查是否缓存null值
        if (result == null && !redisCacheable.cacheNullValues()) {
            return false;
        }

        // 评估缓存条件
        return conditionEvaluator.evaluateCondition(redisCacheable.condition(), method, args, result);
    }

    /**
     * 缓存结果
     */
    private void cacheResult(String cacheKey, Object result, RedisCacheable redisCacheable) {
        try {
            long ttlSeconds = redisCacheable.ttl();
            if (ttlSeconds > 0) {
                redisUtils.strings().set(cacheKey, result, Duration.ofSeconds(ttlSeconds));
            } else {
                // 使用默认TTL
                Duration defaultTtl = properties.getCache().getDefaultTtl();
                redisUtils.strings().set(cacheKey, result, defaultTtl);
            }
        } catch (Exception e) {
            logUtils.exception().business("cache_result_failed", e, "medium", "缓存结果失败", "cacheKey: " + cacheKey);
        }
    }

    /**
     * 驱逐缓存
     */
    private void evictCache(RedisEvict redisEvict, Method method, Object[] args, Object result) {
        try {
            // 评估驱逐条件
            if (!conditionEvaluator.evaluateCondition(redisEvict.condition(), method, args, result)) {
                logUtils.business().trace("cache_aspect", "evict", "condition_not_met", "缓存驱逐条件不满足");
                return;
            }

            if (redisEvict.allEntries()) {
                // 驱逐所有条目
                evictAllEntries(redisEvict);
            } else {
                // 驱逐指定键
                evictSpecificKeys(redisEvict, method, args);
            }
        } catch (Exception e) {
            logUtils.exception().business("cache_evict_general_failed", e, "medium", "缓存驱逐失败");
        }
    }

    /**
     * 驱逐所有缓存条目
     */
    private void evictAllEntries(RedisEvict redisEvict) {
        String keyPrefix = StringUtils.hasText(redisEvict.keyPrefix())
                ? redisEvict.keyPrefix()
                : properties.getCache().getKeyPrefix();

        String pattern = keyPrefix + "*";
        Set<String> keys = redisUtils.strings().scanKeys(pattern);
        if (keys.isEmpty()) {
            // fallback to KEYS in case SCAN returns empty due to server config/count
            keys = redisUtils.strings().keys(pattern);
        }

        if (!keys.isEmpty()) {
            Long deletedCount = redisUtils.strings().delete(keys.toArray(new String[0]));
            logUtils.business().event("cache_aspect", "evict_all", "batch_complete", "pattern: " + pattern, "deletedCount: " + deletedCount);
        } else {
            logUtils.business().trace("cache_aspect", "evict_all", "no_match", "pattern: " + pattern);
        }
    }

    /**
     * 驱逐指定键的缓存
     */
    private void evictSpecificKeys(RedisEvict redisEvict, Method method, Object[] args) {
        String[] keyExpressions = redisEvict.keys();
        if (keyExpressions.length == 0) {
            logUtils.business().trace("cache_aspect", "evict_specific", "no_keys_specified", "@RedisEvict注解未指定要驱逐的键");
            return;
        }

        List<String> keysToEvict = new ArrayList<>();
        String keyPrefix = StringUtils.hasText(redisEvict.keyPrefix())
                ? redisEvict.keyPrefix()
                : properties.getCache().getKeyPrefix();

        for (String keyExpression : keyExpressions) {
            try {
                String generatedKey = keyGenerator.generateKey(keyExpression, method, args);
                String fullKey = keyPrefix + generatedKey;
                keysToEvict.add(fullKey);
            } catch (Exception e) {
                logUtils.exception().business("evict_key_generation_failed", e, "medium", "生成驱逐键失败", "keyExpression: " + keyExpression);
            }
        }

        if (!keysToEvict.isEmpty()) {
            // 优化的缓存删除策略
            List<String> exactKeys = new ArrayList<>();
            List<String> patternKeys = new ArrayList<>();

            // 分离精确键和模式键
            for (String key : keysToEvict) {
                if (key.contains("*") || key.contains("?")) {
                    patternKeys.add(key);
                } else {
                    exactKeys.add(key);
                }
            }

            int totalDeleted = 0;

            // 批量删除精确键
            if (!exactKeys.isEmpty()) {
                Long exactDeleted = redisUtils.strings().delete(exactKeys.toArray(new String[0]));
                totalDeleted += (exactDeleted != null ? exactDeleted.intValue() : 0);
                logUtils.business().trace("cache_aspect", "evict_specific", "exact_keys_deleted", "count: " + exactKeys.size(), "deleted: " + exactDeleted);
            }

            // 处理模式键 - 使用 KEYS + DEL 组合
            for (String pattern : patternKeys) {
                Long patternDeleted = deleteByPattern(pattern);
                totalDeleted += (patternDeleted != null ? patternDeleted.intValue() : 0);
                logUtils.business().trace("cache_aspect", "evict_specific", "pattern_keys_deleted", "pattern: " + pattern, "deleted: " + patternDeleted);
            }

            logUtils.business().event("缓存驱逐完成 - 原始键数量: {}, 总删除数量: {}", String.valueOf(keysToEvict.size()), String.valueOf(totalDeleted));

            if (log.isDebugEnabled()) {
                logUtils.business().trace("cache_aspect", "evict_keys_detail", "驱逐键详情", "exactKeys: " + exactKeys, "patternKeys: " + patternKeys);
            }
        }
    }

    /**
     * 根据模式删除缓存键
     * 使用优化的批量删除策略
     */
    private Long deleteByPattern(String pattern) {
        try {
            Set<String> matchedKeys = redisUtils.strings().scanKeys(pattern);
            if (matchedKeys.isEmpty()) {
                // fallback to KEYS if needed
                matchedKeys = redisUtils.strings().keys(pattern);
            }
            if (!matchedKeys.isEmpty()) {
                return redisUtils.strings().delete(matchedKeys.toArray(new String[0]));
            }
            return 0L;
        } catch (Exception e) {
            logUtils.exception().business("根据模式删除缓存失败", e, "pattern: " + pattern);
            return 0L;
        }
    }

    /**
     * 处理批量缓存操作
     */
    private Object handleBatchCacheable(ProceedingJoinPoint joinPoint, RedisCacheable redisCacheable,
            String cacheKey, Method method, Object[] args) throws Throwable {
        try {
            // 对于批量操作，直接使用原始缓存键，不生成批量键
            Object cachedValue = getCachedValue(cacheKey, redisCacheable.type());
            
            if (cachedValue != null) {
                logUtils.business().trace("cache_aspect", "batch_cache", "hit", "cacheKey: " + cacheKey);
                return cachedValue;
            }

            // 执行原方法
            Object result = joinPoint.proceed();

            // 缓存结果
            if (result != null && shouldCache(redisCacheable, method, args, result)) {
                Map<String, Object> batchData = new HashMap<>();
                batchData.put(cacheKey, result);
                batchManager.batchSet(batchData, redisCacheable.ttl());
                logUtils.business().trace("cache_aspect", "batch_cache", "saved", "cacheKey: " + cacheKey);
            }

            return result;

        } catch (Exception e) {
            logUtils.exception().business("批量缓存操作失败", e, "cacheKey: " + cacheKey);
            return joinPoint.proceed();
        }
    }


    /**
     * 检查是否需要刷新缓存
     */
    private boolean shouldRefreshCache(String cacheKey, RedisCacheable redisCacheable) {
        if (redisCacheable.refreshThreshold() <= 0) {
            return false;
        }

        try {
            Long ttl = redisUtils.strings().getExpire(cacheKey);
            if (ttl != null && ttl > 0) {
                // 如果剩余TTL小于刷新阈值，则需要刷新
                return ttl < redisCacheable.refreshThreshold();
            }
        } catch (Exception e) {
            logUtils.business().trace("cache_aspect", "refresh_cache", "检查刷新条件异常", "cacheKey: " + cacheKey, "error: " + e.getMessage());
        }

        return false;
    }

    /**
     * 异步刷新缓存
     */
    private void refreshCacheAsync(ProceedingJoinPoint joinPoint, String cacheKey, RedisCacheable redisCacheable) {
        try {
            // 执行原方法获取新数据
            Object newValue = joinPoint.proceed();
            if (newValue != null) {
                // 更新缓存
                cacheResult(cacheKey, newValue, redisCacheable);
                logUtils.business().trace("cache_aspect", "refresh_cache", "异步刷新完成", "cacheKey: " + cacheKey);
            }
        } catch (Throwable e) {
            logUtils.exception().business("异步刷新缓存失败", e, "cacheKey: " + cacheKey);
        }
    }

    /**
     * 触发缓存预热
     */
    private void triggerWarmUp(String cacheKey, RedisCacheable redisCacheable) {
        try {
            // 提取键模式用于预热
            String pattern = extractKeyPattern(cacheKey);
            WarmUpPriority priority = redisCacheable.warmUpPriority();

            // 触发预热任务
            warmUpManager.triggerWarmUp(pattern, priority);

            logUtils.business().trace("cache_aspect", "warm_up", "triggered", "pattern: " + pattern, "priority: " + priority);

        } catch (Exception e) {
            logUtils.exception().business("触发缓存预热失败", e, "cacheKey: " + cacheKey);
        }
    }

    /**
     * 从缓存键提取模式
     */
    private String extractKeyPattern(String cacheKey) {
        // 简单的模式提取：将具体值替换为通配符
        return cacheKey.replaceAll(":\\d+", ":*")
                .replaceAll(":[a-zA-Z0-9]+$", ":*");
    }
}