package com.david.admin.config;

import com.david.common.forward.ForwardedAuthenticationToken;
import com.david.common.forward.ForwardedUser;
import com.david.common.forward.ForwardedUserParser;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 将网关转发的用户信息从请求头写入 Spring Security 的上下文。
 */
@Slf4j
public class HeaderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
		    @NonNull  HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
        if (shouldPopulateFromHeaders(currentAuthentication)) {
            Optional<ForwardedUser> forwardedUser = ForwardedUserParser.from(request);
            forwardedUser.ifPresentOrElse(
                    user -> {
                        List<SimpleGrantedAuthority> authorities =
                                user.roles().stream()
                                        .filter(StringUtils::hasText)
                                        .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                                        .map(SimpleGrantedAuthority::new)
                                        .collect(Collectors.toList());
                        ForwardedAuthenticationToken authentication =
                                new ForwardedAuthenticationToken(user, authorities);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.trace(
                                "已从请求头构建用户信息，用户：{}，角色数：{}",
                                user.username(),
                                authorities.size());
                    },
                    () -> log.debug("请求头未包含有效的转发用户信息"));
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldPopulateFromHeaders(Authentication authentication) {
        if (authentication == null) {
            return true;
        }
        if (!authentication.isAuthenticated()) {
            return true;
        }
        if (authentication instanceof AnonymousAuthenticationToken) {
            return true;
        }
        if (authentication instanceof ForwardedAuthenticationToken) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        return !(principal instanceof ForwardedUser);
    }
}
