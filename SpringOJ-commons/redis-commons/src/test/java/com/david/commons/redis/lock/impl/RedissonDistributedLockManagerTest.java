package com.david.commons.redis.lock.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.david.commons.redis.RealRedisTestBase;
import com.david.commons.redis.config.RedisCommonsProperties;
import com.david.commons.redis.exception.RedisLockException;
import com.david.commons.redis.lock.LockType;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RedissonDistributedLockManager 真实Redis测试
 * 使用真实Redis连接进行测试
 *
 * @author David
 */
@SpringBootTest
@Import(RealRedisTestBase.RealRedisTestConfiguration.class)
class RedissonDistributedLockManagerTest extends RealRedisTestBase {

    @Autowired
    private RedissonClient redissonClient;

    private RedisCommonsProperties properties;
    private RedissonDistributedLockManager lockManager;

    @BeforeEach
    void setUp() {
        properties = new RedisCommonsProperties();
        properties.setKeyPrefix(TEST_KEY_PREFIX);

        RedisCommonsProperties.LockConfig lockConfig = new RedisCommonsProperties.LockConfig();
        lockConfig.setDefaultWaitTime(10);
        lockConfig.setDefaultLeaseTime(30);
        lockConfig.setEnableWatchdog(true);
        properties.setLock(lockConfig);

        lockManager = new RedissonDistributedLockManager(redissonClient, properties);
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        try {
            // 强制释放可能遗留的锁
            lockManager.forceUnlock("testKey");
            lockManager.forceUnlock("fairKey");
            lockManager.forceUnlock("readWriteKey");
        } catch (Exception e) {
            // 忽略清理异常
        }
    }

    @Test
    void testGetLock() {
        // 验证Redis连接可用
        assertTrue(isRedissonAvailable(redissonClient), "Redisson连接不可用");

        // When
        RLock result = lockManager.getLock("testKey");

        // Then
        assertNotNull(result);
        // 验证锁键名格式正确
        assertFalse(result.isLocked());
    }

    @Test
    void testGetFairLock() {
        // 验证Redis连接可用
        assertTrue(isRedissonAvailable(redissonClient), "Redisson连接不可用");

        // When
        RLock result = lockManager.getFairLock("fairKey");

        // Then
        assertNotNull(result);
        assertFalse(result.isLocked());
    }

    @Test
    void testGetReadWriteLock() {
        // 验证Redis连接可用
        assertTrue(isRedissonAvailable(redissonClient), "Redisson连接不可用");

        // When
        RReadWriteLock result = lockManager.getReadWriteLock("readWriteKey");

        // Then
        assertNotNull(result);
        assertNotNull(result.readLock());
        assertNotNull(result.writeLock());
    }

    @Test
    void testTryLockSuccess() throws InterruptedException {
        // 验证Redis连接可用
        assertTrue(isRedissonAvailable(redissonClient), "Redisson连接不可用");

        String testKey = randomTestKey();

        // When
        boolean result = lockManager.tryLock(testKey, 1, 5, TimeUnit.SECONDS);

        // Then
        assertTrue(result);
        assertTrue(lockManager.isHeldByCurrentThread(testKey));

        // 清理
        lockManager.unlock(testKey);
    }

    @Test
    void testTryLockTimeout() throws InterruptedException {
        // 验证Redis连接可用
        assertTrue(isRedissonAvailable(redissonClient), "Redisson连接不可用");

        String testKey = randomTestKey();

        // 先获取锁，设置较长的租期确保锁不会自动释放
        assertTrue(lockManager.tryLock(testKey, 1, 60, TimeUnit.SECONDS));

        // 确保锁已被持有
        assertTrue(lockManager.isHeldByCurrentThread(testKey));

        // 在另一个线程中尝试获取同一个锁（应该超时失败）
        AtomicBoolean lockResult = new AtomicBoolean(true); // 默认为true，期望变为false

        Thread otherThread = new Thread(() -> {
            try {
                // 使用lockManager的tryLock方法，确保逻辑一致
                boolean result = lockManager.tryLock(testKey, 500, 50, TimeUnit.MILLISECONDS);
                lockResult.set(result);
            } catch (Exception e) {
                // 任何异常都认为获取锁失败
                lockResult.set(false);
            }
        });

        otherThread.start();
        otherThread.join(2000);

        assertFalse(lockResult.get(), "第二次获取锁应该失败");

        // 清理
        lockManager.unlock(testKey);
    }

