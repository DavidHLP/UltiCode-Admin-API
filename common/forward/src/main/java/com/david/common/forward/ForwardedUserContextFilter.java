package com.david.common.forward;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 使用网关已认证的用户身份填充 {@link org.springframework.security.core.context.SecurityContext}。
 * 该过滤器用于在微服务间调用时传递用户认证信息
 */
@Slf4j
@Order(ForwardedUserContextFilter.ORDER)
public class ForwardedUserContextFilter extends OncePerRequestFilter {

    /** 过滤器执行顺序，设置为最高优先级+50 */
    public static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 50;

    /**
     * 过滤器核心方法，处理请求中的用户认证信息
     *
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param filterChain 过滤器链
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // 获取当前安全上下文中的认证信息
        Authentication currentAuthentication =
                SecurityContextHolder.getContext().getAuthentication();

        // 如果当前没有认证信息或认证未通过，则尝试从请求头中解析转发的用户信息
        if (shouldPopulateFromHeaders(currentAuthentication)) {
            Optional<ForwardedUser> forwardedUser = ForwardedUserParser.from(request);

            // 如果解析到转发的用户信息，则构建认证对象并设置到安全上下文中
            forwardedUser.ifPresent(
                    user -> {
                        log.debug("检测到转发的用户信息，用户名: {}", user.username());

                        // 将用户角色转换为Spring Security所需的权限格式
                        List<SimpleGrantedAuthority> authorities =
                                user.roles().stream()
                                        .filter(StringUtils::hasText) // 过滤空字符串
                                        .map(
                                                role ->
                                                        // 确保角色名称以 ROLE_ 开头
                                                        role.startsWith("ROLE_")
                                                                ? role
                                                                : "ROLE_" + role)
                                        .map(
                                                SimpleGrantedAuthority
                                                        ::new) // 转换为SimpleGrantedAuthority对象
                                        .collect(Collectors.toList()); // 收集为List

                        log.debug("用户角色转换完成，角色数量: {}", authorities.size());

                        // 创建转发认证令牌
                        ForwardedAuthenticationToken authentication =
                                new ForwardedAuthenticationToken(user, authorities);

                        // 将认证信息设置到Spring Security上下文中
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.debug("成功设置转发用户认证信息到安全上下文，用户名: {}", user.username());
                    });

            // 如果没有解析到转发的用户信息，记录警告日志
            if (forwardedUser.isEmpty()) {
                log.warn("未检测到转发的用户信息");
            }
        } else {
            // 如果已有认证信息，记录调试日志
            log.debug("已存在认证信息，跳过转发用户认证处理");
        }

        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }

    private boolean shouldPopulateFromHeaders(Authentication currentAuthentication) {
        if (currentAuthentication == null) {
            return true;
        }
        if (!currentAuthentication.isAuthenticated()) {
            return true;
        }
        if (currentAuthentication instanceof AnonymousAuthenticationToken) {
            return true;
        }
        return false;
    }
}
