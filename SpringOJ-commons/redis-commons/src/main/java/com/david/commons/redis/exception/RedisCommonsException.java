package com.david.commons.redis.exception;

/**
 * Redis Commons 基础异常类
 *
 * @author David
 */
public class RedisCommonsException extends RuntimeException {

    private final String errorCode;
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

    public RedisCommonsException(String errorCode, String message, Throwable cause, Object... args) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = args;
    }

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 获取错误参数
     *
     * @return 错误参数
     */
    public Object[] getArgs() {
        return args;
    }
}