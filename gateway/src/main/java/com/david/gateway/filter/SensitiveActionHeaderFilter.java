package com.david.gateway.filter;

import lombok.extern.slf4j.Slf4j;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import java.util.Set;

@Slf4j
@Component
public class SensitiveActionHeaderFilter implements GlobalFilter, Ordered {

    private static final String SENSITIVE_HEADER = "X-Sensitive-Action-Token";
    private static final Set<HttpMethod> SENSITIVE_METHODS =
            Set.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.PATCH);
    private static final String[] SENSITIVE_PATHS = {"/api/admin/**", "/api/judge/**"};

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpMethod method = exchange.getRequest().getMethod();
        String path = exchange.getRequest().getPath().value();
        if (requiresSensitiveHeader(method, path)) {
            String header = exchange.getRequest().getHeaders().getFirst(SENSITIVE_HEADER);
            if (!StringUtils.hasText(header)) {
                log.warn("敏感操作缺少校验头，path={} method={}", path, method);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
        }
        return chain.filter(exchange);
    }

    private boolean requiresSensitiveHeader(HttpMethod method, String path) {
        if (method == null || path == null) {
            return false;
        }
        if (!SENSITIVE_METHODS.contains(method)) {
            return false;
        }
        for (String pattern : SENSITIVE_PATHS) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return -150;
    }
}
