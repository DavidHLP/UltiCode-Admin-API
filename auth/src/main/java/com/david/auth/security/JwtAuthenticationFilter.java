package com.david.auth.security;

import com.david.auth.config.AppProperties;
import com.david.auth.entity.TokenKind;
import com.david.auth.entity.User;
import com.david.auth.service.TokenService;
import com.david.auth.service.UserService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenService tokenService;
    private final UserService userService;
    private final AppProperties appProperties;
    private final TokenSessionManager tokenSessionManager;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(
            JwtService jwtService,
            TokenService tokenService,
            UserService userService,
            AppProperties appProperties,
            TokenSessionManager tokenSessionManager) {
        this.jwtService = jwtService;
        this.tokenService = tokenService;
        this.userService = userService;
        this.appProperties = appProperties;
        this.tokenSessionManager = tokenSessionManager;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getMethod().equals(HttpMethod.OPTIONS.name())) {
            log.debug("跳过路径 {} 的 OPTIONS 请求", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }
        String path = request.getRequestURI();
        log.debug("请求路径: {}", path);
        if (isWhitelisted(path)) {
            log.debug("路径 {} 在白名单中，跳过认证", path);
            filterChain.doFilter(request, response);
            return;
        }
        String token = tokenSessionManager.resolveAccessToken(request);
        if (StringUtils.hasText(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Jws<Claims> jws = jwtService.parseAndValidate(token);
                Claims claims = jws.getBody();
                log.debug("解析JWT令牌成功");

                if (jwtService.isExpired(claims)) {
                    log.warn("令牌已过期");
                    throw new BadCredentialsException("Token expired");
                }

                TokenKind kind = jwtService.extractTokenKind(claims);
                log.debug("令牌类型: {}", kind);
                if (!kind.isAccess()) {
                    log.warn("需要访问令牌，但提供了{}令牌", kind);
                    throw new BadCredentialsException("Access token required");
                }

                tokenService
                        .findActiveToken(token, TokenKind.ACCESS)
                        .orElseThrow(
                                () -> {
                                    log.warn("令牌已被撤销或无效");
                                    return new BadCredentialsException("Token revoked or invalid");
                                });

                Long userId = jwtService.extractUserId(claims);
                log.debug("提取用户ID: {}", userId);
                User user = userService.getActiveUser(userId);
                List<String> roles = userService.findRoleCodes(userId);
                log.debug("用户角色: {}", roles);

                UserPrincipal principal =
                        new UserPrincipal(
                                user.getId(), user.getUsername(), user.getPasswordHash(), roles);

                var authentication =
                        new org.springframework.security.authentication
                                .UsernamePasswordAuthenticationToken(
                                principal, null, principal.getAuthorities());
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.info("用户 {} 认证成功", user.getUsername());
            } catch (JwtException | IllegalArgumentException | BadCredentialsException ex) {
                log.warn("JWT认证失败: {}", ex.getMessage());
                request.setAttribute("SPRING_SECURITY_LAST_EXCEPTION", ex);
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isWhitelisted(String path) {
        List<String> whitelist = appProperties.getWhiteListPaths();
        boolean result =
                whitelist != null
                        && whitelist.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
        if (result) {
            log.trace("路径 {} 匹配白名单模式", path);
        }
        return result;
    }
}
