package com.david.filter;

import com.david.auth.AuthUserInfo;
import com.david.interfaces.AuthFeignClient;
import com.david.log.commons.LogUtils;
import com.david.utils.ResponseResult;
import com.david.utils.enums.ResponseCode;

import jakarta.annotation.Resource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();
    private static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";
    private static final int HIGHEST_PRECEDENCE = -100;
    private static final String HEADER_AUTHORIZATION = HttpHeaders.AUTHORIZATION;
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_NAME = "X-User-Name";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_ROLES = "X-User-Roles";
    private static final String DEFAULT_ROLE = "USER";
    private static final String EMPTY_STRING = "";
    private static final List<String> PUBLIC_PATHS =
            List.of(
                    "/favicon.ico",
                    "/error",
                    "/api/auth/login",
                    "/api/auth/register",
                    "/api/auth/refresh",
                    "/api/auth/validate",
                    "/api/auth/send-code",
                    "/actuator/health",
                    "/favicon.ico");
    @Lazy @Resource private AuthFeignClient authFeignClient;

    private static Map<String, Object> getUserContext(
            AuthUserInfo authUser, Map<String, Object> requestContext) {
        Map<String, Object> userContext = new HashMap<>(requestContext);
        userContext.put("userId", authUser.getUserId());
        userContext.put("username", authUser.getUsername());
        userContext.put(
                "roles",
                authUser.getRole() != null ? authUser.getRole().getRoleName() : DEFAULT_ROLE);
        return userContext;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        request.getMethod();
        String method = request.getMethod().name();
        String clientIp =
                Optional.ofNullable(request.getRemoteAddress())
                        .map(InetSocketAddress::getAddress)
                        .map(InetAddress::getHostAddress)
                        .orElse("unknown");

        // 记录请求开始
        Map<String, Object> requestContext = new HashMap<>();
        requestContext.put("path", path);
        requestContext.put("method", method);
        requestContext.put("clientIp", clientIp);
        requestContext.put("headers", request.getHeaders());

        // 记录网关请求
        LogUtils.business()
                .auto()
                .message(String.format("接收到新的请求，方法：%s，路径：%s", method, path))
                .info();

        if (log.isInfoEnabled()) {
            log.info("处理请求: {} {}", request.getMethod(), path);
        }

        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            // 记录公共路径访问
            LogUtils.business()
                    .auto()
                    .message(String.format("已访问公共路径，方法：%s，路径：%s", method, path))
                    .info();
            log.debug("公开路径，直接放行: {}", path);
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HEADER_AUTHORIZATION);
        if (authHeader == null
                || authHeader.trim().isEmpty()
                || !authHeader.startsWith(BEARER_PREFIX)) {
            String errorMsg = "请求缺少有效的 Authorization 头";
            // TODO 添加日志记录错误信息
            log.warn("请求缺少有效的 Authorization 头");
            return handleUnauthorized(exchange.getResponse(), errorMsg);
        }

        String token = authHeader.substring(BEARER_PREFIX_LENGTH).trim();

        return Mono.fromCallable(
                        () -> {
                            try {
                                long startTime = System.currentTimeMillis();
                                var result = authFeignClient.loadUserByUsername(token).getData();
                                long duration = System.currentTimeMillis() - startTime;
                                return result;
                            } catch (Exception e) {
                                String errorMsg = "认证服务调用异常: " + e.getMessage();
                                LogUtils.error("认证服务调用异常: {}", e.getMessage(), e);
                                throw new RuntimeException(errorMsg, e);
                            }
                        })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(
                        authUser -> {
                            if (authUser == null) {
                                String errorMsg = "认证服务返回空结果";
                                // TODO 添加日志记录错误信息
                                log.warn("认证服务返回空结果");
                                return handleUnauthorized(exchange.getResponse(), errorMsg);
                            }

                            // 记录认证成功
                            Map<String, Object> userContext =
                                    getUserContext(authUser, requestContext);

                            LogUtils.business()
                                    .auto()
                                    .message(String.format("身份验证成功：方法 %s，路径 %s", method, path))
                                    .info();

                            if (log.isInfoEnabled()) {
                                log.info("用户认证成功: {}", authUser);
                            }

                            String roleName =
                                    authUser.getRole() != null
                                            ? authUser.getRole().getRoleName()
                                            : DEFAULT_ROLE;

                            ServerHttpRequest mutatedRequest =
                                    exchange.getRequest()
                                            .mutate()
                                            .header(
                                                    HEADER_USER_ID,
                                                    String.valueOf(authUser.getUserId()))
                                            .header(
                                                    HEADER_USER_NAME,
                                                    Objects.toString(
                                                            authUser.getUsername(), EMPTY_STRING))
                                            .header(
                                                    HEADER_USER_EMAIL,
                                                    Objects.toString(
                                                            authUser.getEmail(), EMPTY_STRING))
                                            .header(HEADER_USER_ROLES, roleName)
                                            .build();

                            return chain.filter(exchange.mutate().request(mutatedRequest).build());
                        })
                .onErrorResume(
                        throwable -> {
                            log.error("认证过程中发生异常: {}", throwable.getMessage(), throwable);
                            return handleUnauthorized(
                                    exchange.getResponse(), "认证服务异常: " + throwable.getMessage());
                        });
    }

    private Mono<Void> handleUnauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON);

        String responseJson =
                String.valueOf(ResponseResult.fail(ResponseCode.RC401.getCode(), message));

        return response.writeWith(
                Mono.just(
                        response.bufferFactory()
                                .wrap(responseJson.getBytes(StandardCharsets.UTF_8))));
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
