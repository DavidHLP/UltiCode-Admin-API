package com.david.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign请求拦截器
 * 自动将当前请求的权限信息传递到下游服务
 * 不依赖security-commons，保持模块独立性
 */
@Slf4j
public class FeignAuthInterceptor implements RequestInterceptor {

    // 用户信息请求头常量（独立定义，不依赖security-commons）
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_NAME_HEADER = "X-User-Name";
    public static final String USER_EMAIL_HEADER = "X-User-Email";
    public static final String USER_ROLES_HEADER = "X-User-Roles";

    @Override
    public void apply(RequestTemplate template) {
        // 首先尝试从HTTP请求上下文获取权限信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String userId = request.getHeader(USER_ID_HEADER);
        String userName = request.getHeader(USER_NAME_HEADER);
        String userEmail = request.getHeader(USER_EMAIL_HEADER);
        String userRoles = request.getHeader(USER_ROLES_HEADER);

        log.debug("从HTTP请求上下文获取权限信息: userId={}, userName={}", userId, userName);

        // 添加权限头到Feign请求中
        if (userId != null) {
            template.header(USER_ID_HEADER, userId);
            log.debug("Feign请求添加用户ID头: {}", userId);
        }
        if (userName != null) {
            template.header(USER_NAME_HEADER, userName);
            log.debug("Feign请求添加用户名头: {}", userName);
        }
        if (userEmail != null) {
            template.header(USER_EMAIL_HEADER, userEmail);
            log.debug("Feign请求添加用户邮箱头: {}", userEmail);
        }
        if (userRoles != null) {
            template.header(USER_ROLES_HEADER, userRoles);
            log.debug("Feign请求添加用户角色头: {}", userRoles);
        }

        if (userId != null || userName != null) {
            log.debug("Feign请求成功传递权限信息到: {}", template.url());
        }
    }
}