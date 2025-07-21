package com.david.config;

import com.david.utils.AsyncContextUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

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
        
        String userId = null, userName = null, userEmail = null, userRoles = null;
        
        if (attributes != null) {
            // 从HTTP请求上下文获取权限信息
            var request = attributes.getRequest();
            userId = request.getHeader(USER_ID_HEADER);
            userName = request.getHeader(USER_NAME_HEADER);
            userEmail = request.getHeader(USER_EMAIL_HEADER);
            userRoles = request.getHeader(USER_ROLES_HEADER);
            
            log.debug("从HTTP请求上下文获取权限信息: userId={}, userName={}", userId, userName);
        } else {
            // 如果无法获取HTTP请求上下文，尝试从异步上下文获取
            Map<String, String> asyncContext = AsyncContextUtil.getAsyncAuthContext();
            if (asyncContext != null) {
                userId = asyncContext.get(USER_ID_HEADER);
                userName = asyncContext.get(USER_NAME_HEADER);
                userEmail = asyncContext.get(USER_EMAIL_HEADER);
                userRoles = asyncContext.get(USER_ROLES_HEADER);
                
                log.debug("从异步上下文获取权限信息: userId={}, userName={}", userId, userName);
            } else {
                log.warn("无法获取权限上下文（HTTP或异步），Feign调用可能缺少权限信息");
            }
        }
        
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