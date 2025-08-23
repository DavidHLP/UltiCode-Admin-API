package com.david.utils;

import com.david.utils.enums.ResponseCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 响应结果封装，遵循统一响应格式规范
 */
@Setter
@Getter
public class ResponseResult<T> {
    private Integer code;
    private String message;
    private T data;
    private String timestamp;

    public ResponseResult() {}

    public ResponseResult(Integer code, String message, T data, String timestamp) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

    public static ResponseResult<Void> success() {
        return new ResponseResult<>(ResponseCode.RC200.getCode(), ResponseCode.RC200.getMessage(), null, getCurrentTimestamp());
    }

    public static <T> ResponseResult<T> success(String message, T data) {
        return new ResponseResult<>(ResponseCode.RC200.getCode(), message, data, getCurrentTimestamp());
    }

    public static ResponseResult<Void> success(String message) {
        return new ResponseResult<>(ResponseCode.RC200.getCode(), message, null, getCurrentTimestamp());
    }

    public static <T> ResponseResult<T> fail(Integer code, String message) {
        return new ResponseResult<>(code, message, null, getCurrentTimestamp());
    }

    public static <T> ResponseResult<T> fail(Integer code) {
        ResponseCode responseCode = ResponseCode.valueOf(code);
        if (responseCode == null) {
            return new ResponseResult<>(code, "未知错误", null, getCurrentTimestamp());
        }
        return new ResponseResult<>(responseCode.getCode(), responseCode.getMessage(), null, getCurrentTimestamp());
    }

    public static <T> ResponseResult<T> fail(ResponseCode responseCode) {
        return new ResponseResult<>(responseCode.getCode(), responseCode.getMessage(), null, getCurrentTimestamp());
    }

    public static <T> ResponseResult<T> fail(ResponseCode responseCode, String customMessage) {
        return new ResponseResult<>(responseCode.getCode(), customMessage, null, getCurrentTimestamp());
    }

    private static String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}