package com.david.commons.redis.cache.protection.nexts;

import com.david.commons.redis.config.RedisCommonsProperties;

import com.david.commons.redis.cache.protection.interfaces.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于令牌桶算法的限流器实现
 *
 * @author David
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenBucketRateLimiter implements RateLimiter {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisCommonsProperties properties;

    /** 统计信息缓存 */
    private final ConcurrentHashMap<String, RateLimitStatsImpl> statsCache = new ConcurrentHashMap<>();

    /** Lua脚本：令牌桶限流 */
    private static final String TOKEN_BUCKET_SCRIPT = """
            local key = KEYS[1]
            local capacity = tonumber(ARGV[1])
            local tokens = tonumber(ARGV[2])
            local interval = tonumber(ARGV[3])
            local requested = tonumber(ARGV[4])

            local bucket = redis.call('HMGET', key, 'tokens', 'last_refill')
            local current_tokens = tonumber(bucket[1]) or capacity
            local last_refill = tonumber(bucket[2]) or redis.call('TIME')[1]

            local now = redis.call('TIME')[1]
            local time_passed = math.max(0, now - last_refill)

            -- 计算应该添加的令牌数
            local tokens_to_add = math.floor(time_passed * tokens / interval)
            current_tokens = math.min(capacity, current_tokens + tokens_to_add)

            local allowed = 0
            if current_tokens >= requested then
                current_tokens = current_tokens - requested
                allowed = 1
            end

            -- 更新令牌桶状态
            redis.call('HMSET', key, 'tokens', current_tokens, 'last_refill', now)
            redis.call('EXPIRE', key, interval * 2)

            return {allowed, current_tokens}
            """;

    private final DefaultRedisScript<Object> tokenBucketScript = new DefaultRedisScript<>(TOKEN_BUCKET_SCRIPT,
            Object.class);

    @Override
    public boolean tryAcquire(String key) {
        return tryAcquire(key, 1);
    }

    @Override
    public boolean tryAcquire(String key, int permits) {
        RateLimitStatsImpl stats = getOrCreateStats(key);
        stats.incrementTotalRequests();

        try {
            String redisKey = getRedisKey(key);
            int capacity = properties.getProtection().getRateLimitPermitsPerSecond();

            @SuppressWarnings("unchecked")
            java.util.List<Long> result = (java.util.List<Long>) redisTemplate.execute(
                    tokenBucketScript,
                    Collections.singletonList(redisKey),
                    capacity, // 容量
                    capacity, // 每秒补充的令牌数
                    1, // 时间间隔（秒）
                    permits // 请求的令牌数
            );

            boolean allowed = result.get(0) == 1;
            long remainingTokens = result.get(1);

            if (allowed) {
                stats.incrementAllowedRequests();
                log.debug("Rate limit allowed for key: {}, remaining tokens: {}", key, remainingTokens);
            } else {
                stats.incrementRejectedRequests();
                log.debug("Rate limit rejected for key: {}, remaining tokens: {}", key, remainingTokens);
            }

            return allowed;
        } catch (Exception e) {
            log.error("Error in rate limiting for key: {}", key, e);
            // 发生异常时，为了安全起见，允许请求通过
            stats.incrementAllowedRequests();
            return true;
        }
    }

    @Override
    public boolean tryAcquire(String key, long timeoutMs) {
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (tryAcquire(key)) {
                return true;
            }

            try {
                Thread.sleep(10); // 等待10ms后重试
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        return false;
    }

    @Override
    public long getAvailablePermits(String key) {
        try {
            String redisKey = getRedisKey(key);
            Object tokens = redisTemplate.opsForHash().get(redisKey, "tokens");
            return tokens != null ? Long.parseLong(tokens.toString()) : 0;
        } catch (Exception e) {
            log.error("Error getting available permits for key: {}", key, e);
            return 0;
        }
    }

    @Override
    public RateLimitStats getStats(String key) {
        return getOrCreateStats(key);
    }

    /**
     * 获取Redis键名
     */
    private String getRedisKey(String key) {
        return properties.getKeyPrefix() + "rate_limit:" + key;
    }

    /**
     * 获取或创建统计信息
     */
    private RateLimitStatsImpl getOrCreateStats(String key) {
        return statsCache.computeIfAbsent(key, RateLimitStatsImpl::new);
    }

    /**
     * 限流统计信息实现
     */
    private static class RateLimitStatsImpl implements RateLimitStats {

        private final String key;
        private final AtomicLong totalRequests = new AtomicLong(0);
        private final AtomicLong allowedRequests = new AtomicLong(0);
        private final AtomicLong rejectedRequests = new AtomicLong(0);
        private volatile long lastRequestTime = System.currentTimeMillis();

        public RateLimitStatsImpl(String key) {
            this.key = key;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public long getTotalRequests() {
            return totalRequests.get();
        }

        @Override
        public long getAllowedRequests() {
            return allowedRequests.get();
        }

        @Override
        public long getRejectedRequests() {
            return rejectedRequests.get();
        }

        @Override
        public double getPassRate() {
            long total = totalRequests.get();
            return total > 0 ? (double) allowedRequests.get() / total : 0.0;
        }

        @Override
        public double getCurrentQps() {
            long now = System.currentTimeMillis();
            long timeDiff = now - lastRequestTime;
            return timeDiff > 0 ? 1000.0 / timeDiff : 0.0;
        }

        public void incrementTotalRequests() {
            totalRequests.incrementAndGet();
            lastRequestTime = System.currentTimeMillis();
        }

        public void incrementAllowedRequests() {
            allowedRequests.incrementAndGet();
        }

        public void incrementRejectedRequests() {
            rejectedRequests.incrementAndGet();
        }
    }
}