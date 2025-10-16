package com.david.common.security;

import com.david.common.forward.ForwardedAuthenticationToken;
import com.david.common.forward.ForwardedUser;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/** 下游服务便捷地访问当前转发用户的工具类。 */
public final class ForwardedSecurityContext {

    private ForwardedSecurityContext() {}

    public static Optional<ForwardedUser> currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        if (authentication instanceof ForwardedAuthenticationToken token) {
            return Optional.ofNullable(token.getPrincipal());
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof ForwardedUser forwardedUser) {
            return Optional.of(forwardedUser);
        }
        Object details = authentication.getDetails();
        if (details instanceof ForwardedUser detailsUser) {
            return Optional.of(detailsUser);
        }
        return Optional.empty();
    }

    public static ForwardedUser requireUser() {
        return currentUser().orElseThrow(() -> new IllegalStateException("当前请求缺少已经认证的用户信息"));
    }

    public static Optional<Long> currentUserId() {
        return currentUser().map(ForwardedUser::id);
    }

    public static boolean hasRole(String role) {
        return currentUser().map(user -> user.hasRole(role)).orElse(false);
    }
}
