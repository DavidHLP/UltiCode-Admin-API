package com.david.utils;

import com.david.entity.role.Role;
import com.david.entity.user.AuthUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 统一用户上下文工具类
 * 支持 WebFlux 响应式和传统 Servlet 两种方式获取用户信息
 */
@Slf4j
public class UserContextUtil {

    // 用户信息请求头常量
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_NAME_HEADER = "X-User-Name";
    public static final String USER_EMAIL_HEADER = "X-User-Email";
    public static final String USER_ROLES_HEADER = "X-User-Roles";
    public static final String USER_AUTHORITIES_HEADER = "X-User-Authorities";

    // ==================== WebFlux 响应式方法 ====================

    /**
     * 从 ServerWebExchange 获取用户ID
     */
    public static Optional<Long> getCurrentUserId(ServerWebExchange exchange) {
        try {
            String userIdStr = exchange.getRequest().getHeaders().getFirst(USER_ID_HEADER);
            if (userIdStr != null) {
                return Optional.of(Long.parseLong(userIdStr));
            }
        } catch (NumberFormatException e) {
            log.warn("解析用户ID失败: {}", e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * 从 ServerWebExchange 获取用户名
     */
    public static Optional<String> getCurrentUsername(ServerWebExchange exchange) {
        String username = exchange.getRequest().getHeaders().getFirst(USER_NAME_HEADER);
        return Optional.ofNullable(username);
    }

    /**
     * 从 ServerWebExchange 获取用户邮箱
     */
    public static Optional<String> getCurrentUserEmail(ServerWebExchange exchange) {
        String email = exchange.getRequest().getHeaders().getFirst(USER_EMAIL_HEADER);
        return Optional.ofNullable(email);
    }

    /**
     * 从 ServerWebExchange 获取用户角色
     */
    public static Optional<String> getCurrentUserRole(ServerWebExchange exchange) {
        String role = exchange.getRequest().getHeaders().getFirst(USER_ROLES_HEADER);
        return Optional.ofNullable(role);
    }

    /**
     * 从 ServerWebExchange 获取用户权限
     */
    public static Optional<String> getCurrentUserAuthorities(ServerWebExchange exchange) {
        String authorities = exchange.getRequest().getHeaders().getFirst(USER_AUTHORITIES_HEADER);
        return Optional.ofNullable(authorities);
    }

    /**
     * 检查当前用户是否具有指定角色
     */
    public static boolean hasRole(ServerWebExchange exchange, String role) {
        return getCurrentUserRole(exchange)
                .map(userRole -> userRole.equals(role))
                .orElse(false);
    }

    /**
     * 检查当前用户是否具有管理员权限
     */
    public static boolean isAdmin(ServerWebExchange exchange) {
        return hasRole(exchange, "ADMIN");
    }

    // ==================== 传统 Servlet 方法（用于下游服务） ====================

    /**
     * 从 HttpServletRequest 获取用户ID
     */
    public static Optional<Long> getCurrentUserId(HttpServletRequest request) {
        try {
            String userIdStr = request.getHeader(USER_ID_HEADER);
            if (userIdStr != null) {
                return Optional.of(Long.parseLong(userIdStr));
            }
        } catch (NumberFormatException e) {
            log.warn("解析用户ID失败: {}", e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * 从 HttpServletRequest 获取用户名
     */
    public static Optional<String> getCurrentUsername(HttpServletRequest request) {
        String username = request.getHeader(USER_NAME_HEADER);
        return Optional.ofNullable(username);
    }

    /**
     * 从 HttpServletRequest 获取用户邮箱
     */
    public static Optional<String> getCurrentUserEmail(HttpServletRequest request) {
        String email = request.getHeader(USER_EMAIL_HEADER);
        return Optional.ofNullable(email);
    }

    /**
     * 从 HttpServletRequest 获取用户角色
     */
    public static Optional<String> getCurrentUserRole(HttpServletRequest request) {
        String role = request.getHeader(USER_ROLES_HEADER);
        return Optional.ofNullable(role);
    }

    /**
     * 从 HttpServletRequest 获取用户权限
     */
    public static Optional<String> getCurrentUserAuthorities(HttpServletRequest request) {
        String authorities = request.getHeader(USER_AUTHORITIES_HEADER);
        return Optional.ofNullable(authorities);
    }

    /**
     * 检查当前用户是否具有指定角色（Servlet版本）
     */
    public static boolean hasRole(HttpServletRequest request, String role) {
        return getCurrentUserRole(request)
                .map(userRole -> userRole.equals(role))
                .orElse(false);
    }

    /**
     * 检查当前用户是否具有管理员权限（Servlet版本）
     */
    public static boolean isAdmin(HttpServletRequest request) {
        return hasRole(request, "ADMIN");
    }

    // ==================== 响应式安全上下文方法 ====================

    /**
     * 从响应式安全上下文获取当前认证信息
     */
    public static Mono<Authentication> getCurrentAuthentication() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .doOnNext(auth -> log.debug("获取当前认证信息: {}", auth.getName()))
                .doOnError(error -> log.warn("获取认证信息失败: {}", error.getMessage()));
    }

    /**
     * 从响应式安全上下文获取当前用户名
     */
    public static Mono<String> getCurrentUsernameFromContext() {
        return getCurrentAuthentication()
                .map(Authentication::getName);
    }

    /**
     * 检查当前用户是否为指定用户（通过用户ID）
     */
    public static boolean isCurrentUser(ServerWebExchange exchange, Long userId) {
        return getCurrentUserId(exchange)
                .map(currentUserId -> currentUserId.equals(userId))
                .orElse(false);
    }

    /**
     * 检查当前用户是否为指定用户（Servlet版本）
     */
    public static boolean isCurrentUser(HttpServletRequest request, Long userId) {
        return getCurrentUserId(request)
                .map(currentUserId -> currentUserId.equals(userId))
                .orElse(false);
    }

    // ==================== 便捷方法 ====================

    /**
     * 从 HttpServletRequest 构建完整的用户信息对象
     */
    public static Optional<AuthUser> getCurrentAuthUser(HttpServletRequest request) {
        Optional<Long> userId = getCurrentUserId(request);
        Optional<String> username = getCurrentUsername(request);
        
        if (userId.isPresent() && username.isPresent()) {
            List<String> authorities = parseAuthoritiesList(
                getCurrentUserAuthorities(request).orElse(""));
            
            // 构建 Role 对象
            String roleName = getCurrentUserRole(request).orElse("USER");
            Role role = Role.builder()
                    .roleName(roleName)
                    .status(1)
                    .build();
            
            AuthUser authUser = AuthUser.builder()
                    .userId(userId.get())
                    .username(username.get())
                    .email(getCurrentUserEmail(request).orElse(null))
                    .status(1) // 下游服务认为通过网关的用户都是启用状态
                    .role(role)
                    .authorities(authorities) // 这里是字符串列表，不是GrantedAuthority对象
                    .build();
            
            // 清理权限列表，确保没有空值
            authUser.cleanAuthorities();
            
            return Optional.of(authUser);
        }
        return Optional.empty();
    }

    /**
     * 从 ServerWebExchange 构建完整的用户信息对象
     */
    public static Optional<AuthUser> getCurrentAuthUser(ServerWebExchange exchange) {
        Optional<Long> userId = getCurrentUserId(exchange);
        Optional<String> username = getCurrentUsername(exchange);
        
        if (userId.isPresent() && username.isPresent()) {
            List<String> authorities = parseAuthoritiesList(
                getCurrentUserAuthorities(exchange).orElse(""));
            
            // 构建 Role 对象
            String roleName = getCurrentUserRole(exchange).orElse("USER");
            Role role = Role.builder()
                    .roleName(roleName)
                    .status(1)
                    .build();
            
            AuthUser authUser = AuthUser.builder()
                    .userId(userId.get())
                    .username(username.get())
                    .email(getCurrentUserEmail(exchange).orElse(null))
                    .status(1) // 下游服务认为通过网关的用户都是启用状态
                    .role(role)
                    .authorities(authorities) // 这里是字符串列表，不是GrantedAuthority对象
                    .build();
            
            // 清理权限列表，确保没有空值
            authUser.cleanAuthorities();
            
            return Optional.of(authUser);
        }
        return Optional.empty();
    }

    /**
     * 解析权限字符串为权限列表
     */
    private static List<String> parseAuthoritiesList(String authoritiesStr) {
        if (!StringUtils.hasText(authoritiesStr)) {
            return Collections.emptyList();
        }
        
        // 处理格式如 "[PERMISSION1, PERMISSION2]" 或 "PERMISSION1,PERMISSION2"
        String cleanStr = authoritiesStr.replaceAll("[\\[\\]]", "").trim();
        if (cleanStr.isEmpty()) {
            return Collections.emptyList();
        }
        
        return Arrays.stream(cleanStr.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }
}
