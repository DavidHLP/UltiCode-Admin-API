package com.david.utils;

import com.david.utils.enums.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 响应结果封装，遵循统一响应格式规范
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class ResponseResult<T> {
    private Integer code;
    private String message;
    private T data;
    private String timestamp;

    public static <Void> ResponseResult<Void> success() {
        return ResponseResult.<Void>builder().code(ResponseCode.RC200.getCode()).data(null).message(ResponseCode.RC200.getMessage()).timestamp(getCurrentTimestamp()).build();
    }

    public static <T> ResponseResult<T> success(String message, T data) {
        return ResponseResult.<T>builder().code(ResponseCode.RC200.getCode()).data(data).message(message).timestamp(getCurrentTimestamp()).build();
    }

    public static <Void> ResponseResult<Void> success(String message) {
        return ResponseResult.<Void>builder().code(ResponseCode.RC200.getCode()).data(null).message(message).timestamp(getCurrentTimestamp()).build();
    }

    public static <T> ResponseResult<T> fail(Integer code, String message) {
        return ResponseResult.<T>builder().code(code).data(null).message(message).timestamp(getCurrentTimestamp()).build();
    }

    public static <T> ResponseResult<T> fail(Integer code) {
        ResponseCode responseCode = ResponseCode.valueOf(code);
        return ResponseResult.<T>builder().code(responseCode.getCode()).data(null).message(responseCode.getMessage()).timestamp(getCurrentTimestamp()).build();
    }

    private static String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}