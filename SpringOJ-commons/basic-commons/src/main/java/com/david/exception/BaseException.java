package com.david.exception;

import lombok.Getter;

/** 统一运行时异常基类，携带错误码与可选占位参数。 */
@Getter
public class BaseException extends RuntimeException {
    private final Integer code;
    private final transient Object[] args;

    public BaseException(IErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.args = null;
    }

    public BaseException(IErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
        this.args = null;
    }

    public BaseException(IErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.code = errorCode.getCode();
        this.args = null;
    }

    public BaseException(Integer code, String message) {
        super(message);
        this.code = code;
        this.args = null;
    }

    public BaseException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.args = null;
    }

    public BaseException(IErrorCode errorCode, Object... args) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.args = args;
    }
}
