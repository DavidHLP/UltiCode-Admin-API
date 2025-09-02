package com.david.commons.redis.cache.expression;

import com.david.commons.redis.exception.RedisCommonsException;

/**
 * 缓存表达式异常
 * <p>
 * 当 SpEL 表达式解析或求值失败时抛出此异常。
 * </p>
 *
 * @author David
 */
public class CacheExpressionException extends RedisCommonsException {

    public CacheExpressionException(String message) {
        super(message);
    }

    public CacheExpressionException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheExpressionException(String message, String errorCode, Object... args) {
        super(errorCode, message, args);
    }

    public CacheExpressionException(String message, Throwable cause, String errorCode, Object... args) {
        super(errorCode, message, cause, args);
    }
}