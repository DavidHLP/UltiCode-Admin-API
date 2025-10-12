package com.david.utils;

import com.david.exception.BaseException;
import com.david.exception.IErrorCode;
import com.david.utils.enums.ResponseCode;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/** 统一响应结果封装，企业级字段更完善且保持兼容 */
@Setter
@Getter
public class ResponseResult<T> {
    private Integer code;
    private String message;
    private T data;
    private String timestamp;
    private Boolean success;
    private String traceId;

    public ResponseResult() {}

    public ResponseResult(Integer code, String message, T data, String timestamp) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
        this.success = code != null && code.equals(ResponseCode.RC200.getCode());
    }

    public static ResponseResult<Void> success() {
        return new ResponseResult<>(
                ResponseCode.RC200.getCode(),
                ResponseCode.RC200.getMessage(),
                null,
                now());
    }

    public static <T> ResponseResult<T> success(T data) {
        return new ResponseResult<>(ResponseCode.RC200.getCode(), ResponseCode.RC200.getMessage(), data, now());
    }

    public static <T> ResponseResult<T> success(String message, T data) {
        return new ResponseResult<>(ResponseCode.RC200.getCode(), message, data, now());
    }

    public static ResponseResult<Void> success(String message) {
        return new ResponseResult<>(ResponseCode.RC200.getCode(), message, null, now());
    }

    public static <T> ResponseResult<T> fail(Integer code, String message) {
        ResponseResult<T> r = new ResponseResult<>(code, message, null, now());
        r.setSuccess(false);
        return r;
    }

    public static <T> ResponseResult<T> fail(Integer code) {
        ResponseCode responseCode = ResponseCode.valueOf(code);
        if (responseCode == null) {
            return fail(code, "未知错误");
        }
        return fail(responseCode);
    }

    public static <T> ResponseResult<T> fail(IErrorCode errorCode) {
        ResponseResult<T> r = new ResponseResult<>(errorCode.getCode(), errorCode.getMessage(), null, now());
        r.setSuccess(false);
        return r;
    }

    public static <T> ResponseResult<T> fail(IErrorCode errorCode, String customMessage) {
        ResponseResult<T> r = new ResponseResult<>(errorCode.getCode(), customMessage, null, now());
        r.setSuccess(false);
        return r;
    }

    public static <T> ResponseResult<T> fromException(Throwable t) {
        if (t instanceof BaseException be) {
            return fail(be.getCode(), t.getMessage());
        }
        return fail(ResponseCode.RC500, t.getMessage());
    }

    private static String now() {
        return OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    public ResponseResult<T> withTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }
}
