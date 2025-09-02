package com.david.commons.redis.cache;

/**
 * 缓存操作类型枚举
 *
 * @author David
 */
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

    public String getOperation() {
        return operation;
    }

    @Override
    public String toString() {
        return operation;
    }
}