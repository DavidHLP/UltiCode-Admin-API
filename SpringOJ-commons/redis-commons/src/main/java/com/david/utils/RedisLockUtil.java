package com.david.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis 分布式锁工具类
 * 集成 RedisCacheUtil 和 RedissonClient，提供多种锁机制
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisLockUtil {

    private final RedissonClient redissonClient;
    private final RedisCacheUtil redisCacheUtil;

    // 锁的默认超时时间，单位：秒
    private static final long DEFAULT_LEASE_TIME = 30;
    private static final long DEFAULT_WAIT_TIME = 10;
    private static final String LOCK_PREFIX = "lock:";

    // ========================= Redisson 分布式锁 =========================

    /**
     * 获取分布式锁（Redisson实现）
     */
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
        try {
            return redissonClient.getLock(LOCK_PREFIX + lockKey).tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁[{}]失败：{}", lockKey, e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("获取分布式锁[{}]发生异常：{}", lockKey, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取分布式锁（使用默认参数）
     */
    public boolean tryLock(String lockKey) {
        return tryLock(lockKey, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, TimeUnit.SECONDS);
    }

    /**
     * 释放分布式锁
     */
    public void unlock(String lockKey) {
        try {
            RLock lock = redissonClient.getLock(LOCK_PREFIX + lockKey);
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        } catch (Exception e) {
            log.error("释放分布式锁[{}]发生异常：{}", lockKey, e.getMessage(), e);
        }
    }

    /**
     * 执行带锁的操作（自动释放锁）
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> supplier) {
        return executeWithLock(lockKey, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, TimeUnit.SECONDS, supplier);
    }

    /**
     * 执行带锁的操作（自动释放锁）
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Supplier<T> supplier) {
        if (tryLock(lockKey, waitTime, leaseTime, unit)) {
            try {
                return supplier.get();
            } finally {
                unlock(lockKey);
            }
        }
        throw new RuntimeException("获取锁失败: " + lockKey);
    }

    /**
     * 执行带锁的操作（无返回值）
     */
    public void executeWithLock(String lockKey, Runnable runnable) {
        executeWithLock(lockKey, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, TimeUnit.SECONDS, runnable);
    }

    /**
     * 执行带锁的操作（无返回值）
     */
    public void executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Runnable runnable) {
        if (tryLock(lockKey, waitTime, leaseTime, unit)) {
            try {
                runnable.run();
            } finally {
                unlock(lockKey);
            }
        } else {
            throw new RuntimeException("获取锁失败: " + lockKey);
        }
    }

    // ========================= 读写锁 =========================

    /**
     * 获取读锁
     */
    public boolean tryReadLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
        try {
            return redissonClient.getReadWriteLock(LOCK_PREFIX + lockKey).readLock().tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取读锁[{}]失败：{}", lockKey, e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("获取读锁[{}]发生异常：{}", lockKey, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取读锁（使用默认参数）
     */
    public boolean tryReadLock(String lockKey) {
        return tryReadLock(lockKey, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, TimeUnit.SECONDS);
    }

    /**
     * 获取写锁
     */
    public boolean tryWriteLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
        try {
            return redissonClient.getReadWriteLock(LOCK_PREFIX + lockKey).writeLock().tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取写锁[{}]失败：{}", lockKey, e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("获取写锁[{}]发生异常：{}", lockKey, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取写锁（使用默认参数）
     */
    public boolean tryWriteLock(String lockKey) {
        return tryWriteLock(lockKey, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, TimeUnit.SECONDS);
    }

    /**
     * 释放读写锁
     */
    public void unlockReadWriteLock(String lockKey, boolean isWriteLock) {
        try {
            RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(LOCK_PREFIX + lockKey);
            if (isWriteLock) {
                RLock writeLock = readWriteLock.writeLock();
                if (writeLock.isHeldByCurrentThread()) {
                    writeLock.unlock();
                }
            } else {
                RLock readLock = readWriteLock.readLock();
                if (readLock.isHeldByCurrentThread()) {
                    readLock.unlock();
                }
            }
        } catch (Exception e) {
            log.error("释放{}锁[{}]发生异常：{}", isWriteLock ? "写" : "读", lockKey, e.getMessage(), e);
        }
    }

    /**
     * 执行带读锁的操作
     */
    public <T> T executeWithReadLock(String lockKey, Supplier<T> supplier) {
        return executeWithReadLock(lockKey, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, TimeUnit.SECONDS, supplier);
    }

    /**
     * 执行带读锁的操作
     */
    public <T> T executeWithReadLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Supplier<T> supplier) {
        if (tryReadLock(lockKey, waitTime, leaseTime, unit)) {
            try {
                return supplier.get();
            } finally {
                unlockReadWriteLock(lockKey, false);
            }
        }
        throw new RuntimeException("获取读锁失败: " + lockKey);
    }

    /**
     * 执行带写锁的操作
     */
    public <T> T executeWithWriteLock(String lockKey, Supplier<T> supplier) {
        return executeWithWriteLock(lockKey, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, TimeUnit.SECONDS, supplier);
    }

    /**
     * 执行带写锁的操作
     */
    public <T> T executeWithWriteLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Supplier<T> supplier) {
        if (tryWriteLock(lockKey, waitTime, leaseTime, unit)) {
            try {
                return supplier.get();
            } finally {
                unlockReadWriteLock(lockKey, true);
            }
        }
        throw new RuntimeException("获取写锁失败: " + lockKey);
    }

    // ========================= 基于 RedisCacheUtil 的简单锁 =========================

    /**
     * 基于 RedisCacheUtil 的简单分布式锁
     * 适用于对性能要求不高的场景
     */
    public boolean trySimpleLock(String lockKey, String requestId, long expireTime) {
        return redisCacheUtil.setIfAbsent(LOCK_PREFIX + lockKey, requestId, expireTime);
    }

    /**
     * 释放基于 RedisCacheUtil 的简单锁
     */
    public boolean releaseSimpleLock(String lockKey, String requestId) {
        return redisCacheUtil.releaseLock(LOCK_PREFIX + lockKey, requestId);
    }

    /**
     * 执行带简单锁的操作
     */
    public <T> T executeWithSimpleLock(String lockKey, String requestId, long expireTime, Supplier<T> supplier) {
        if (trySimpleLock(lockKey, requestId, expireTime)) {
            try {
                return supplier.get();
            } finally {
                releaseSimpleLock(lockKey, requestId);
            }
        }
        throw new RuntimeException("获取简单锁失败: " + lockKey);
    }

    // ========================= 锁信息查询 =========================

    /**
     * 检查锁是否存在
     */
    public boolean isLocked(String lockKey) {
        try {
            RLock lock = redissonClient.getLock(LOCK_PREFIX + lockKey);
            return lock.isLocked();
        } catch (Exception e) {
            log.error("检查锁状态异常：{}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 检查当前线程是否持有锁
     */
    public boolean isHeldByCurrentThread(String lockKey) {
        try {
            RLock lock = redissonClient.getLock(LOCK_PREFIX + lockKey);
            return lock.isHeldByCurrentThread();
        } catch (Exception e) {
            log.error("检查锁持有状态异常：{}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取锁的剩余时间（毫秒）
     */
    public long getRemainingTime(String lockKey) {
        try {
            RLock lock = redissonClient.getLock(LOCK_PREFIX + lockKey);
            return lock.remainTimeToLive();
        } catch (Exception e) {
            log.error("获取锁剩余时间异常：{}", e.getMessage(), e);
            return -1;
        }
    }

    /**
     * 强制释放锁（谨慎使用）
     */
    public void forceUnlock(String lockKey) {
        try {
            RLock lock = redissonClient.getLock(LOCK_PREFIX + lockKey);
            lock.forceUnlock();
        } catch (Exception e) {
            log.error("强制释放锁异常：{}", e.getMessage(), e);
        }
    }

    // ========================= 锁的批量操作 =========================

    /**
     * 批量获取锁
     */
    public boolean tryLockBatch(String[] lockKeys, long waitTime, long leaseTime, TimeUnit unit) {
        RLock[] locks = new RLock[lockKeys.length];
        for (int i = 0; i < lockKeys.length; i++) {
            locks[i] = redissonClient.getLock(LOCK_PREFIX + lockKeys[i]);
        }

        try {
            RLock multiLock = redissonClient.getMultiLock(locks);
            return multiLock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("批量获取锁失败：{}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("批量获取锁异常：{}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 批量释放锁
     */
    public void unlockBatch(String[] lockKeys) {
        RLock[] locks = new RLock[lockKeys.length];
        for (int i = 0; i < lockKeys.length; i++) {
            locks[i] = redissonClient.getLock(LOCK_PREFIX + lockKeys[i]);
        }

        try {
            RLock multiLock = redissonClient.getMultiLock(locks);
            if (multiLock.isHeldByCurrentThread()) {
                multiLock.unlock();
            }
        } catch (Exception e) {
            log.error("批量释放锁异常：{}", e.getMessage(), e);
        }
    }

    /**
     * 执行需要多个锁的操作
     */
    public <T> T executeWithMultiLock(String[] lockKeys, Supplier<T> supplier) {
        return executeWithMultiLock(lockKeys, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, TimeUnit.SECONDS, supplier);
    }

    /**
     * 执行需要多个锁的操作
     */
    public <T> T executeWithMultiLock(String[] lockKeys, long waitTime, long leaseTime, TimeUnit unit, Supplier<T> supplier) {
        if (tryLockBatch(lockKeys, waitTime, leaseTime, unit)) {
            try {
                return supplier.get();
            } finally {
                unlockBatch(lockKeys);
            }
        }
        throw new RuntimeException("获取多重锁失败");
    }

    // ========================= 重入锁相关 =========================

    /**
     * 获取可重入锁
     */
    public RLock getReentrantLock(String lockKey) {
        return redissonClient.getLock(LOCK_PREFIX + lockKey);
    }

    /**
     * 获取公平锁
     */
    public RLock getFairLock(String lockKey) {
        return redissonClient.getFairLock(LOCK_PREFIX + lockKey);
    }

    /**
     * 获取读写锁
     */
    public RReadWriteLock getReadWriteLock(String lockKey) {
        return redissonClient.getReadWriteLock(LOCK_PREFIX + lockKey);
    }
}