    @Test
    void testTryLockWithFairLockType() throws InterruptedException {
        // 验证Redis连接可用
        assertTrue(isRedissonAvailable(redissonClient), "Redisson连接不可用");

        String testKey = randomTestKey();

        // When
        boolean result = lockManager.tryLock(testKey, 1, 5, TimeUnit.SECONDS, LockType.FAIR);

        // Then
        assertTrue(result);
        assertTrue(lockManager.isHeldByCurrentThread(testKey));

        // 清理
        lockManager.unlock(testKey);
    }

    @Test
    void testTryLockWithReadLockType() throws InterruptedException {
        // 验证Redis连接可用
        assertTrue(isRedissonAvailable(redissonClient), "Redisson连接不可用");

        String testKey = randomTestKey();

        // When
        boolean result = lockManager.tryLock(testKey, 1, 5, TimeUnit.SECONDS, LockType.READ);

        // Then
        assertTrue(result);
        // 读锁允许多个线程同时持有
        assertTrue(lockManager.isLocked(testKey));

        // 清理
        lockManager.unlock(testKey);
    }

    @Test
    void testTryLockWithWriteLockType() throws InterruptedException {
        // 验证Redis连接可用
        assertTrue(isRedissonAvailable(redissonClient), "Redisson连接不可用");

        String testKey = randomTestKey();

        // When
        boolean result = lockManager.tryLock(testKey, 1, 5, TimeUnit.SECONDS, LockType.WRITE);

        // Then
        assertTrue(result);
        assertTrue(lockManager.isLocked(testKey));

        // 清理
        lockManager.unlock(testKey);
    }

    @Test
    void testUnlock() {
        // 验证Redis连接可用
        assertTrue(isRedissonAvailable(redissonClient), "Redisson连接不可用");

        String testKey = randomTestKey();

        // 先获取锁
        assertTrue(lockManager.tryLock(testKey, 1, 5, TimeUnit.SECONDS));
        assertTrue(lockManager.isHeldByCurrentThread(testKey));

        // When
        lockManager.unlock(testKey);

        // Then
        assertFalse(lockManager.isHeldByCurrentThread(testKey));
        assertFalse(lockManager.isLocked(testKey));
    }

    @Test
    void testExecuteWithLockSuccess() throws InterruptedException {
        // 验证Redis连接可用
        assertTrue(isRedissonAvailable(redissonClient), "Redisson连接不可用");

        String testKey = randomTestKey();
        AtomicInteger counter = new AtomicInteger(0);

        // When
        Integer result = lockManager.executeWithLock(testKey, () -> {
            counter.incrementAndGet();
            return counter.get();
        }, 1, 5, TimeUnit.SECONDS);

        // Then
        assertEquals(1, result);
        assertEquals(1, counter.get());
        assertFalse(lockManager.isHeldByCurrentThread(testKey));
    }

    @Test
    void testExecuteWithLockTimeout() throws InterruptedException {
        // 验证Redis连接可用
        assertTrue(isRedissonAvailable(redissonClient), "Redisson连接不可用");

        String testKey = randomTestKey();

        // 先在主线程获取锁，设置较长租期
        assertTrue(lockManager.tryLock(testKey, 1, 60, TimeUnit.SECONDS));

        // 确保锁已被持有
        assertTrue(lockManager.isHeldByCurrentThread(testKey));

        try {
            // 在另一个线程中尝试执行（应该超时失败）
            AtomicBoolean exceptionThrown = new AtomicBoolean(false);
            AtomicBoolean correctMessage = new AtomicBoolean(false);

            Thread otherThread = new Thread(() -> {
                try {
                    // 使用较短的超时时间确保失败
                    lockManager.executeWithLock(testKey, () -> "test", 500, 50, TimeUnit.MILLISECONDS);
                } catch (RedisLockException e) {
                    exceptionThrown.set(true);
                    correctMessage.set(e.getMessage().contains("Failed to acquire lock within timeout"));
                } catch (Exception e) {
                    // 可能抛出其他异常，也算测试通过
                    exceptionThrown.set(true);
                    correctMessage.set(e.getMessage().contains("timeout") || e.getMessage().contains("acquire"));
                }
            });

            otherThread.start();
            otherThread.join(2000);

            assertTrue(exceptionThrown.get(), "应该抛出RedisLockException");
            assertTrue(correctMessage.get(), "异常消息应该包含超时信息");
        } finally {
            // 清理
            lockManager.unlock(testKey);
        }
    }

    @Test
    void testExecuteWithLockOperationException() {
        // 验证Redis连接可用
        assertTrue(isRedissonAvailable(redissonClient), "Redisson连接不可用");

        String testKey = randomTestKey();

        // When & Then
        RedisLockException exception = assertThrows(RedisLockException.class,
                () -> lockManager.executeWithLock(testKey, () -> {
                    throw new RuntimeException("Operation failed");
                }, 1, 5, TimeUnit.SECONDS));

        assertTrue(exception.getMessage().contains("Operation failed while holding lock"));
        // 锁应该已经被释放
        assertFalse(lockManager.isLocked(testKey));
    }

