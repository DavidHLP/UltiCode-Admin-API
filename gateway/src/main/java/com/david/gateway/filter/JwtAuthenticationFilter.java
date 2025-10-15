package com.david.gateway.filter;

import com.david.common.forward.ForwardedUser;
import com.david.common.forward.ForwardedUserHeaders;
import com.david.common.http.ApiError;
import com.david.common.http.ApiResponse;
import com.david.gateway.config.AppProperties;
import com.david.gateway.support.AuthClient;
import com.david.gateway.support.IntrospectResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final AuthClient authClient;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(
            AuthClient authClient, AppProperties appProperties, ObjectMapper objectMapper) {
        this.authClient = authClient;
        this.appProperties = appProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }
        String path = exchange.getRequest().getPath().value();
        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }
        String authorization =
                exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            return respond(exchange, HttpStatus.UNAUTHORIZED, "Missing Authorization header");
        }
        String token = authorization.substring(7);
        return authClient
                .introspect(token)
                .flatMap(payload -> continueChainWithUser(chain, exchange, payload))
                .onErrorResume(
                        WebClientResponseException.class,
                        ex -> {
                            if (ex.getStatusCode().is4xxClientError()) {
                                return respond(
                                        exchange,
                                        HttpStatus.UNAUTHORIZED,
                                        "Invalid or expired token");
                            }
                            return respond(
                                    exchange,
                                    HttpStatus.SERVICE_UNAVAILABLE,
                                    "Authentication service unavailable");
                        })
                .onErrorResume(
                        ex -> {
                            if (ex instanceof IllegalStateException illegalStateException) {
                                String failureMessage =
                                        StringUtils.hasText(illegalStateException.getMessage())
                                                ? illegalStateException.getMessage()
                                                : "Invalid or expired token";
                                return respond(exchange, HttpStatus.UNAUTHORIZED, failureMessage);
                            }
                            return respond(
                                    exchange,
                                    HttpStatus.SERVICE_UNAVAILABLE,
                                    "Authentication service unavailable");
                        });
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isWhitelisted(String path) {
        List<String> whitelist = appProperties.getWhiteListPaths();
        return whitelist != null
                && whitelist.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private Mono<Void> continueChainWithUser(
            GatewayFilterChain chain, ServerWebExchange exchange, IntrospectResponse payload) {
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
    }

    private Mono<Void> respond(ServerWebExchange exchange, HttpStatus status, String message) {
        ApiResponse<Void> errorResponse =
                ApiResponse.failure(
                        ApiError.of(status.value(), status.name(), message));
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(errorResponse);
        } catch (Exception ex) {
            bytes =
                    ("{\"status\":"
                                    + status.value()
                                    + ",\"message\":\""
                                    + message
                                    + "\"}")
                            .getBytes(StandardCharsets.UTF_8);
        }
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse()
                .getHeaders()
                .set(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }
}
