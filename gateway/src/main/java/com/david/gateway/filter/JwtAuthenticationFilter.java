package com.david.gateway.filter;

import com.david.common.forward.ForwardedUser;
import com.david.common.forward.ForwardedUserHeaders;
import com.david.common.http.ApiError;
import com.david.common.http.ApiResponse;
import com.david.common.forward.AppProperties;
import com.david.gateway.support.AuthClient;
import com.david.gateway.support.IntrospectResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final AuthClient authClient;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher;
    private final long tokenCacheTtlMillis;
    private static final String SESSION_CACHE_KEY = "CF_GATEWAY_AUTH_CACHE";

    public JwtAuthenticationFilter(
            AuthClient authClient,
            AppProperties appProperties,
            ObjectMapper objectMapper,
            AntPathMatcher pathMatcher) {
        this.authClient = authClient;
        this.appProperties = appProperties;
        this.objectMapper = objectMapper;
        this.pathMatcher = pathMatcher;
        Duration ttl = appProperties.getTokenCacheTtl();
        this.tokenCacheTtlMillis = ttl == null ? 0 : Math.max(ttl.toMillis(), 0);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            log.debug("跳过路径 {} 的 OPTIONS 请求", exchange.getRequest().getPath().value());
            return chain.filter(exchange);
        }
        String path = exchange.getRequest().getPath().value();
        if (isWhitelisted(path)) {
            log.debug("路径 {} 在白名单中，跳过认证", path);
            return chain.filter(exchange);
        }
        String authorization =
                exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            log.warn("路径 {} 缺少或无效的 Authorization 头", path);
            return respond(exchange, HttpStatus.UNAUTHORIZED, "缺少 Authorization 头");
        }
        String token = authorization.substring(7);
        log.debug("为路径 {} 验证令牌", path);

        return exchange
                .getSession()
                .flatMap(session -> {
                    CachedAuth cached = session.getAttribute(SESSION_CACHE_KEY);
                    if (cached != null && cached.matches(token) && !cached.isExpired()) {
                        log.trace("命中会话缓存，用户: {}", cached.payload().username());
                        return continueChainWithUser(chain, exchange, cached.payload());
                    }
                    return authClient
                            .introspect(token)
                            .flatMap(
                                    payload -> {
                                        log.debug(
                                                "用户 {} 的令牌验证成功，角色：{}",
                                                payload.username(),
                                                payload.roles());
                                        if (tokenCacheTtlMillis > 0) {
                                            session.getAttributes()
                                                    .put(
                                                            SESSION_CACHE_KEY,
                                                            CachedAuth.from(
                                                                    token,
                                                                    payload,
                                                                    tokenCacheTtlMillis));
                                        }
                                        return continueChainWithUser(chain, exchange, payload);
                                    });
                })
                .onErrorResume(
                        WebClientResponseException.class,
                        ex -> {
                            if (ex.getStatusCode().is4xxClientError()) {
                                log.warn("令牌验证期间客户端错误：{}", ex.getMessage());
                                return respond(exchange, HttpStatus.UNAUTHORIZED, "令牌无效或已过期");
                            }
                            log.error("令牌验证期间服务器错误", ex);
                            return respond(exchange, HttpStatus.SERVICE_UNAVAILABLE, "认证服务不可用");
                        })
                .onErrorResume(
                        ex -> {
                            if (ex instanceof IllegalStateException illegalStateException) {
                                String failureMessage =
                                        StringUtils.hasText(illegalStateException.getMessage())
                                                ? illegalStateException.getMessage()
                                                : "令牌无效或已过期";
                                log.warn("令牌处理期间非法状态：{}", failureMessage);
                                return respond(exchange, HttpStatus.UNAUTHORIZED, failureMessage);
                            }
                            log.error("认证期间发生意外错误", ex);
                            return respond(exchange, HttpStatus.SERVICE_UNAVAILABLE, "认证服务不可用");
                        });
    }

    @Override
    public int getOrder() {
        return -100;
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

    private Mono<Void> continueChainWithUser(
            GatewayFilterChain chain, ServerWebExchange exchange, IntrospectResponse payload) {
        if (payload.userId() == null) {
            log.warn("令牌验证成功但缺少用户ID，拒绝请求");
            return respond(exchange, HttpStatus.UNAUTHORIZED, "认证信息不完整，缺少用户ID");
        }
        if (!StringUtils.hasText(payload.username())) {
            log.warn("令牌验证成功但缺少用户名，拒绝请求");
            return respond(exchange, HttpStatus.UNAUTHORIZED, "认证信息不完整，缺少用户名");
        }
        ForwardedUser forwardedUser =
                ForwardedUser.of(payload.userId(), payload.username(), payload.roles());
        ServerHttpRequest mutatedRequest =
                exchange.getRequest()
                        .mutate()
                        .headers(headers -> applyForwardedUser(headers, forwardedUser))
                        .build();
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private void applyForwardedUser(HttpHeaders headers, ForwardedUser user) {
        if (user.id() != null) {
            headers.set(ForwardedUserHeaders.USER_ID, String.valueOf(user.id()));
        } else {
            headers.remove(ForwardedUserHeaders.USER_ID);
        }
        String username = user.username();
        headers.set(ForwardedUserHeaders.USER_NAME, username == null ? "" : username);
        String roles =
                String.join(
                        ForwardedUserHeaders.ROLE_DELIMITER,
                        user.roles() == null ? List.of() : user.roles());
        headers.set(ForwardedUserHeaders.USER_ROLES, roles);
        log.debug("为用户 {} 应用转发用户头，ID：{}，角色：{}", username, user.id(), roles);
    }

    private Mono<Void> respond(ServerWebExchange exchange, HttpStatus status, String message) {
        log.debug("响应状态：{}，消息：{}", status, message);
        ApiResponse<Void> errorResponse =
                ApiResponse.failure(ApiError.of(status.value(), status.name(), message));
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(errorResponse);
        } catch (Exception ex) {
            log.warn("序列化错误响应失败，使用备用方案", ex);
            bytes =
                    ("{\"status\":" + status.value() + ",\"message\":\"" + message + "\"}")
                            .getBytes(StandardCharsets.UTF_8);
        }
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse()
                .getHeaders()
                .set(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    private record CachedAuth(String token, IntrospectResponse payload, long expiresAt) {
        private boolean matches(String rawToken) {
            return Objects.equals(this.token, rawToken);
        }

        private boolean isExpired() {
            return System.currentTimeMillis() >= expiresAt;
        }

        private static CachedAuth from(String token, IntrospectResponse payload, long ttlMillis) {
            long effectiveTtl = Math.max(ttlMillis, 0);
            long expiresAt = System.currentTimeMillis() + effectiveTtl;
            return new CachedAuth(token, payload, expiresAt);
        }
    }
}
