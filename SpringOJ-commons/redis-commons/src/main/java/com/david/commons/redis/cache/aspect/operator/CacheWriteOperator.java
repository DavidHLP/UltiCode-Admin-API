package com.david.commons.redis.cache.aspect.operator;

import com.david.commons.redis.RedisUtils;
import com.david.commons.redis.cache.CacheContext;
import com.david.commons.redis.cache.CacheMetadata;
import com.david.commons.redis.cache.expression.CacheExpressionEvaluator;
import com.david.commons.redis.operations.RedisStringOperations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 缓存写入操作器
 *
 * <p>负责将数据写入Redis缓存，支持TTL和空值缓存策略
 *
 * @author David
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CacheWriteOperator {

    private final RedisUtils redisUtils;
    private final CacheKeyBuilder keyBuilder;
    private final CacheTtlManager ttlManager;
    private final CacheExpressionEvaluator expressionEvaluator;

    /**
     * 写入缓存值（用于@RedisCacheable操作）
     *
     * @param metadata 缓存元数据
     * @param context 缓存上下文
     */
    public void writeCacheableResult(CacheMetadata metadata, CacheContext context) {
        try {
            Object result = context.getResult();

            // 检查是否应该缓存空值
            if (result == null && !metadata.cacheNull()) {
                log.debug("跳过缓存空值，键模式: {}", metadata.key());
                return;
            }

            String cacheKey = keyBuilder.buildCacheKey(metadata, context, result);
            long ttl = ttlManager.determineTtl(metadata, result);

            log.debug("缓存结果，键: {}, TTL: {}秒", cacheKey, ttl);

            writeToCache(metadata, cacheKey, result, ttl);

        } catch (Exception e) {
            log.error("缓存结果时发生错误，键模式: {}", metadata.key(), e);
            // 缓存失败不影响业务逻辑
        }
    }

    /**
     * 写入缓存值（用于@RedisPut操作）
     *
     * @param metadata 缓存元数据
     * @param context 缓存上下文
     */
    public void writePutValue(CacheMetadata metadata, CacheContext context) {
        try {
            Object result = context.getResult();

            // 获取要缓存的值
            Object valueToCache = result;
            if (StringUtils.hasText(metadata.value())) {
                valueToCache = expressionEvaluator.evaluateValue(metadata.value(), context, result);
            }

            // 检查是否应该缓存空值
            if (valueToCache == null && !metadata.cacheNull()) {
                log.debug("跳过写入空值，键模式: {}", metadata.key());
                return;
            }

            String cacheKey = keyBuilder.buildCacheKey(metadata, context, result);
            long ttl = ttlManager.determineTtl(metadata, valueToCache);

            log.debug("写入缓存值，键: {}, TTL: {}秒", cacheKey, ttl);

            writeToCache(metadata, cacheKey, valueToCache, ttl);

        } catch (Exception e) {
            log.error("写入缓存值时发生错误，键模式: {}", metadata.key(), e);
            // 缓存失败不影响业务逻辑
        }
    }

    /** 底层写入缓存方法 */
    private void writeToCache(CacheMetadata metadata, String cacheKey, Object value, long ttl) {

        RedisStringOperations stringOps = redisUtils.string().using(metadata.serialization());

        if (ttl > 0) {
            stringOps.set(cacheKey, value, ttl, TimeUnit.SECONDS);
            log.debug("已缓存，TTL: {} 秒", ttl);
        } else if (ttl == 0) {
            stringOps.set(cacheKey, value); // 永不过期
            log.debug("已缓存，永不过期");
        } else {
            // 使用默认 TTL
            long defaultTtl = ttlManager.getDefaultTtl();
            stringOps.set(cacheKey, value, defaultTtl, TimeUnit.SECONDS);
            log.debug("已缓存，使用默认TTL: {} 秒", defaultTtl);
        }
    }
}
