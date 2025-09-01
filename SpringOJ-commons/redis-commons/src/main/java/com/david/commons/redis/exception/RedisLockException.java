package com.david.commons.redis.exception;

/**
 * Redis 分布式锁异常
 *
 * @author David
 */
public class RedisLockException extends RedisCommonsException {

    public RedisLockException(String message) {
        super(message);
    }

    public RedisLockException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedisLockException(String errorCode, String message) {
        super(errorCode, message);
    }

    public RedisLockException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public RedisLockException(String errorCode, String message, Object... args) {
        super(errorCode, message, args);
    }

    public RedisLockException(String errorCode, String message, Throwable cause, Object... args) {
        super(errorCode, message, cause, args);
    }
}
