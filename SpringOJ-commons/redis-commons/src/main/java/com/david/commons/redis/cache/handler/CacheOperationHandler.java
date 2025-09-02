package com.david.commons.redis.cache.handler;

import com.david.commons.redis.RedisUtils;
import com.david.commons.redis.cache.CacheMetadata;
import com.david.commons.redis.cache.aspect.CacheContext;
import com.david.commons.redis.cache.expression.CacheExpressionEvaluator;
import com.david.commons.redis.config.RedisCommonsProperties;
import com.david.commons.redis.operations.RedisStringOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;

/**
 * 缓存操作处理器
 * <p>
 * 负责处理具体的缓存操作，包括缓存查询、更新、清除等。
 * 封装了与 Redis 交互的底层逻辑。
 * </p>
 *
 * @author David
 */
@Component
@Slf4j
public class CacheOperationHandler {

    private final RedisUtils redisUtils;
    private final CacheExpressionEvaluator expressionEvaluator;
    private final RedisCommonsProperties properties;

    public CacheOperationHandler(RedisUtils redisUtils,
            CacheExpressionEvaluator expressionEvaluator,
            RedisCommonsProperties properties) {
        this.redisUtils = redisUtils;
        this.expressionEvaluator = expressionEvaluator;
        this.properties = properties;
    }

    /**
     * 处理 @RedisCacheable 操作 - 查询缓存
     *
     * @param metadata 缓存元数据
     * @param context  缓存上下文
     * @return 缓存值，如果缓存未命中返回 null
     */
    public Object handleCacheable(CacheMetadata metadata, CacheContext context) {
        try {
            String cacheKey = buildCacheKey(metadata, context, null);
            log.debug("Checking cache for key: {}", cacheKey);

            Class<?> valueType = metadata.getReturnType() != null ? metadata.getReturnType() : Object.class;
            @SuppressWarnings({"rawtypes", "unchecked"})
            RedisStringOperations<Object> stringOps = (RedisStringOperations) redisUtils
                    .string(metadata.getSerialization(), (Class) valueType);
            Object cachedValue = stringOps.get(cacheKey);

            if (cachedValue != null) {
                log.debug("Cache hit for key: {}", cacheKey);
                return cachedValue;
            } else {
                log.debug("Cache miss for key: {}", cacheKey);
                return null;
            }
        } catch (Exception e) {
            log.error("Error handling cacheable operation", e);
            return null; // 缓存操作失败时返回 null，让方法正常执行
        }
    }

    /**
     * 处理 @RedisCacheable 操作 - 缓存结果
     *
     * @param metadata 缓存元数据
     * @param context  缓存上下文
     */
    public void handleCacheableResult(CacheMetadata metadata, CacheContext context) {
        try {
            Object result = context.getResult();

            // 检查是否应该缓存空值
            if (result == null && !metadata.isCacheNull()) {
                log.debug("Skipping null value caching for key pattern: {}", metadata.getKey());
                return;
            }

            String cacheKey = buildCacheKey(metadata, context, result);
            long ttl = determineTtl(metadata, result);

            log.debug("Caching result for key: {}, ttl: {}", cacheKey, ttl);

            Class<?> valueType = metadata.getReturnType() != null ? metadata.getReturnType() : Object.class;
            @SuppressWarnings({"rawtypes", "unchecked"})
            RedisStringOperations<Object> stringOps = (RedisStringOperations) redisUtils
                    .string(metadata.getSerialization(), (Class) valueType);

            if (ttl > 0) {
                stringOps.set(cacheKey, result, ttl, TimeUnit.SECONDS);
            } else if (ttl == 0) {
                stringOps.set(cacheKey, result); // 永不过期
            } else {
                // 使用默认 TTL
                long defaultTtl = properties.getCache().getDefaultTtl();
                stringOps.set(cacheKey, result, defaultTtl, TimeUnit.SECONDS);
            }

        } catch (Exception e) {
            log.error("Error caching result", e);
            // 缓存失败不影响业务逻辑
        }
    }

