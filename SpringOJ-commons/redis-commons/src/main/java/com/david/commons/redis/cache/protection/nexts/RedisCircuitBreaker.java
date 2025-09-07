package com.david.commons.redis.cache.protection.nexts;

import com.david.commons.redis.config.RedisCommonsProperties;

import com.david.commons.redis.cache.protection.interfaces.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * 基于Redis的熔断器实现
 *
 * @author David
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisCircuitBreaker implements CircuitBreaker {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisCommonsProperties properties;

    /** 统计信息缓存 */
    private final ConcurrentHashMap<String, CircuitBreakerStatsImpl> statsCache = new ConcurrentHashMap<>();

    /** Lua脚本：熔断器状态检查和更新 */
    private static final String CIRCUIT_BREAKER_SCRIPT = """
            local key = KEYS[1]
            local failure_threshold = tonumber(ARGV[1])
            local recovery_timeout = tonumber(ARGV[2])
            local window_size = tonumber(ARGV[3])
            local operation_type = ARGV[4] -- 'check', 'success', 'failure'

            local now = redis.call('TIME')[1]
            local state_key = key .. ':state'
            local stats_key = key .. ':stats'
            local window_key = key .. ':window'

            -- 获取当前状态
            local state_data = redis.call('HMGET', state_key, 'state', 'last_change', 'failure_count')
            local current_state = state_data[1] or 'CLOSED'
            local last_change = tonumber(state_data[2]) or now
            local failure_count = tonumber(state_data[3]) or 0

            -- 获取滑动窗口数据
            local window_data = redis.call('ZRANGEBYSCORE', window_key, now - window_size, '+inf')
            local total_requests = #window_data
            local failed_requests = 0
            for i = 1, #window_data do
                if window_data[i] == 'failure' then
                    failed_requests = failed_requests + 1
                end
            end

            -- 清理过期的窗口数据
            redis.call('ZREMRANGEBYSCORE', window_key, '-inf', now - window_size)

            if operation_type == 'check' then
                -- 检查是否允许请求
                if current_state == 'CLOSED' then
                    return {1, current_state, total_requests, failed_requests}
                elseif current_state == 'OPEN' then
                    if now - last_change >= recovery_timeout then
                        -- 转换到半开状态
                        redis.call('HMSET', state_key, 'state', 'HALF_OPEN', 'last_change', now)
                        redis.call('EXPIRE', state_key, recovery_timeout * 2)
                        return {1, 'HALF_OPEN', total_requests, failed_requests}
                    else
                        return {0, current_state, total_requests, failed_requests}
                    end
                elseif current_state == 'HALF_OPEN' then
                    return {1, current_state, total_requests, failed_requests}
                end
            elseif operation_type == 'success' then
                -- 记录成功
                redis.call('ZADD', window_key, now, 'success')
                redis.call('EXPIRE', window_key, window_size * 2)

                if current_state == 'HALF_OPEN' then
                    -- 半开状态下成功，转换到关闭状态
                    redis.call('HMSET', state_key, 'state', 'CLOSED', 'last_change', now, 'failure_count', 0)
                    redis.call('EXPIRE', state_key, recovery_timeout * 2)
                    return {1, 'CLOSED', total_requests + 1, failed_requests}
                end

                return {1, current_state, total_requests + 1, failed_requests}
            elseif operation_type == 'failure' then
                -- 记录失败
                redis.call('ZADD', window_key, now, 'failure')
                redis.call('EXPIRE', window_key, window_size * 2)

                local new_failure_count = failure_count + 1
                local new_failed_requests = failed_requests + 1
                local new_total_requests = total_requests + 1

                -- 检查是否需要打开熔断器
                if new_total_requests >= 10 and new_failed_requests >= failure_threshold then
                    redis.call('HMSET', state_key, 'state', 'OPEN', 'last_change', now, 'failure_count', new_failure_count)
                    redis.call('EXPIRE', state_key, recovery_timeout * 2)
                    return {0, 'OPEN', new_total_requests, new_failed_requests}
                else
                    redis.call('HMSET', state_key, 'state', current_state, 'last_change', last_change, 'failure_count', new_failure_count)
                    redis.call('EXPIRE', state_key, recovery_timeout * 2)
                    return {1, current_state, new_total_requests, new_failed_requests}
                end
            end

            return {0, current_state, total_requests, failed_requests}
            """;

    private final DefaultRedisScript<Object> circuitBreakerScript = new DefaultRedisScript<>(CIRCUIT_BREAKER_SCRIPT,
            Object.class);

    @Override
    public <T> T execute(String key, Supplier<T> operation) {
        return execute(key, operation, () -> {
            throw new RuntimeException("熔断器已打开，键：" + key);
        });
    }

    @Override
    public <T> T execute(String key, Supplier<T> operation, Supplier<T> fallback) {
        CircuitBreakerStatsImpl stats = getOrCreateStats(key);
        stats.incrementTotalRequests();

        // 检查熔断器状态
        if (!isRequestAllowed(key)) {
            stats.incrementCircuitOpenRequests();
            log.debug("熔断器已打开，键：{}，执行降级逻辑", key);
            return fallback.get();
        }

        try {
            T result = operation.get();
            recordSuccess(key);
            stats.incrementSuccessfulRequests();
            return result;
        } catch (Exception e) {
            recordFailure(key);
            stats.incrementFailedRequests();
            log.debug("操作失败，键：{}，错误：{}", key, e.getMessage());
            throw e;
        }
    }

    @Override
    public void recordSuccess(String key) {
        try {
            String redisKey = getRedisKey(key);
            int failureThreshold = properties.getProtection().getCircuitBreakerFailureThreshold();
            long recoveryTimeout = properties.getProtection().getCircuitBreakerRecoveryTimeout() / 1000; // 转换为秒
            long windowSize = 60; // 60秒窗口

            redisTemplate.execute(
                    circuitBreakerScript,
                    Arrays.asList(redisKey),
                    String.valueOf(failureThreshold),
                    String.valueOf(recoveryTimeout),
                    String.valueOf(windowSize),
                    "success");

            log.debug("记录熔断器成功事件：{}", key);
        } catch (Exception e) {
            log.error("记录熔断器成功事件时出错：{}", key, e);
        }
    }

    @Override
    public void recordFailure(String key) {
        try {
            String redisKey = getRedisKey(key);
            int failureThreshold = properties.getProtection().getCircuitBreakerFailureThreshold();
            long recoveryTimeout = properties.getProtection().getCircuitBreakerRecoveryTimeout() / 1000; // 转换为秒
            long windowSize = 60; // 60秒窗口

            redisTemplate.execute(
                    circuitBreakerScript,
                    Arrays.asList(redisKey),
                    String.valueOf(failureThreshold),
                    String.valueOf(recoveryTimeout),
                    String.valueOf(windowSize),
                    "failure");

            log.debug("记录熔断器失败事件：{}", key);
        } catch (Exception e) {
            log.error("记录熔断器失败事件时出错：{}", key, e);
        }
    }

    @Override
    public CircuitBreakerState getState(String key) {
        try {
            @SuppressWarnings("unchecked")
            java.util.List<Object> result = (java.util.List<Object>) checkState(key);
            String state = (String) result.get(1);
            return CircuitBreakerState.valueOf(state);
        } catch (Exception e) {
            log.error("获取熔断器状态时出错，键：{}", key, e);
            return CircuitBreakerState.CLOSED;
        }
    }

    @Override
    public void forceOpen(String key) {
        try {
            String redisKey = getRedisKey(key);
            long now = System.currentTimeMillis() / 1000;
            redisTemplate.opsForHash().putAll(redisKey + ":state",
                    java.util.Map.of(
                            "state", "OPEN",
                            "last_change", String.valueOf(now),
                            "failure_count", "999"));
            log.info("强制打开熔断器，键：{}", key);
        } catch (Exception e) {
            log.error("强制打开熔断器时出错，键：{}", key, e);
        }
    }

    @Override
    public void forceClose(String key) {
        try {
            String redisKey = getRedisKey(key);
            long now = System.currentTimeMillis() / 1000;
            redisTemplate.opsForHash().putAll(redisKey + ":state",
                    java.util.Map.of(
                            "state", "CLOSED",
                            "last_change", String.valueOf(now),
                            "failure_count", "0"));
            log.info("强制关闭熔断器，键：{}", key);
        } catch (Exception e) {
            log.error("强制关闭熔断器时出错，键：{}", key, e);
        }
    }

    @Override
    public CircuitBreakerStats getStats(String key) {
        return getOrCreateStats(key);
    }

    /**
     * 检查是否允许请求
     */
    private boolean isRequestAllowed(String key) {
        try {
            @SuppressWarnings("unchecked")
            java.util.List<Object> result = (java.util.List<Object>) checkState(key);
            return ((Number) result.get(0)).intValue() == 1;
        } catch (Exception e) {
            log.error("检查请求是否允许时出错，键：{}", key, e);
            // 发生异常时，为了安全起见，允许请求通过
            return true;
        }
    }

    /**
     * 检查熔断器状态
     */
    private Object checkState(String key) {
        String redisKey = getRedisKey(key);
        int failureThreshold = properties.getProtection().getCircuitBreakerFailureThreshold();
        long recoveryTimeout = properties.getProtection().getCircuitBreakerRecoveryTimeout() / 1000; // 转换为秒
        long windowSize = 60; // 60秒窗口

        return redisTemplate.execute(
                circuitBreakerScript,
                Arrays.asList(redisKey),
                String.valueOf(failureThreshold),
                String.valueOf(recoveryTimeout),
                String.valueOf(windowSize),
                "check");
    }

    /**
     * 获取Redis键名
     */
    private String getRedisKey(String key) {
        return properties.getKeyPrefix() + "circuit_breaker:" + key;
    }

    /**
     * 获取或创建统计信息
     */
    private CircuitBreakerStatsImpl getOrCreateStats(String key) {
        return statsCache.computeIfAbsent(key, CircuitBreakerStatsImpl::new);
    }

    /**
     * 熔断器统计信息实现
     */
    private static class CircuitBreakerStatsImpl implements CircuitBreakerStats {

        private final String key;
        private final AtomicLong totalRequests = new AtomicLong(0);
        private final AtomicLong successfulRequests = new AtomicLong(0);
        private final AtomicLong failedRequests = new AtomicLong(0);
        private final AtomicLong circuitOpenRequests = new AtomicLong(0);
        private volatile long lastStateChangeTime = System.currentTimeMillis();

        public CircuitBreakerStatsImpl(String key) {
            this.key = key;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public CircuitBreakerState getState() {
            // 这里返回一个默认状态，实际状态需要从Redis获取
            return CircuitBreakerState.CLOSED;
        }

        @Override
        public long getTotalRequests() {
            return totalRequests.get();
        }

        @Override
        public long getSuccessfulRequests() {
            return successfulRequests.get();
        }

        @Override
        public long getFailedRequests() {
            return failedRequests.get();
        }

        @Override
        public long getCircuitOpenRequests() {
            return circuitOpenRequests.get();
        }

        @Override
        public double getFailureRate() {
            long total = totalRequests.get();
            return total > 0 ? (double) failedRequests.get() / total : 0.0;
        }

        @Override
        public long getLastStateChangeTime() {
            return lastStateChangeTime;
        }

        @Override
        public long getNextAllowedTime() {
            // 这里返回一个默认值，实际值需要从Redis计算
            return System.currentTimeMillis();
        }

        public void incrementTotalRequests() {
            totalRequests.incrementAndGet();
        }

        public void incrementSuccessfulRequests() {
            successfulRequests.incrementAndGet();
        }

        public void incrementFailedRequests() {
            failedRequests.incrementAndGet();
        }

        public void incrementCircuitOpenRequests() {
            circuitOpenRequests.incrementAndGet();
        }

        public void updateLastStateChangeTime() {
            lastStateChangeTime = System.currentTimeMillis();
        }
    }
}