package com.david.commons.redis.cache.enums;

import lombok.Getter;

/**
 * 缓存操作类型枚举
 *
 * @author David
 */
@Getter
public enum CacheOperation {

    /**
     * 缓存查询操作
     * 对应 @RedisCacheable 注解
     */
    CACHEABLE("cacheable"),

    /**
     * 缓存清除操作
     * 对应 @RedisEvict 注解
     */
    EVICT("evict"),

    /**
     * 缓存更新操作
     * 对应 @RedisPut 注解
     */
    PUT("put");

    private final String operation;

    CacheOperation(String operation) {
        this.operation = operation;
    }

	@Override
    public String toString() {
        return operation;
    }
}