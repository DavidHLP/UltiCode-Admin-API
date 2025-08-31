package com.david.commons.redis.lock;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LockRetryStrategy 单元测试
 *
 * @author David
 */
class LockRetryStrategyTest {

    @Test
    void testFixedDelayStrategy() {
        LockRetryStrategy strategy = LockRetryStrategy.fixedDelay();

        // 测试延迟计算
        assertEquals(1000, strategy.calculateDelay(1, 1000, TimeUnit.MILLISECONDS));
        assertEquals(1000, strategy.calculateDelay(2, 1000, TimeUnit.MILLISECONDS));
        assertEquals(1000, strategy.calculateDelay(5, 1000, TimeUnit.MILLISECONDS));

        // 测试重试判断
        assertTrue(strategy.shouldRetry(1, 3, 500, 5000));
        assertTrue(strategy.shouldRetry(3, 3, 4000, 5000));
        assertFalse(strategy.shouldRetry(4, 3, 1000, 5000)); // 超过最大次数
        assertFalse(strategy.shouldRetry(2, 3, 6000, 5000)); // 超过最大时间
    }

    @Test
    void testExponentialBackoffStrategy() {
        LockRetryStrategy strategy = LockRetryStrategy.exponentialBackoff();

        // 测试延迟计算 - 指数增长
        assertEquals(100, strategy.calculateDelay(1, 100, TimeUnit.MILLISECONDS));
        assertEquals(200, strategy.calculateDelay(2, 100, TimeUnit.MILLISECONDS));
        assertEquals(400, strategy.calculateDelay(3, 100, TimeUnit.MILLISECONDS));
        assertEquals(800, strategy.calculateDelay(4, 100, TimeUnit.MILLISECONDS));

        // 测试最大延迟限制
        assertEquals(5000, strategy.calculateDelay(10, 100, TimeUnit.MILLISECONDS));

        // 测试重试判断
        assertTrue(strategy.shouldRetry(1, 5, 1000, 10000));
        assertFalse(strategy.shouldRetry(6, 5, 1000, 10000));
    }

    @Test
    void testLinearBackoffStrategy() {
        LockRetryStrategy strategy = LockRetryStrategy.linearBackoff();

        // 测试延迟计算 - 线性增长
        assertEquals(100, strategy.calculateDelay(1, 100, TimeUnit.MILLISECONDS));
        assertEquals(200, strategy.calculateDelay(2, 100, TimeUnit.MILLISECONDS));
        assertEquals(300, strategy.calculateDelay(3, 100, TimeUnit.MILLISECONDS));
        assertEquals(500, strategy.calculateDelay(5, 100, TimeUnit.MILLISECONDS));

        // 测试重试判断
        assertTrue(strategy.shouldRetry(2, 5, 1000, 10000));
        assertFalse(strategy.shouldRetry(6, 5, 1000, 10000));
    }

    @Test
    void testTimeUnitConversion() {
        LockRetryStrategy strategy = LockRetryStrategy.fixedDelay();

        assertEquals(1000, strategy.calculateDelay(1, 1, TimeUnit.SECONDS));
        assertEquals(60000, strategy.calculateDelay(1, 1, TimeUnit.MINUTES));
        assertEquals(500, strategy.calculateDelay(1, 500, TimeUnit.MILLISECONDS));
    }
}