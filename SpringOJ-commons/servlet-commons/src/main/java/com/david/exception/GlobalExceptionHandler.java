package com.david.exception;

import com.david.utils.ResponseResult;
import com.david.utils.enums.ResponseCode;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/** 全局异常处理器：将各类异常统一转换为 ResponseResult */
@ConditionalOnClass(name = "jakarta.servlet.ServletException")
@RestControllerAdvice(basePackages = "com.david")
public class GlobalExceptionHandler {

    /** 业务异常 */
    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseResult<Void> handleBizException(BizException e) {
        return ResponseResult.fail(e.getCode(), e.getMessage());
    }

    /** JSON/请求体不可读 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseResult<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return ResponseResult.fail(
                ResponseCode.RC400.getCode(), "请求体解析失败: " + e.getMostSpecificCause().getMessage());
    }

    /** 参数校验 - @Valid 对象字段 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseResult<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        return ResponseResult.fail(ResponseCode.RC400.getCode(), msg);
    }

    /** 参数校验 - @Validated 单个参数 */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseResult<Void> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(this::formatViolation)
                .collect(Collectors.joining("; "));
        return ResponseResult.fail(ResponseCode.RC400.getCode(), msg);
    }

    /** 绑定异常（如 GET 场景的参数绑定失败） */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseResult<Void> handleBindException(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        return ResponseResult.fail(ResponseCode.RC400.getCode(), msg);
    }

    /** 参数类型不匹配（例如枚举/数字解析失败） */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseResult<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String name = e.getName();
        String required = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知类型";
        String value = e.getValue() != null ? e.getValue().toString() : "null";
        String msg = String.format("参数类型错误: %s 需要 %s, 实际值=%s", name, required, value);
        return ResponseResult.fail(ResponseCode.RC400.getCode(), msg);
    }

    /** 缺少必须的请求参数 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseResult<Void> handleMissingParam(MissingServletRequestParameterException e) {
        String msg = String.format("缺少必要参数: %s(%s)", e.getParameterName(), e.getParameterType());
        return ResponseResult.fail(ResponseCode.RC400.getCode(), msg);
    }

    /** 兜底异常 */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseResult<Void> handleException(Exception e) {
        return ResponseResult.fail(ResponseCode.RC500.getCode(), e.getMessage());
    }

    private String formatFieldError(FieldError fe) {
        return fe.getField()
                + ": "
                + (fe.getDefaultMessage() == null ? "参数不合法" : fe.getDefaultMessage());
    }

    private String formatViolation(ConstraintViolation<?> v) {
        return v.getPropertyPath() + ": " + (v.getMessage() == null ? "参数不合法" : v.getMessage());
    }
}
