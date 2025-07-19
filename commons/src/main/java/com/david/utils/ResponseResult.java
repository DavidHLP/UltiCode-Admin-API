package com.david.utils;

import com.david.utils.enums.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 响应结果封装，遵循统一响应格式规范
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class ResponseResult<T> {
    private final Integer code;
    private final String message;
    private final T data;

    public static <Void> ResponseResult<Void> success() {
        return ResponseResult.<Void>builder().code(ResponseCode.RC200.getCode()).data(null).message(ResponseCode.RC200.getMessage()).build();
    }
    public static <T> ResponseResult<T> success(T data) {
        return ResponseResult.<T>builder().code(ResponseCode.RC200.getCode()).data(data).message(ResponseCode.RC200.getMessage()).build();
    }

    public static <T> ResponseResult<T> success(T data , String message) {
        return ResponseResult.<T>builder().code(ResponseCode.RC200.getCode()).data(data).message(message).build();
    }

    public static <Void> ResponseResult<Void> successNoDataHasMessage(String message) {
        return ResponseResult.<Void>builder().code(ResponseCode.RC200.getCode()).data(null).message(message).build();
    }

    public static <T> ResponseResult<T> fail(Integer code, String message) {
        return ResponseResult.<T>builder().code(code).data(null).message(message).build();
    }

    public static <T> ResponseResult<T> fail(Integer code) {
        ResponseCode responseCode = ResponseCode.valueOf(code);
        return ResponseResult.<T>builder().code(responseCode.getCode()).data(null).message(responseCode.getMessage()).build();
    }

}