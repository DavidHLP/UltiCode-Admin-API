package com.david.commons.redis.serialization.exceptions;

import com.david.commons.redis.exception.RedisCommonsException;

/**
 * Redis 序列化异常
 *
 * @author David
 */
public class RedisSerializationException extends RedisCommonsException {

    public RedisSerializationException(String message) {
        super(message);
    }

    public RedisSerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedisSerializationException(String errorCode, String message) {
        super(errorCode, message);
    }

    public RedisSerializationException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public RedisSerializationException(String errorCode, String message, Object... args) {
        super(errorCode, message, args);
    }

    public RedisSerializationException(String errorCode, String message, Throwable cause, Object... args) {
        super(errorCode, message, cause, args);
    }
}