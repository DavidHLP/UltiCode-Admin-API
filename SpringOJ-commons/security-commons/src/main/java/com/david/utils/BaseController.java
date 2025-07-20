package com.david.utils;

import com.david.entity.user.AuthUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

public class BaseController {

    /**
     * 获取当前登录用户信息
     * 优先从 Spring Security 上下文获取，如果没有则从请求头获取
     */
    protected AuthUser getCurrentUser() {
        // 首先尝试从 Security 上下文获取
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof AuthUser) {
            return (AuthUser) authentication.getPrincipal();
        }

        // 如果 Security 上下文没有，则从请求头获取
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            Optional<AuthUser> authUser = UserContextUtil.getCurrentAuthUser(request);
            if (authUser.isPresent()) {
                return authUser.get();
            }
        }

        throw new RuntimeException("用户信息未找到，请检查认证状态");
    }

    /**
     * 获取当前用户ID
     */
    protected Long getCurrentUserId() {
        return getCurrentUser().getUserId();
    }

    /**
     * 获取当前用户名
     */
    protected String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }

    /**
     * 获取当前用户邮箱
     */
    protected String getCurrentUserEmail() {
        return getCurrentUser().getEmail();
    }

    /**
     * 检查当前用户是否具有指定角色
     */
    protected boolean hasRole(String roleName) {
        return getCurrentUser().getRole() != null &&
               roleName.equals(getCurrentUser().getRole().getRoleName());
    }

    /**
     * 检查当前用户是否为管理员
     */
    protected boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * 检查是否为当前用户本人（通过用户ID比较）
     */
    protected boolean isCurrentUser(Long userId) {
        return getCurrentUserId().equals(userId);
    }

    /**
     * 获取当前 HTTP 请求对象
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
