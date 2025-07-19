package com.david.filter;

import com.david.entity.token.TokenValidateRequest;
import com.david.entity.user.AuthUser;
import com.david.interfaces.AuthFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gateway 全局权限验证过滤器
 * 拦截所有请求，通过 AuthenticationService 验证用户权限
 * 将用户信息存储到请求头中传递给下游服务
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    /**
     * 公开路径，无需权限验证
     */
    private final List<String> publicPaths = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/actuator/health",
            "/favicon.ico");
    @Autowired
    @Lazy
    private AuthFeignClient authFeignClient;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String path = request.getURI().getPath();
        log.info("Gateway 处理请求: {} {}", request.getMethod(), path);

        // 检查是否为公开路径
        if (isPublicPath(path)) {
            log.info("公开路径，直接放行: {}", path);
            return chain.filter(exchange);
        }

        // 获取 Authorization 头
        String authHeader = request.getHeaders().getFirst("Authorization");

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.warn("请求缺少有效的 Authorization 头: {}", path);
            return handleUnauthorized(response, "缺少有效的认证令牌");
        }

        // 提取 token
        String token = authHeader.substring(7);

        // 使用响应式方式调用认证服务，避免阻塞操作
        return Mono.fromCallable(() -> {
                    try {
                        // 调用认证服务验证 token
                        return authFeignClient.loadUserByUsername(TokenValidateRequest.builder().token(token).build()).getData();
                    } catch (Exception e) {
                        log.error("认证服务调用异常: {}", e.getMessage(), e);
                        throw new RuntimeException("认证服务异常: " + e.getMessage(), e);
                    }
                })
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic()) // 在弹性线程池中执行阻塞操作
                .flatMap(authUser -> {
                    if (authUser == null) {
                        log.warn("认证服务返回空结果，token: {}", token);
                        return handleUnauthorized(response, "认证失败");
                    }

                    log.info("用户认证成功: {} (ID: {})", authUser.getUsername(), authUser.getUserId());

                    // 将用户信息添加到请求头中，传递给下游服务
                    ServerHttpRequest mutatedRequest = request.mutate()
                            .header("X-User-Id", String.valueOf(authUser.getUserId()))
                            .header("X-User-Name", authUser.getUsername())
                            .header("X-User-Email", authUser.getEmail() != null ? authUser.getEmail() : "")
                            .header("X-User-Roles", authUser.getRole() != null ? authUser.getRole().getRoleName() : "USER")
                            .header("X-User-Authorities", serializeAuthorities(authUser.getAuthorities()))
                            .build();

                    // 创建新的 exchange 并继续处理
                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(mutatedRequest)
                            .build();

                    return chain.filter(mutatedExchange);
                })
                .onErrorResume(throwable -> {
                    log.error("认证过程中发生异常: {}", throwable.getMessage(), throwable);
                    return handleUnauthorized(response, "认证服务异常: " + throwable.getMessage());
                });
    }

    /**
     * 检查是否为公开路径
     */
    private boolean isPublicPath(String path) {
        return publicPaths.stream().anyMatch(path::startsWith);
    }

    /**
     * 处理未授权请求
     */
    private Mono<Void> handleUnauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        String body = String.format("{\"code\": 401, \"message\": \"%s\", \"data\": null}", message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        // 设置较高优先级，确保在其他过滤器之前执行
        return -100;
    }

    /**
     * 序列化权限集合为字符串
     */
    private String serializeAuthorities(Collection<? extends GrantedAuthority> authorities) {
        if (authorities == null || authorities.isEmpty()) {
            return "";
        }

        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth != null && !auth.isEmpty())
                .collect(Collectors.joining(","));
    }
}
