package com.david.judge.exception;

import com.david.common.http.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(
            BusinessException ex, HttpServletResponse response) {
        response.setStatus(ex.getStatus().value());
        return ApiResponse.failure(
                ex.getStatus().value(), "BUSINESS_ERROR", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleUnknownException(
            Exception ex, HttpServletResponse response) {
        log.error("未处理异常", ex);
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ApiResponse.failure(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_ERROR",
                "系统繁忙，请稍后重试");
    }
}
