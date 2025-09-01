package com.david.commons.redis.exception;

import lombok.Getter;

/**
 * Redis Commons 基础异常类
 *
 * @author David
 */
@Getter
public class RedisCommonsException extends RuntimeException {

    /**
     * -- GETTER -- 获取错误码
     *
     */
    private final String errorCode;

    /**
     * -- GETTER -- 获取错误参数
     *
     */
    private final Object[] args;

    public RedisCommonsException(String message) {
        super(message);
        this.errorCode = null;
        this.args = null;
    }

    public RedisCommonsException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
        this.args = null;
    }

    public RedisCommonsException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.args = null;
    }

    public RedisCommonsException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = null;
    }

    public RedisCommonsException(String errorCode, String message, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
    }

    public RedisCommonsException(
            String errorCode, String message, Throwable cause, Object... args) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = args;
    }
}
