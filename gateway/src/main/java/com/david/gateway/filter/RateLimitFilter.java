package com.david.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter implements GlobalFilter, Ordered {

    private static final long WINDOW_MILLIS = 60_000;
    private static final int DEFAULT_LIMIT = 240;
    private static final Map<String, Integer> PATH_LIMITS =
            Map.of("/api/auth/login", 40, "/api/auth/register", 20, "/api/auth/refresh", 80);

    private final ConcurrentHashMap<String, RateBucket> rateBuckets = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String clientIp = resolveClientIp(request);
        if (!StringUtils.hasText(clientIp)) {
            return chain.filter(exchange);
        }
        int limit = PATH_LIMITS.entrySet().stream()
                .filter(entry -> path.startsWith(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(DEFAULT_LIMIT);
        RateBucket bucket = rateBuckets.computeIfAbsent(clientIp, key -> new RateBucket());
        if (bucket.incrementAndCheck(limit)) {
            return chain.filter(exchange);
        }
        log.warn("触发限流，IP={}, path={}", clientIp, path);
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -200;
    }

    private String resolveClientIp(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String header = headers.getFirst("X-Forwarded-For");
        if (StringUtils.hasText(header)) {
            return header.split(",")[0].trim();
        }
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        return remoteAddress == null ? null : Objects.toString(remoteAddress.getAddress(), null);
    }

    private static final class RateBucket {
        private final AtomicInteger counter = new AtomicInteger(0);
        private volatile long windowStart = Instant.now().toEpochMilli();

        boolean incrementAndCheck(int limit) {
            long now = Instant.now().toEpochMilli();
            if (now - windowStart > WINDOW_MILLIS) {
                windowStart = now;
                counter.set(0);
            }
            int current = counter.incrementAndGet();
            return current <= limit;
        }
    }
}
