package com.david.auth.security;

import com.david.common.http.ApiError;
import com.david.common.http.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException)
            throws IOException {
        log.warn(
                "访问被拒绝: 请求URI={}, 用户代理={}, 远程地址={}, 错误信息={}",
                request.getRequestURI(),
                request.getHeader("User-Agent"),
                request.getRemoteAddr(),
                accessDeniedException.getMessage());

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json;charset=UTF-8");
        ApiResponse<Void> body =
                ApiResponse.failure(
                        ApiError.of(
                                HttpStatus.FORBIDDEN.value(),
                                HttpStatus.FORBIDDEN.name(),
                                accessDeniedException.getMessage()));
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
