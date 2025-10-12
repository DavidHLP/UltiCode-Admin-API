package com.david.exception;

/**
 * 标准错误码接口，便于不同模块定义各自的错误枚举并统一处理。
 */
public interface IErrorCode {
    Integer getCode();
    String getMessage();
}

