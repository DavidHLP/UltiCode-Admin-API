package com.david.commons.redis.exception;

/**
 * Redis 连接异常
 *
 * @author David
 */
public class RedisConnectionException extends RedisCommonsException {

    public RedisConnectionException(String message) {
        super(message);
    }

    public RedisConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedisConnectionException(String errorCode, String message) {
        super(errorCode, message);
    }

    public RedisConnectionException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public RedisConnectionException(String errorCode, String message, Object... args) {
        super(errorCode, message, args);
    }

    public RedisConnectionException(
            String errorCode, String message, Throwable cause, Object... args) {
        super(errorCode, message, cause, args);
    }
}