    /**
     * 处理 @RedisPut 操作
     *
     * @param metadata 缓存元数据
     * @param context  缓存上下文
     */
    public void handlePut(CacheMetadata metadata, CacheContext context) {
        try {
            Object result = context.getResult();

            // 获取要缓存的值
            Object valueToCache = result;
            if (StringUtils.hasText(metadata.getValue())) {
                valueToCache = expressionEvaluator.evaluateValue(metadata.getValue(), context, result);
            }

            // 检查是否应该缓存空值
            if (valueToCache == null && !metadata.isCacheNull()) {
                log.debug("Skipping null value put for key pattern: {}", metadata.getKey());
                return;
            }

            String cacheKey = buildCacheKey(metadata, context, result);
            long ttl = determineTtl(metadata, valueToCache);

            log.debug("Putting value to cache for key: {}, ttl: {}", cacheKey, ttl);

            Class<?> valueType = metadata.getReturnType() != null ? metadata.getReturnType() : Object.class;
            @SuppressWarnings({"rawtypes", "unchecked"})
            RedisStringOperations<Object> stringOps = (RedisStringOperations) redisUtils
                    .string(metadata.getSerialization(), (Class) valueType);

            if (ttl > 0) {
                stringOps.set(cacheKey, valueToCache, ttl, TimeUnit.SECONDS);
            } else if (ttl == 0) {
                stringOps.set(cacheKey, valueToCache); // 永不过期
            } else {
                // 使用默认 TTL
                long defaultTtl = properties.getCache().getDefaultTtl();
                stringOps.set(cacheKey, valueToCache, defaultTtl, TimeUnit.SECONDS);
            }

        } catch (Exception e) {
            log.error("Error putting value to cache", e);
            // 缓存失败不影响业务逻辑
        }
    }

    /**
     * 处理 @RedisEvict 操作
     *
     * @param metadata 缓存元数据
     * @param context  缓存上下文
     */
    public void handleEvict(CacheMetadata metadata, CacheContext context) {
        try {
            String cacheKey = buildCacheKey(metadata, context, context.getResult());

            if (metadata.isAllEntries()) {
                // 批量删除匹配的键
                log.debug("Evicting all entries matching pattern: {}", cacheKey);
                evictByPattern(cacheKey, metadata.getBatchSize());
            } else {
                // 删除单个键
                log.debug("Evicting single key: {}", cacheKey);
                redisUtils.common().delete(cacheKey);
            }

        } catch (Exception e) {
            log.error("Error evicting cache", e);
            // 缓存清除失败不影响业务逻辑
        }
    }

    /**
     * 构建完整的缓存键
     */
    private String buildCacheKey(CacheMetadata metadata, CacheContext context, Object result) {
        // 1) 求值键表达式
        String evaluatedKey = expressionEvaluator.evaluateKey(metadata.getKey(), context, result);

        // 2) 基础校验
        if (!StringUtils.hasText(evaluatedKey)) {
            throw new IllegalArgumentException("Evaluated cache key is empty for expression: " + metadata.getKey());
        }

        // 3) 规范化：去除由 SpEL 拼接导致的 null 片段与空片段，防止出现 :null 或尾随 null
        String sanitizedKey = Arrays.stream(evaluatedKey.split(":"))
                .filter(StringUtils::hasText)
                .filter(seg -> !"null".equalsIgnoreCase(seg))
                .collect(Collectors.joining(":"));

        if (!StringUtils.hasText(sanitizedKey)) {
            throw new IllegalArgumentException("Sanitized cache key is empty for expression: " + metadata.getKey());
        }

        // 4) 应用前缀：优先使用注解上的 keyPrefix，其次交给 redisUtils 全局前缀
        String overridePrefix = metadata.getKeyPrefix();
        if (StringUtils.hasText(overridePrefix)) {
            return overridePrefix + sanitizedKey;
        }

        return redisUtils.buildKey(sanitizedKey);
    }

    /**
     * 确定 TTL 值
     */
    private long determineTtl(CacheMetadata metadata, Object value) {
        // 如果值为 null，使用空值 TTL
        if (value == null && metadata.getNullTtl() >= 0) {
            return metadata.getNullTtl() > 0 ? metadata.getNullTtl() : properties.getCache().getNullCacheTtl();
        }

        // 使用配置的 TTL
        if (metadata.getTtl() >= 0) {
            return metadata.getTtl();
        }

        // 使用默认 TTL
        return properties.getCache().getDefaultTtl();
    }

    /**
     * 根据模式批量删除缓存
     */
    private void evictByPattern(String pattern, int batchSize) {
        try {
            // 使用 SCAN 增量扫描并分批删除，避免 KEYS 导致的阻塞
            final int effectiveBatch = batchSize > 0 ? batchSize : 1000;
            final java.util.List<String> batch = new java.util.ArrayList<>(effectiveBatch);
            final long[] totalDeleted = new long[1];

            redisUtils.common().scan(pattern, effectiveBatch, key -> {
                batch.add(key);
                if (batch.size() >= effectiveBatch) {
                    Long deleted = redisUtils.common().delete(batch);
                    totalDeleted[0] += deleted != null ? deleted : 0;
                    log.debug("Deleted batch of {} keys", batch.size());
                    batch.clear();
                }
            });

            // 清理残留批次
            if (!batch.isEmpty()) {
                Long deleted = redisUtils.common().delete(batch);
                totalDeleted[0] += deleted != null ? deleted : 0;
                log.debug("Deleted final batch of {} keys", batch.size());
                batch.clear();
            }

            log.debug("Evict by pattern '{}' completed, deleted {} keys", pattern, totalDeleted[0]);
        } catch (Exception e) {
            log.error("Error evicting keys by pattern: {}", pattern, e);
        }
    }
}