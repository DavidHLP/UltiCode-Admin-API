package com.david.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.david.auth.entity.BlacklistedIp;
import com.david.auth.mapper.BlacklistedIpMapper;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityPolicyService {

    private static final int LOGIN_MAX_ATTEMPTS_PER_MINUTE = 20;

    private final BlacklistedIpMapper blacklistedIpMapper;
    private final Clock clock;
    private final Map<String, RateCounter> loginCounters = new ConcurrentHashMap<>();

    public void ensureIpAllowed(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return;
        }
        LambdaQueryWrapper<BlacklistedIp> query =
                new LambdaQueryWrapper<BlacklistedIp>()
                        .eq(BlacklistedIp::getIpAddress, ipAddress)
                        .and(
                                wrapper ->
                                        wrapper.isNull(BlacklistedIp::getExpiresAt)
                                                .or()
                                                .gt(
                                                        BlacklistedIp::getExpiresAt,
                                                        LocalDateTime.now(clock)));
        BlacklistedIp blocked = blacklistedIpMapper.selectOne(query);
        if (blocked != null) {
            log.warn("IP {} 在黑名单中，拒绝访问", ipAddress);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "IP地址已被临时封禁");
        }
    }

    public void registerLoginAttempt(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return;
        }
        RateCounter counter =
                loginCounters.computeIfAbsent(ipAddress, key -> new RateCounter(clock, 60_000));
        int attempts = counter.increment();
        if (attempts > LOGIN_MAX_ATTEMPTS_PER_MINUTE) {
            log.warn("IP {} 登录尝试过于频繁: {}", ipAddress, attempts);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "请求过于频繁，请稍后重试");
        }
    }

    private static final class RateCounter {
        private final Clock clock;
        private final long windowMillis;
        private volatile long windowStart;
        private final AtomicInteger counter = new AtomicInteger(0);

        private RateCounter(Clock clock, long windowMillis) {
            this.clock = clock;
            this.windowMillis = windowMillis;
            this.windowStart = clock.millis();
        }

        int increment() {
            long now = clock.millis();
            if (now - windowStart > windowMillis) {
                windowStart = now;
                counter.set(0);
            }
            return counter.incrementAndGet();
        }
    }
}
