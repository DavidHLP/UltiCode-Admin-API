package com.david.exception;

import com.david.utils.enums.ResponseCode;

/** 业务异常，默认使用 {@link ResponseCode#BUSINESS_ERROR} */
public class BizException extends BaseException {

    public BizException(String message) {
        super(ResponseCode.BUSINESS_ERROR, message);
    }

    public BizException(Integer code, String message) {
        super(code, message);
    }

    public BizException(ResponseCode responseCode) {
        super(responseCode);
    }

    public BizException(ResponseCode responseCode, String message) {
        super(responseCode, message);
    }

    public static BizException of(Integer code, String message) {
        return new BizException(code, message);
    }

    public static BizException of(ResponseCode responseCode) {
        return new BizException(responseCode);
    }
}
