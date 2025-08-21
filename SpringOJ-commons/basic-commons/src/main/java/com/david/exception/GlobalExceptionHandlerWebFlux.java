package com.david.exception;

import com.david.utils.ResponseResult;
import com.david.utils.enums.ResponseCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ServerWebInputException;

import java.util.stream.Collectors;

/**
 * WebFlux 全局异常处理器：仅在 Reactive 环境下装配
 */
@ConditionalOnClass(name = "org.springframework.web.server.ServerWebExchange")
@RestControllerAdvice(basePackages = "com.david")
public class GlobalExceptionHandlerWebFlux {

    /**
     * 业务异常
     */
    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseResult<Void> handleBizException(BizException e) {
        return ResponseResult.fail(e.getCode(), e.getMessage());
    }

    /**
     * 方法不允许（405）
     */
    @ExceptionHandler(MethodNotAllowedException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseResult<Void> handleMethodNotAllowed(MethodNotAllowedException e) {
        return ResponseResult.fail(ResponseCode.RC405);
    }

    /**
     * 请求体/参数解析错误（400）
     */
    @ExceptionHandler(ServerWebInputException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseResult<Void> handleServerWebInput(ServerWebInputException e) {
        String msg = e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : e.getReason();
        return ResponseResult.fail(ResponseCode.RC400.getCode(), "请求体解析失败: " + msg);
    }

    /**
     * 参数校验 - 对象字段（@Valid）
     */
    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseResult<Void> handleWebExchangeBindException(WebExchangeBindException e) {
        String msg = e.getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + (fe.getDefaultMessage() == null ? "参数不合法" : fe.getDefaultMessage()))
                .collect(Collectors.joining("; "));
        return ResponseResult.fail(ResponseCode.RC400.getCode(), msg);
    }

    /**
     * 参数校验 - 单参数（@Validated）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseResult<Void> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(this::formatViolation)
                .collect(Collectors.joining("; "));
        return ResponseResult.fail(ResponseCode.RC400.getCode(), msg);
    }

    /**
     * 兜底异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseResult<Void> handleException(Exception e) {
        return ResponseResult.fail(ResponseCode.RC500.getCode(), e.getMessage());
    }

    private String formatViolation(ConstraintViolation<?> v) {
        return v.getPropertyPath() + ": " + (v.getMessage() == null ? "参数不合法" : v.getMessage());
    }
}
