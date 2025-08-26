package com.david.redis.commons.aspect;

import com.david.redis.commons.annotation.RedisCacheable;
import com.david.redis.commons.annotation.RedisEvict;
import com.david.redis.commons.core.cache.CacheConditionEvaluator;
import com.david.redis.commons.core.cache.CacheKeyGenerator;
import com.david.redis.commons.core.cache.CacheOperationContext;
import com.david.redis.commons.core.RedisUtils;
import com.david.redis.commons.properties.RedisCommonsProperties;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Redis缓存切面，处理@RedisCacheable和@RedisEvict注解
 *
 * @author David
 */
@Slf4j
@Aspect
@Component
public class CacheAspect {

    private final RedisUtils redisUtils;
    private final CacheKeyGenerator keyGenerator;
    private final CacheConditionEvaluator conditionEvaluator;
    private final RedisCommonsProperties properties;

    public CacheAspect(RedisUtils redisUtils,
            CacheKeyGenerator keyGenerator,
            CacheConditionEvaluator conditionEvaluator,
            RedisCommonsProperties properties) {
        this.redisUtils = redisUtils;
        this.keyGenerator = keyGenerator;
        this.conditionEvaluator = conditionEvaluator;
        this.properties = properties;
    }

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

        try {
            // 生成缓存键
            String cacheKey = generateCacheKey(redisCacheable, method, args);
            log.debug("处理缓存注解 @RedisCacheable - 方法: {}, 缓存键: {}",
                    context.getMethodSignature(), cacheKey);

            // 尝试从缓存获取数据
            Object cachedValue = getCachedValue(cacheKey, redisCacheable.type());
            if (cachedValue != null) {
                log.debug("缓存命中 - 键: {}, 值类型: {}", cacheKey, cachedValue.getClass().getSimpleName());
                return cachedValue;
            }

            log.debug("缓存未命中 - 键: {}, 执行原方法", cacheKey);

            // 执行原方法
            Object result = joinPoint.proceed();
            context.setResult(result);

            // 评估缓存条件
            if (shouldCache(redisCacheable, method, args, result)) {
                // 缓存结果
                cacheResult(cacheKey, result, redisCacheable);
                log.debug("缓存结果已保存 - 键: {}, TTL: {}秒", cacheKey, redisCacheable.ttl());
            } else {
                log.debug("缓存条件不满足，跳过缓存 - 键: {}", cacheKey);
            }

            return result;

        } catch (Exception e) {
            log.error("缓存操作失败 - 方法: {}, 参数: {}",
                    context.getMethodSignature(), context.getArgsString(), e);

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
                log.debug("方法执行前驱逐缓存 - 方法: {}", context.getMethodSignature());
                evictCache(redisEvict, method, args, null);
            }

            // 执行原方法
            result = joinPoint.proceed();
            context.setResult(result);

            // 如果需要在方法执行后驱逐缓存（默认行为）
            if (!redisEvict.beforeInvocation()) {
                log.debug("方法执行后驱逐缓存 - 方法: {}", context.getMethodSignature());
                evictCache(redisEvict, method, args, result);
            }

            return result;

        } catch (Exception e) {
            log.error("缓存驱逐操作失败 - 方法: {}, 参数: {}",
                    context.getMethodSignature(), context.getArgsString(), e);
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
     * 从缓存获取值
     */
    private Object getCachedValue(String cacheKey, Class<?> type) {
        try {
            if (type == Object.class) {
                return redisUtils.get(cacheKey, Object.class);
            } else {
                return redisUtils.get(cacheKey, type);
            }
        } catch (Exception e) {
            log.warn("从缓存获取数据失败 - 键: {}", cacheKey, e);
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
                redisUtils.set(cacheKey, result, Duration.ofSeconds(ttlSeconds));
            } else {
                // 使用默认TTL
                Duration defaultTtl = properties.getCache().getDefaultTtl();
                redisUtils.set(cacheKey, result, defaultTtl);
            }
        } catch (Exception e) {
            log.error("缓存结果失败 - 键: {}", cacheKey, e);
        }
    }

    /**
     * 驱逐缓存
     */
    private void evictCache(RedisEvict redisEvict, Method method, Object[] args, Object result) {
        try {
            // 评估驱逐条件
            if (!conditionEvaluator.evaluateCondition(redisEvict.condition(), method, args, result)) {
                log.debug("缓存驱逐条件不满足，跳过驱逐操作");
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
            log.error("缓存驱逐失败", e);
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
        Set<String> keys = redisUtils.scanKeys(pattern);
        if (keys.isEmpty()) {
            // fallback to KEYS in case SCAN returns empty due to server config/count
            keys = redisUtils.keys(pattern);
        }

        if (!keys.isEmpty()) {
            Long deletedCount = redisUtils.delete(keys.toArray(new String[0]));
            log.info("批量驱逐缓存完成 - 模式: {}, 删除数量: {}", pattern, deletedCount);
        } else {
            log.debug("未找到匹配的缓存键 - 模式: {}", pattern);
        }
    }

    /**
     * 驱逐指定键的缓存
     */
    private void evictSpecificKeys(RedisEvict redisEvict, Method method, Object[] args) {
        String[] keyExpressions = redisEvict.keys();
        if (keyExpressions.length == 0) {
            log.warn("@RedisEvict注解未指定要驱逐的键");
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
                log.error("生成驱逐键失败 - 表达式: {}", keyExpression, e);
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
                Long exactDeleted = redisUtils.delete(exactKeys.toArray(new String[0]));
                totalDeleted += (exactDeleted != null ? exactDeleted.intValue() : 0);
                log.debug("删除精确键 {} 个，成功删除: {}", exactKeys.size(), exactDeleted);
            }

            // 处理模式键 - 使用 KEYS + DEL 组合
            for (String pattern : patternKeys) {
                Long patternDeleted = deleteByPattern(pattern);
                totalDeleted += (patternDeleted != null ? patternDeleted.intValue() : 0);
                log.debug("删除模式键 '{}' 匹配的缓存，成功删除: {}", pattern, patternDeleted);
            }

            log.info("缓存驱逐完成 - 原始键数量: {}, 总删除数量: {}", keysToEvict.size(), totalDeleted);

            if (log.isDebugEnabled()) {
                log.debug("驱逐键详情 - 精确键: {}, 模式键: {}", exactKeys, patternKeys);
            }
        }
    }

    /**
     * 根据模式删除缓存键
     * 使用优化的批量删除策略
     */
    private Long deleteByPattern(String pattern) {
        try {
            Set<String> matchedKeys = redisUtils.scanKeys(pattern);
            if (matchedKeys.isEmpty()) {
                // fallback to KEYS if needed
                matchedKeys = redisUtils.keys(pattern);
            }
            if (!matchedKeys.isEmpty()) {
                return redisUtils.delete(matchedKeys.toArray(new String[0]));
            }
            return 0L;
        } catch (Exception e) {
            log.error("根据模式删除缓存失败 - 模式: {}", pattern, e);
            return 0L;
        }
    }
}