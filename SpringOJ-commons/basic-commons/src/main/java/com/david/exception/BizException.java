package com.david.exception;

import com.david.utils.enums.ResponseCode;

/**
 * 业务异常，携带错误码与消息
 */
public class BizException extends RuntimeException {
    private final Integer code;

    public BizException(String message) {
        super(message);
        this.code = ResponseCode.BUSINESS_ERROR.getCode();
    }

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.code = responseCode.getCode();
    }

    public static BizException of(Integer code, String message) {
        return new BizException(code, message);
    }

    public static BizException of(ResponseCode responseCode) {
        return new BizException(responseCode);
    }

    public Integer getCode() {
        return code;
    }
}
