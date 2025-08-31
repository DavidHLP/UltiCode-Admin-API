package com.david.commons.redis.exception;

/**
 * Redis 缓存异常
 *
 * @author David
 */
public class RedisCacheException extends RedisCommonsException {

    public RedisCacheException(String message) {
        super(message);
    }

    public RedisCacheException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedisCacheException(String errorCode, String message) {
        super(errorCode, message);
    }

    public RedisCacheException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public RedisCacheException(String errorCode, String message, Object... args) {
        super(errorCode, message, args);
    }

    public RedisCacheException(String errorCode, String message, Throwable cause, Object... args) {
        super(errorCode, message, cause, args);
    }
}