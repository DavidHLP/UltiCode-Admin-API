package com.david.commons.redis.lock;

import java.util.concurrent.TimeUnit;

/**
 * 锁重试策略接口
 *
 * @author David
 */
public interface LockRetryStrategy {

    /**
     * 计算下次重试的延迟时间
     *
     * @param attempt   当前重试次数（从1开始）
     * @param baseDelay 基础延迟时间
     * @param unit      时间单位
     * @return 延迟时间（毫秒）
     */
    long calculateDelay(int attempt, long baseDelay, TimeUnit unit);

    /**
     * 判断是否应该继续重试
     *
     * @param attempt     当前重试次数（从1开始）
     * @param maxAttempts 最大重试次数
     * @param elapsedTime 已经过的时间（毫秒）
     * @param maxWaitTime 最大等待时间（毫秒）
     * @return 是否继续重试
     */
    boolean shouldRetry(int attempt, int maxAttempts, long elapsedTime, long maxWaitTime);

    /**
     * 固定延迟重试策略
     */
    static LockRetryStrategy fixedDelay() {
        return new FixedDelayRetryStrategy();
    }

    /**
     * 指数退避重试策略
     */
    static LockRetryStrategy exponentialBackoff() {
        return new ExponentialBackoffRetryStrategy();
    }

    /**
     * 线性退避重试策略
     */
    static LockRetryStrategy linearBackoff() {
        return new LinearBackoffRetryStrategy();
    }

    /**
     * 固定延迟重试策略实现
     */
    class FixedDelayRetryStrategy implements LockRetryStrategy {
        @Override
        public long calculateDelay(int attempt, long baseDelay, TimeUnit unit) {
            return unit.toMillis(baseDelay);
        }

        @Override
        public boolean shouldRetry(int attempt, int maxAttempts, long elapsedTime, long maxWaitTime) {
            return attempt <= maxAttempts && elapsedTime < maxWaitTime;
        }
    }

    /**
     * 指数退避重试策略实现
     */
    class ExponentialBackoffRetryStrategy implements LockRetryStrategy {
        private static final double BACKOFF_MULTIPLIER = 2.0;
        private static final long MAX_DELAY_MS = 5000; // 最大延迟5秒

        @Override
        public long calculateDelay(int attempt, long baseDelay, TimeUnit unit) {
            long baseDelayMs = unit.toMillis(baseDelay);
            long delay = (long) (baseDelayMs * Math.pow(BACKOFF_MULTIPLIER, attempt - 1));
            return Math.min(delay, MAX_DELAY_MS);
        }

        @Override
        public boolean shouldRetry(int attempt, int maxAttempts, long elapsedTime, long maxWaitTime) {
            return attempt <= maxAttempts && elapsedTime < maxWaitTime;
        }
    }

    /**
     * 线性退避重试策略实现
     */
    class LinearBackoffRetryStrategy implements LockRetryStrategy {
        @Override
        public long calculateDelay(int attempt, long baseDelay, TimeUnit unit) {
            long baseDelayMs = unit.toMillis(baseDelay);
            return baseDelayMs * attempt;
        }

        @Override
        public boolean shouldRetry(int attempt, int maxAttempts, long elapsedTime, long maxWaitTime) {
            return attempt <= maxAttempts && elapsedTime < maxWaitTime;
        }
    }
}