package com.david.commons.redis.lock.impl;

import com.david.commons.redis.config.RedisCommonsProperties;
import com.david.commons.redis.exception.RedisLockException;
import com.david.commons.redis.lock.LockType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * RedissonDistributedLockManager 单元测试
 *
 * @author David
 */
@ExtendWith(MockitoExtension.class)
class RedissonDistributedLockManagerTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock rLock;

    @Mock
    private RLock fairLock;

    @Mock
    private RReadWriteLock readWriteLock;

    @Mock
    private RLock readLock;

    @Mock
    private RLock writeLock;

    private RedisCommonsProperties properties;
    private RedissonDistributedLockManager lockManager;

    @BeforeEach
    void setUp() {
        properties = new RedisCommonsProperties();
        properties.setKeyPrefix("test:");

        RedisCommonsProperties.LockConfig lockConfig = new RedisCommonsProperties.LockConfig();
        lockConfig.setDefaultWaitTime(10);
        lockConfig.setDefaultLeaseTime(30);
        lockConfig.setEnableWatchdog(true);
        properties.setLock(lockConfig);

        lockManager = new RedissonDistributedLockManager(redissonClient, properties);
    }

    @Test
    void testGetLock() {
        // Given
        when(redissonClient.getLock("test:lock:testKey")).thenReturn(rLock);

        // When
        RLock result = lockManager.getLock("testKey");

        // Then
        assertNotNull(result);
        assertEquals(rLock, result);
        verify(redissonClient).getLock("test:lock:testKey");
    }

    @Test
    void testGetFairLock() {
        // Given
        when(redissonClient.getFairLock("test:lock:testKey")).thenReturn(fairLock);

        // When
        RLock result = lockManager.getFairLock("testKey");

        // Then
        assertNotNull(result);
        assertEquals(fairLock, result);
        verify(redissonClient).getFairLock("test:lock:testKey");
    }

    @Test
    void testGetReadWriteLock() {
        // Given
        when(redissonClient.getReadWriteLock("test:lock:testKey")).thenReturn(readWriteLock);

        // When
        RReadWriteLock result = lockManager.getReadWriteLock("testKey");

        // Then
        assertNotNull(result);
        assertEquals(readWriteLock, result);
        verify(redissonClient).getReadWriteLock("test:lock:testKey");
    }

    @Test
    void testTryLockSuccess() throws InterruptedException {
        // Given
        when(redissonClient.getLock("test:lock:testKey")).thenReturn(rLock);
        when(rLock.tryLock(10, 30, TimeUnit.SECONDS)).thenReturn(true);

        // When
        boolean result = lockManager.tryLock("testKey", 10, 30, TimeUnit.SECONDS);

        // Then
        assertTrue(result);
        verify(rLock).tryLock(10, 30, TimeUnit.SECONDS);
    }

    @Test
    void testTryLockFailed() throws InterruptedException {
        // Given
        when(redissonClient.getLock("test:lock:testKey")).thenReturn(rLock);
        when(rLock.tryLock(10, 30, TimeUnit.SECONDS)).thenReturn(false);

        // When
        boolean result = lockManager.tryLock("testKey", 10, 30, TimeUnit.SECONDS);

        // Then
        assertFalse(result);
        verify(rLock).tryLock(10, 30, TimeUnit.SECONDS);
    }

    @Test
    void testTryLockInterrupted() throws InterruptedException {
        // Given
        when(redissonClient.getLock("test:lock:testKey")).thenReturn(rLock);
        when(rLock.tryLock(10, 30, TimeUnit.SECONDS)).thenThrow(new InterruptedException("Test interrupt"));

        // When & Then
        RedisLockException exception = assertThrows(RedisLockException.class,
                () -> lockManager.tryLock("testKey", 10, 30, TimeUnit.SECONDS));

        assertTrue(exception.getMessage().contains("Lock acquisition interrupted"));
        assertTrue(Thread.currentThread().isInterrupted());
    }

    @Test
    void testTryLockWithFairLockType() throws InterruptedException {
        // Given
        when(redissonClient.getFairLock("test:lock:testKey")).thenReturn(fairLock);
        when(fairLock.tryLock(10, 30, TimeUnit.SECONDS)).thenReturn(true);

        // When
        boolean result = lockManager.tryLock("testKey", 10, 30, TimeUnit.SECONDS, LockType.FAIR);

        // Then
        assertTrue(result);
        verify(fairLock).tryLock(10, 30, TimeUnit.SECONDS);
    }

    @Test
    void testTryLockWithReadLockType() throws InterruptedException {
        // Given
        when(redissonClient.getReadWriteLock("test:lock:testKey")).thenReturn(readWriteLock);
        when(readWriteLock.readLock()).thenReturn(readLock);
        when(readLock.tryLock(10, 30, TimeUnit.SECONDS)).thenReturn(true);

        // When
        boolean result = lockManager.tryLock("testKey", 10, 30, TimeUnit.SECONDS, LockType.READ);

        // Then
        assertTrue(result);
        verify(readLock).tryLock(10, 30, TimeUnit.SECONDS);
    }

    @Test
    void testTryLockWithWriteLockType() throws InterruptedException {
        // Given
        when(redissonClient.getReadWriteLock("test:lock:testKey")).thenReturn(readWriteLock);
        when(readWriteLock.writeLock()).thenReturn(writeLock);
        when(writeLock.tryLock(10, 30, TimeUnit.SECONDS)).thenReturn(true);

        // When
        boolean result = lockManager.tryLock("testKey", 10, 30, TimeUnit.SECONDS, LockType.WRITE);

        // Then
        assertTrue(result);
        verify(writeLock).tryLock(10, 30, TimeUnit.SECONDS);
    }

    @Test
    void testUnlock() {
        // Given
        when(redissonClient.getLock("test:lock:testKey")).thenReturn(rLock);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        // When
        lockManager.unlock("testKey");

        // Then
        verify(rLock).unlock();
    }

    @Test
    void testUnlockNotHeldByCurrentThread() {
        // Given
        when(redissonClient.getLock("test:lock:testKey")).thenReturn(rLock);
        when(rLock.isHeldByCurrentThread()).thenReturn(false);

        // When
        lockManager.unlock("testKey");

        // Then
        verify(rLock, never()).unlock();
    }

    @Test
    void testUnlockWithException() {
        // Given
        when(redissonClient.getLock("test:lock:testKey")).thenReturn(rLock);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        doThrow(new RuntimeException("Test exception")).when(rLock).unlock();

        // When & Then
        RedisLockException exception = assertThrows(RedisLockException.class, () -> lockManager.unlock("testKey"));

        assertTrue(exception.getMessage().contains("Failed to release lock"));
    }

    @Test
    void testExecuteWithLockSuccess() throws InterruptedException {
        // Given
        when(redissonClient.getLock("test:lock:testKey")).thenReturn(rLock);
        when(rLock.tryLock(10, 30, TimeUnit.SECONDS)).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        AtomicInteger counter = new AtomicInteger(0);

        // When
        Integer result = lockManager.executeWithLock("testKey", () -> {
            counter.incrementAndGet();
            return counter.get();
        }, 10, 30, TimeUnit.SECONDS);

        // Then
        assertEquals(1, result);
        assertEquals(1, counter.get());
        verify(rLock).tryLock(10, 30, TimeUnit.SECONDS);
        verify(rLock).unlock();
    }

    @Test
    void testExecuteWithLockTimeout() throws InterruptedException {
        // Given
        when(redissonClient.getLock("test:lock:testKey")).thenReturn(rLock);
        when(rLock.tryLock(10, 30, TimeUnit.SECONDS)).thenReturn(false);

        // When & Then
        RedisLockException exception = assertThrows(RedisLockException.class,
                () -> lockManager.executeWithLock("testKey", () -> "test", 10, 30, TimeUnit.SECONDS));

        assertTrue(exception.getMessage().contains("Failed to acquire lock within timeout"));
    }

    @Test
    void testExecuteWithLockOperationException() throws InterruptedException {
        // Given
        when(redissonClient.getLock("test:lock:testKey")).thenReturn(rLock);
        when(rLock.tryLock(10, 30, TimeUnit.SECONDS)).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        // When & Then
        RedisLockException exception = assertThrows(RedisLockException.class,
                () -> lockManager.executeWithLock("testKey", () -> {
                    throw new RuntimeException("Operation failed");
                }, 10, 30, TimeUnit.SECONDS));

        assertTrue(exception.getMessage().contains("Operation failed while holding lock"));
        verify(rLock).unlock(); // Should still unlock
    }

    @Test
    void testExecuteWithLockRunnable() throws InterruptedException {
        // Given
        when(redissonClient.getLock("test:lock:testKey")).thenReturn(rLock);
        when(rLock.tryLock(10, 30, TimeUnit.SECONDS)).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        AtomicBoolean executed = new AtomicBoolean(false);

        // When
        lockManager.executeWithLock("testKey", () -> executed.set(true), 10, 30, TimeUnit.SECONDS);

        // Then
        assertTrue(executed.get());
        verify(rLock).tryLock(10, 30, TimeUnit.SECONDS);
        verify(rLock).unlock();
    }

    @Test
    void testIsLocked() {
        // Given
        when(redissonClient.getLock("test:lock:testKey")).thenReturn(rLock);
        when(rLock.isLocked()).thenReturn(true);

        // When
        boolean result = lockManager.isLocked("testKey");

        // Then
        assertTrue(result);
        verify(rLock).isLocked();
    }

    @Test
    void testIsHeldByCurrentThread() {
        // Given
        when(redissonClient.getLock("test:lock:testKey")).thenReturn(rLock);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        // When
        boolean result = lockManager.isHeldByCurrentThread("testKey");

        // Then
        assertTrue(result);
        verify(rLock).isHeldByCurrentThread();
    }

    @Test
    void testGetHoldCount() {
        // Given
        when(redissonClient.getLock("test:lock:testKey")).thenReturn(rLock);
        when(rLock.getHoldCount()).thenReturn(2);

        // When
        int result = lockManager.getHoldCount("testKey");

        // Then
        assertEquals(2, result);
        verify(rLock).getHoldCount();
    }

    @Test
    void testForceUnlock() {
        // Given
        when(redissonClient.getLock("test:lock:testKey")).thenReturn(rLock);
        when(rLock.forceUnlock()).thenReturn(true);

        // When
        boolean result = lockManager.forceUnlock("testKey");

        // Then
        assertTrue(result);
        verify(rLock).forceUnlock();
    }

    @Test
    void testForceUnlockWithException() {
        // Given
        when(redissonClient.getLock("test:lock:testKey")).thenReturn(rLock);
        when(rLock.forceUnlock()).thenThrow(new RuntimeException("Force unlock failed"));

        // When & Then
        RedisLockException exception = assertThrows(RedisLockException.class, () -> lockManager.forceUnlock("testKey"));

        assertTrue(exception.getMessage().contains("Failed to force unlock"));
    }

    @Test
    void testUnsupportedLockType() {
        // When & Then
        RedisLockException exception = assertThrows(RedisLockException.class,
                () -> lockManager.tryLock("testKey", 10, 30, TimeUnit.SECONDS, LockType.MULTI));

        assertTrue(exception.getMessage().contains("Unsupported lock type"));
    }
}