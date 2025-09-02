package com.david.commons.redis.cache.parser;

import com.david.commons.redis.cache.CacheMetadata;
import com.david.commons.redis.cache.annotation.RedisCacheable;
import com.david.commons.redis.cache.annotation.RedisEvict;
import com.david.commons.redis.cache.annotation.RedisPut;
import com.david.commons.redis.cache.enums.CacheOperation;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 缓存注解解析器
 *
 * <p>
 * 负责解析方法上的缓存注解，并转换为统一的缓存元数据对象。
 *
 * @author David
 */
@Component
@RequiredArgsConstructor
public class CacheAnnotationParser {

        private static final Map<Class<? extends Annotation>, Function<Method, List<CacheMetadata>>> ANNOTATION_PARSERS = Map
                        .of(
                                        RedisCacheable.class, CacheAnnotationParser::parseCacheableAnnotation,
                                        RedisEvict.class, CacheAnnotationParser::parseEvictAnnotation,
                                        RedisPut.class, CacheAnnotationParser::parsePutAnnotation);

        /** 解析 @RedisCacheable 注解 */
        private static List<CacheMetadata> parseCacheableAnnotation(Method method) {
                RedisCacheable cacheable = method.getAnnotation(RedisCacheable.class);

                // 解析类型，处理默认类型和void类型
                Class<?> resolvedType = (cacheable.type() == void.class || cacheable.type() == Void.class)
                                ? (method.getReturnType() == void.class
                                                || method.getReturnType() == Void.class
                                                                ? Object.class
                                                                : method.getReturnType())
                                : cacheable.type();

                return List.of(
                                CacheMetadata.builder()
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
                                                .build());
        }

        /** 解析 @RedisEvict 注解 */
        private static List<CacheMetadata> parseEvictAnnotation(Method method) {
                RedisEvict evict = method.getAnnotation(RedisEvict.class);

                // 提取键值列表
                List<String> keys;
                if (evict.keys() != null && evict.keys().length > 0) {
                        keys = Arrays.stream(evict.keys())
                                        .filter(StringUtils::hasText)
                                        .collect(Collectors.toList());
                } else if (StringUtils.hasText(evict.key())) {
                        keys = List.of(evict.key());
                } else {
                        keys = Collections.emptyList();
                }

                // 验证键配置
                if (keys.isEmpty()) {
                        throw new IllegalArgumentException("@RedisEvict 注解必须指定 'key' 或非空的 'keys'");
                }

                return keys.stream()
                                .map(
                                                key -> CacheMetadata.builder()
                                                                .operation(CacheOperation.EVICT)
                                                                .method(method)
                                                                .key(key)
                                                                .keyPrefix(evict.keyPrefix())
                                                                .condition(evict.condition())
                                                                .sync(evict.sync())
                                                                .allEntries(evict.allEntries())
                                                                .beforeInvocation(evict.beforeInvocation())
                                                                .batchSize(evict.batchSize())
                                                                .build())
                                .collect(Collectors.toList());
        }

        /** 解析 @RedisPut 注解 */
        private static List<CacheMetadata> parsePutAnnotation(Method method) {
                RedisPut put = method.getAnnotation(RedisPut.class);

                // 解析类型，处理默认类型和void类型
                Class<?> resolvedType = (put.type() == void.class || put.type() == Void.class)
                                ? (method.getReturnType() == void.class
                                                || method.getReturnType() == Void.class
                                                                ? Object.class
                                                                : method.getReturnType())
                                : put.type();

                return List.of(
                                CacheMetadata.builder()
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
                                                .build());
        }

        /**
         * 解析方法上的缓存注解
         *
         * @param method 目标方法
         * @return 缓存元数据列表，一个方法可能有多个缓存操作
         */
        public List<CacheMetadata> parseCacheAnnotations(Method method) {
                return ANNOTATION_PARSERS.entrySet().stream()
                                .filter(entry -> method.isAnnotationPresent(entry.getKey()))
                                .flatMap(entry -> entry.getValue().apply(method).stream())
                                .collect(Collectors.toList());
        }
}