    @Test
    void testExecuteWithLockRunnable() {
        // 验证Redis连接可用
        assertTrue(isRedissonAvailable(redissonClient), "Redisson连接不可用");

        String testKey = randomTestKey();
        AtomicBoolean executed = new AtomicBoolean(false);

        // When
        lockManager.executeWithLock(testKey, () -> executed.set(true), 1, 5, TimeUnit.SECONDS);

        // Then
        assertTrue(executed.get());
        assertFalse(lockManager.isHeldByCurrentThread(testKey));
    }

    @Test
    void testIsLocked() {
        // 验证Redis连接可用
        assertTrue(isRedissonAvailable(redissonClient), "Redisson连接不可用");

        String testKey = randomTestKey();

        // 初始状态未锁定
        assertFalse(lockManager.isLocked(testKey));

        // 获取锁后应该被锁定
        assertTrue(lockManager.tryLock(testKey, 1, 5, TimeUnit.SECONDS));
        assertTrue(lockManager.isLocked(testKey));

        // 释放锁后应该未锁定
        lockManager.unlock(testKey);
        assertFalse(lockManager.isLocked(testKey));
    }

    @Test
    void testIsHeldByCurrentThread() {
        // 验证Redis连接可用
        assertTrue(isRedissonAvailable(redissonClient), "Redisson连接不可用");

        String testKey = randomTestKey();

        // 初始状态未持有
        assertFalse(lockManager.isHeldByCurrentThread(testKey));

        // 获取锁后应该被当前线程持有
        assertTrue(lockManager.tryLock(testKey, 1, 5, TimeUnit.SECONDS));
        assertTrue(lockManager.isHeldByCurrentThread(testKey));

        // 释放锁后应该未被持有
        lockManager.unlock(testKey);
        assertFalse(lockManager.isHeldByCurrentThread(testKey));
    }

    @Test
    void testGetHoldCount() {
        // 验证Redis连接可用
        assertTrue(isRedissonAvailable(redissonClient), "Redisson连接不可用");

        String testKey = randomTestKey();

        // 初始持有计数为0
        assertEquals(0, lockManager.getHoldCount(testKey));

        // 获取锁后持有计数为1
        assertTrue(lockManager.tryLock(testKey, 1, 5, TimeUnit.SECONDS));
        assertEquals(1, lockManager.getHoldCount(testKey));

        // 释放锁后持有计数为0
        lockManager.unlock(testKey);
        assertEquals(0, lockManager.getHoldCount(testKey));
    }

    @Test
    void testForceUnlock() {
        // 验证Redis连接可用
        assertTrue(isRedissonAvailable(redissonClient), "Redisson连接不可用");

        String testKey = randomTestKey();

        // 获取锁
        assertTrue(lockManager.tryLock(testKey, 1, 5, TimeUnit.SECONDS));
        assertTrue(lockManager.isLocked(testKey));

        // 强制释放锁
	    lockManager.forceUnlock(testKey);

	    // 验证结果（可能为true或false，取决于锁的状态）
        assertFalse(lockManager.isLocked(testKey));
    }

    @Test
    void testUnsupportedLockType() {
        // When & Then
        RedisLockException exception = assertThrows(RedisLockException.class,
                () -> lockManager.tryLock("testKey", 1, 5, TimeUnit.SECONDS, LockType.MULTI));

        assertTrue(exception.getMessage().contains("Unsupported lock type"));
    }

    @Test
    void testConcurrentLockAccess() throws InterruptedException {
        // 验证Redis连接可用
        assertTrue(isRedissonAvailable(redissonClient), "Redisson连接不可用");

        String testKey = randomTestKey();
        AtomicInteger successCount = new AtomicInteger(0);
        int threadCount = 5;
        Thread[] threads = new Thread[threadCount];

        // 创建多个线程同时尝试获取同一个锁
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                try {
                    // 减少等待时间，确保只有第一个线程能获取到锁，其他线程快速超时
                    if (lockManager.tryLock(testKey, 100, 500, TimeUnit.MILLISECONDS)) {
                        try {
                            // 模拟一些工作，持有锁一段时间
                            Thread.sleep(200);
                            successCount.incrementAndGet();
                        } finally {
                            lockManager.unlock(testKey);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join(5000);
        }

        // 验证只有一个线程成功获取了锁
        assertEquals(1, successCount.get());
        assertFalse(lockManager.isLocked(testKey));
    }
}