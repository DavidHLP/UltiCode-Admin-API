package com.david.commons.redis.cache.aspect.operator;

import com.david.commons.redis.RedisUtils;
import com.david.commons.redis.cache.CacheContext;
import com.david.commons.redis.cache.CacheMetadata;
import com.david.commons.redis.operations.RedisStringOperations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * 缓存读取操作器
 *
 * <p>负责从Redis读取缓存数据，处理序列化和类型转换
 *
 * @author David
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CacheReadOperator {

    private final RedisUtils redisUtils;
    private final CacheKeyBuilder keyBuilder;

    /**
     * 从缓存中读取值
     *
     * @param metadata 缓存元数据
     * @param context 缓存上下文
     * @return 缓存值，未命中返回null
     */
    public Object readCache(CacheMetadata metadata, CacheContext context) {
        try {
            String cacheKey = keyBuilder.buildCacheKey(metadata, context, null);
            log.debug("Reading cache for key: {}", cacheKey);

            Class<?> valueType = metadata.returnType() != null ? metadata.returnType() : Object.class;
            
            RedisStringOperations stringOps = redisUtils.string().using(metadata.serialization());

            Object cachedValue = stringOps.get(cacheKey, valueType.asSubclass(Object.class));

            if (cachedValue != null) {
                log.debug("Cache hit for key: {}", cacheKey);
                return cachedValue;
            } else {
                log.debug("Cache miss for key: {}", cacheKey);
                return null;
            }
        } catch (Exception e) {
            log.error("Error reading cache for key pattern: {}", metadata.key(), e);
            return null; // 缓存读取失败时返回 null，让方法正常执行
        }
    }

    /**
     * 检查缓存是否存在
     *
     * @param metadata 缓存元数据
     * @param context 缓存上下文
     * @return 是否存在
     */
    public boolean exists(CacheMetadata metadata, CacheContext context) {
        try {
            String cacheKey = keyBuilder.buildCacheKey(metadata, context, null);
            return redisUtils.common().hasKey(cacheKey);
        } catch (Exception e) {
            log.error("Error checking cache existence for key pattern: {}", metadata.key(), e);
            return false;
        }
    }
}
