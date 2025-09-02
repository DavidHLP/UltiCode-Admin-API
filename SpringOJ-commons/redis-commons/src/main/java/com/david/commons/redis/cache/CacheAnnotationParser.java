package com.david.commons.redis.cache;

import com.david.commons.redis.cache.annotation.RedisCacheable;
import com.david.commons.redis.cache.annotation.RedisEvict;
import com.david.commons.redis.cache.annotation.RedisPut;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 缓存注解解析器
 * <p>
 * 负责解析方法上的缓存注解，并转换为统一的缓存元数据对象。
 * </p>
 *
 * @author David
 */
public class CacheAnnotationParser {

    /**
     * 解析方法上的缓存注解
     *
     * @param method 目标方法
     * @return 缓存元数据列表，一个方法可能有多个缓存操作
     */
    public List<CacheMetadata> parseCacheAnnotations(Method method) {
        List<CacheMetadata> metadataList = new ArrayList<>();

        // 解析 @RedisCacheable 注解
        RedisCacheable cacheable = method.getAnnotation(RedisCacheable.class);
        if (cacheable != null) {
            Class<?> resolvedType = (cacheable.type() == Void.class || cacheable.type() == void.class)
                    ? method.getReturnType()
                    : cacheable.type();
            if (resolvedType == void.class || resolvedType == Void.class) {
                resolvedType = Object.class;
            }

            CacheMetadata metadata = CacheMetadata.builder()
                    .operation(CacheOperation.CACHEABLE)
                    .method(method)
                    .key(cacheable.key())
                    .keyPrefix(cacheable.keyPrefix())
                    .condition(cacheable.condition())
                    .unless(cacheable.unless())
                    .ttl(cacheable.ttl())
                    .serialization(cacheable.serialization())
                    .sync(cacheable.sync())
                    .cacheNull(cacheable.cacheNull())
                    .nullTtl(cacheable.nullTtl())
                    .returnType(resolvedType)
                    .build();
            metadataList.add(metadata);
        }

        // 解析 @RedisEvict 注解
        RedisEvict evict = method.getAnnotation(RedisEvict.class);
        if (evict != null) {
            String[] keys = evict.keys();
            List<String> cleaned = new ArrayList<>();
            if (keys != null && keys.length > 0) {
                cleaned = Arrays.stream(keys)
                        .filter(StringUtils::hasText)
                        .toList();
            }

            if (!cleaned.isEmpty()) {
                for (String k : cleaned) {
                    CacheMetadata metadata = CacheMetadata.builder()
                            .operation(CacheOperation.EVICT)
                            .method(method)
                            .key(k)
                            .keyPrefix(evict.keyPrefix())
                            .condition(evict.condition())
                            .sync(evict.sync())
                            .allEntries(evict.allEntries())
                            .beforeInvocation(evict.beforeInvocation())
                            .batchSize(evict.batchSize())
                            .build();
                    metadataList.add(metadata);
                }
            } else if (StringUtils.hasText(evict.key())) {
                CacheMetadata metadata = CacheMetadata.builder()
                        .operation(CacheOperation.EVICT)
                        .method(method)
                        .key(evict.key())
                        .keyPrefix(evict.keyPrefix())
                        .condition(evict.condition())
                        .sync(evict.sync())
                        .allEntries(evict.allEntries())
                        .beforeInvocation(evict.beforeInvocation())
                        .batchSize(evict.batchSize())
                        .build();
                metadataList.add(metadata);
            } else {
                throw new IllegalArgumentException("@RedisEvict must specify 'key' or non-empty 'keys'");
            }
        }

        // 解析 @RedisPut 注解
        RedisPut put = method.getAnnotation(RedisPut.class);
        if (put != null) {
            Class<?> resolvedType = (put.type() == Void.class || put.type() == void.class)
                    ? method.getReturnType()
                    : put.type();
            if (resolvedType == void.class || resolvedType == Void.class) {
                resolvedType = Object.class;
            }

            CacheMetadata metadata = CacheMetadata.builder()
                    .operation(CacheOperation.PUT)
                    .method(method)
                    .key(put.key())
                    .keyPrefix(put.keyPrefix())
                    .condition(put.condition())
                    .unless(put.unless())
                    .ttl(put.ttl())
                    .serialization(put.serialization())
                    .sync(put.sync())
                    .cacheNull(put.cacheNull())
                    .nullTtl(put.nullTtl())
                    .value(put.value())
                    .returnType(resolvedType)
                    .build();
            metadataList.add(metadata);
        }

        return metadataList;
    }

    /**
     * 检查方法是否有缓存注解
     *
     * @param method 目标方法
     * @return 是否有缓存注解
     */
    public boolean hasCacheAnnotation(Method method) {
        return method.isAnnotationPresent(RedisCacheable.class) ||
                method.isAnnotationPresent(RedisEvict.class) ||
                method.isAnnotationPresent(RedisPut.class);
    }

    /**
     * 获取方法的缓存操作类型
     *
     * @param method 目标方法
     * @return 缓存操作类型列表
     */
    public List<CacheOperation> getCacheOperations(Method method) {
        List<CacheOperation> operations = new ArrayList<>();

        if (method.isAnnotationPresent(RedisCacheable.class)) {
            operations.add(CacheOperation.CACHEABLE);
        }
        if (method.isAnnotationPresent(RedisEvict.class)) {
            operations.add(CacheOperation.EVICT);
        }
        if (method.isAnnotationPresent(RedisPut.class)) {
            operations.add(CacheOperation.PUT);
        }

        return operations;
    }
}