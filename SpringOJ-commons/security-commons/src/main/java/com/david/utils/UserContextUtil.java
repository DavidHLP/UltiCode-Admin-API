package com.david.utils;

import com.david.entity.role.Role;
import com.david.entity.user.AuthUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
public class UserContextUtil {

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_NAME_HEADER = "X-User-Name";
    public static final String USER_EMAIL_HEADER = "X-User-Email";
    public static final String USER_ROLES_HEADER = "X-User-Roles";

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

    public static Optional<String> getCurrentUsername(ServerWebExchange exchange) {
        String username = exchange.getRequest().getHeaders().getFirst(USER_NAME_HEADER);
        return Optional.ofNullable(username);
    }

    public static Optional<String> getCurrentUserEmail(ServerWebExchange exchange) {
        String email = exchange.getRequest().getHeaders().getFirst(USER_EMAIL_HEADER);
        return Optional.ofNullable(email);
    }

    public static Optional<String> getCurrentUserRole(ServerWebExchange exchange) {
        String role = exchange.getRequest().getHeaders().getFirst(USER_ROLES_HEADER);
        return Optional.ofNullable(role);
    }

    public static boolean hasRole(ServerWebExchange exchange, String role) {
        return getCurrentUserRole(exchange)
                .map(userRole -> userRole.equals(role))
                .orElse(false);
    }

    public static boolean isAdmin(ServerWebExchange exchange) {
        return hasRole(exchange, "ADMIN");
    }

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

    public static Optional<String> getCurrentUsername(HttpServletRequest request) {
        String username = request.getHeader(USER_NAME_HEADER);
        return Optional.ofNullable(username);
    }

    public static Optional<String> getCurrentUserEmail(HttpServletRequest request) {
        String email = request.getHeader(USER_EMAIL_HEADER);
        return Optional.ofNullable(email);
    }

    public static Optional<String> getCurrentUserRole(HttpServletRequest request) {
        String role = request.getHeader(USER_ROLES_HEADER);
        return Optional.ofNullable(role);
    }

    public static boolean hasRole(HttpServletRequest request, String role) {
        return getCurrentUserRole(request)
                .map(userRole -> userRole.equals(role))
                .orElse(false);
    }

    public static boolean isAdmin(HttpServletRequest request) {
        return hasRole(request, "ADMIN");
    }

    public static Mono<Authentication> getCurrentAuthentication() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .doOnNext(auth -> log.debug("获取当前认证信息: {}", auth.getName()))
                .doOnError(error -> log.warn("获取认证信息失败: {}", error.getMessage()));
    }

    public static Mono<String> getCurrentUsernameFromContext() {
        return getCurrentAuthentication()
                .map(Authentication::getName);
    }

    public static boolean isCurrentUser(ServerWebExchange exchange, Long userId) {
        return getCurrentUserId(exchange)
                .map(currentUserId -> currentUserId.equals(userId))
                .orElse(false);
    }

    public static boolean isCurrentUser(HttpServletRequest request, Long userId) {
        return getCurrentUserId(request)
                .map(currentUserId -> currentUserId.equals(userId))
                .orElse(false);
    }

    public static Optional<AuthUser> getCurrentAuthUser(HttpServletRequest request) {
        Optional<Long> userId = getCurrentUserId(request);
        Optional<String> username = getCurrentUsername(request);

        if (userId.isPresent() && username.isPresent()) {
            String roleName = getCurrentUserRole(request).orElse("USER");
            Role role = Role.builder()
                    .roleName(roleName)
                    .status(1)
                    .build();

            AuthUser authUser = AuthUser.builder()
                    .userId(userId.get())
                    .username(username.get())
                    .email(getCurrentUserEmail(request).orElse(null))
                    .status(1)
                    .role(role)
                    .build();

            return Optional.of(authUser);
        }
        return Optional.empty();
    }

    public static Optional<AuthUser> getCurrentAuthUser(ServerWebExchange exchange) {
        Optional<Long> userId = getCurrentUserId(exchange);
        Optional<String> username = getCurrentUsername(exchange);

        if (userId.isPresent() && username.isPresent()) {
            String roleName = getCurrentUserRole(exchange).orElse("USER");
            Role role = Role.builder()
                    .roleName(roleName)
                    .status(1)
                    .build();

            AuthUser authUser = AuthUser.builder()
                    .userId(userId.get())
                    .username(username.get())
                    .email(getCurrentUserEmail(exchange).orElse(null))
                    .status(1)
                    .role(role)
                    .build();

            return Optional.of(authUser);
        }
        return Optional.empty();
    }
}