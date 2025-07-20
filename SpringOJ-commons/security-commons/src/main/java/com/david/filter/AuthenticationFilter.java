package com.david.filter;

import com.david.entity.user.AuthUser;
import com.david.utils.UserContextUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 下游服务权限过滤器
 * 将 Gateway 传递的用户信息转换为 Spring Security 认证上下文
 * 只在传统 Servlet 环境下加载，避免在 WebFlux Gateway 中冲突
 */
@Slf4j
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class AuthenticationFilter extends OncePerRequestFilter {

    private final String[] AUTH_WHITELIST = {
            "/actuator/**",
            "/favicon.ico",
            "/error",
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/auth/validate",
            "/api/auth/send-code"
    };

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (Arrays.stream(AUTH_WHITELIST).anyMatch(path -> request.getRequestURI().startsWith(path))) {
            filterChain.doFilter(request, response);
            return;
        }

        // 尝试从请求头构建 AuthUser 对象
        Optional<AuthUser> authUserOpt = UserContextUtil.getCurrentAuthUser(request);

        if (authUserOpt.isPresent()) {
            AuthUser authUser = authUserOpt.get();

            // 创建 Authentication 对象，使用 AuthUser 作为 principal
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    authUser, // principal - 使用完整的 AuthUser 对象
                    null, // credentials (不需要密码)
                    authUser.getAuthorities() // authorities - 使用 AuthUser 的 getAuthorities 方法
            );

            // 设置到 Security 上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("设置用户认证上下文: {} (ID: {}), 权限: {}",
                    authUser.getUsername(), authUser.getUserId(), authUser.getAuthorities());
        } else {
            throw new RuntimeException("用户信息未找到");
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 清理上下文
            SecurityContextHolder.clearContext();
        }
    }

}
