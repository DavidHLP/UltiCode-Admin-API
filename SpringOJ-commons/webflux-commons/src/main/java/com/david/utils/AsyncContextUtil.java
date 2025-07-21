package com.david.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * 异步任务上下文工具类
 * 用于在异步任务中传递权限信息
 * 不依赖security-commons，保持模块独立性
 */
@Slf4j
public class AsyncContextUtil {

    // 用户信息请求头常量（独立定义，不依赖security-commons）
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_NAME_HEADER = "X-User-Name";
    public static final String USER_EMAIL_HEADER = "X-User-Email";
    public static final String USER_ROLES_HEADER = "X-User-Roles";

    private static final ThreadLocal<Map<String, String>> ASYNC_CONTEXT = new ThreadLocal<>();

    /**
     * 从当前HTTP请求上下文中提取权限信息
     * 在调用异步方法前调用此方法
     */
    public static Map<String, String> captureAuthContext() {
        Map<String, String> authContext = new HashMap<>();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            var request = attributes.getRequest();

            String userId = request.getHeader(USER_ID_HEADER);
            String userName = request.getHeader(USER_NAME_HEADER);
            String userEmail = request.getHeader(USER_EMAIL_HEADER);
            String userRoles = request.getHeader(USER_ROLES_HEADER);

            if (userId != null) {
                authContext.put(USER_ID_HEADER, userId);
            }
            if (userName != null) {
                authContext.put(USER_NAME_HEADER, userName);
            }
            if (userEmail != null) {
                authContext.put(USER_EMAIL_HEADER, userEmail);
            }
            if (userRoles != null) {
                authContext.put(USER_ROLES_HEADER, userRoles);
            }

            log.debug("捕获权限上下文: userId={}, userName={}", userId, userName);
        } else {
            log.warn("无法获取当前请求上下文，权限信息可能缺失");
        }

        return authContext;
    }

    /**
     * 设置异步任务的权限上下文
     * 在异步方法开始时调用
     */
    public static void setAsyncAuthContext(Map<String, String> authContext) {
        if (authContext != null && !authContext.isEmpty()) {
            ASYNC_CONTEXT.set(authContext);
            log.debug("设置异步权限上下文: {}", authContext);
        }
    }

    /**
     * 获取当前异步任务的权限信息
     * 在FeignAuthInterceptor中使用
     */
    public static Map<String, String> getAsyncAuthContext() {
        return ASYNC_CONTEXT.get();
    }

    /**
     * 清理异步任务的权限上下文
     * 在异步方法结束时调用
     */
    public static void clearAsyncAuthContext() {
        ASYNC_CONTEXT.remove();
        log.debug("清理异步权限上下文");
    }

    /**
     * 获取指定权限头的值
     */
    public static String getAsyncAuthHeader(String headerName) {
        Map<String, String> context = getAsyncAuthContext();
        return context != null ? context.get(headerName) : null;
    }
}
