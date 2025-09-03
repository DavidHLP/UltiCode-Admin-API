package com.david.commons.redis.lock.impl;

import com.david.commons.redis.config.RedisCommonsProperties;
import com.david.commons.redis.exception.RedisErrorCodes;
import com.david.commons.redis.exception.RedisLockException;
import com.david.commons.redis.lock.DistributedLockManager;
import com.david.commons.redis.lock.LockRecoveryHandler;
import com.david.commons.redis.lock.LockRetryStrategy;
import com.david.commons.redis.lock.enums.LockType;

import jakarta.annotation.PreDestroy;

import lombok.extern.slf4j.Slf4j;

import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 基于 Redisson 的分布式锁管理器实现
 *
 * @author David
 */
@Slf4j
@Component
public class RedissonDistributedLockManager implements DistributedLockManager {

    private final RedissonClient redissonClient;
    private final RedisCommonsProperties properties;
    private final ConcurrentHashMap<String, RLock> lockCache = new ConcurrentHashMap<>();
    private final LockRetryStrategy retryStrategy;
    private final LockRecoveryHandler recoveryHandler;

    public RedissonDistributedLockManager(
            RedissonClient redissonClient, RedisCommonsProperties properties) {
        this.redissonClient = redissonClient;
        this.properties = properties;
        this.retryStrategy = LockRetryStrategy.exponentialBackoff();
        this.recoveryHandler = new LockRecoveryHandler();
        this.recoveryHandler.startRecoveryTask();
    }

    @Override
    public RLock getLock(String key) {
        String lockKey = buildLockKey(key);
        return lockCache.computeIfAbsent(lockKey, redissonClient::getLock);
    }

    @Override
    public RLock getFairLock(String key) {
        String lockKey = buildLockKey(key);
        return redissonClient.getFairLock(lockKey);
    }

    @Override
    public RReadWriteLock getReadWriteLock(String key) {
        String lockKey = buildLockKey(key);
        return redissonClient.getReadWriteLock(lockKey);
    }

    @Override
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit) {
        return tryLock(key, waitTime, leaseTime, unit, LockType.REENTRANT);
    }

    @Override
    public boolean tryLock(
            String key, long waitTime, long leaseTime, TimeUnit unit, LockType lockType) {
        return tryLockWithRetry(key, waitTime, leaseTime, unit, lockType, 3);
    }

    /** 带重试机制的锁获取 */
    public boolean tryLockWithRetry(
            String key,
            long waitTime,
            long leaseTime,
            TimeUnit unit,
            LockType lockType,
            int maxAttempts) {
        RLock lock = getLockByType(key, lockType);
        long startTime = System.currentTimeMillis();
        long maxWaitTimeMs = unit.toMillis(waitTime);

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                long elapsedTime = System.currentTimeMillis() - startTime;
                if (elapsedTime >= maxWaitTimeMs) {
                    log.warn("获取锁超时，键为: {}，耗时: {}ms", key, elapsedTime);
                    return false;
                }

                // 计算本次尝试的等待时间，确保不超过剩余时间
                long remainingTime = maxWaitTimeMs - elapsedTime;
                long attemptWaitTime = Math.min(remainingTime, unit.toMillis(waitTime));

                // 将租期统一换算为毫秒，避免单位不一致导致的过期异常
                long leaseTimeMs = unit.toMillis(leaseTime);

                boolean acquired =
                        lock.tryLock(attemptWaitTime, leaseTimeMs, TimeUnit.MILLISECONDS);
                if (acquired) {
                    log.debug("成功获取 {} 类型锁，键为: {}，尝试次数: {}", lockType, key, attempt);
                    // 注册到恢复处理器
                    recoveryHandler.registerLock(key, lock, leaseTime, unit);
                    return true;
                }

                // 检查总等待时间是否已超时
                elapsedTime = System.currentTimeMillis() - startTime;
                if (elapsedTime >= maxWaitTimeMs) {
                    log.warn("获取锁超时，键为: {}，耗时: {}ms", key, elapsedTime);
                    return false;
                }

                // 检查是否应该重试
                if (!retryStrategy.shouldRetry(attempt, maxAttempts, elapsedTime, maxWaitTimeMs)) {
                    break;
                }

                // 计算重试延迟
                if (attempt < maxAttempts) {
                    long retryDelay =
                            retryStrategy.calculateDelay(
                                    attempt,
                                    properties.getLock().getRetryInterval(),
                                    TimeUnit.MILLISECONDS);

                    // 确保重试延迟不会导致总超时
                    if (elapsedTime + retryDelay >= maxWaitTimeMs) {
                        log.warn("获取锁超时，键为: {}，耗时: {}ms", key, elapsedTime);
                        return false;
                    }

                    log.debug(
                            "获取锁失败，键为: {}，{}ms 后重试 (尝试次数 {}/{})",
                            key,
                            retryDelay,
                            attempt,
                            maxAttempts);

                    Thread.sleep(retryDelay);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RedisLockException(
                        RedisErrorCodes.LOCK_INTERRUPTED, "获取锁被中断，键为: " + key, e);
            } catch (Exception e) {
                log.warn("获取锁过程中发生异常，尝试次数: {}，键为: {}", attempt, key, e);
                if (attempt == maxAttempts) {
                    throw new RedisLockException(
                            RedisErrorCodes.LOCK_ACQUISITION_FAILED,
                            "获取锁失败，键为: " + key + "，尝试次数: " + maxAttempts,
                            e);
                }
            }
        }

        log.warn("获取 {} 类型锁失败，键为: {}，尝试次数: {}", lockType, key, maxAttempts);
        return false;
    }

    @Override
    public void unlock(String key) {
        unlock(key, LockType.REENTRANT);
    }

    @Override
    public void unlock(String key, LockType lockType) {
        RLock lock = getLockByType(key, lockType);
        try {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                recoveryHandler.unregisterLock(key);
                log.debug("成功释放 {} 类型锁，键为: {}", lockType, key);
            } else {
                log.warn("尝试释放非当前线程持有的 {} 类型锁，键为: {}", lockType, key);
            }
        } catch (Exception e) {
            log.error("释放 {} 类型锁失败，键为: {}", lockType, key, e);

            // 尝试恢复
            boolean recovered = recoveryHandler.tryRecoverLock(key, lock, e);
            if (!recovered) {
                throw new RedisLockException(
                        RedisErrorCodes.LOCK_RELEASE_FAILED, "释放锁失败，键为: " + key, e);
            }
        }
    }

    @Override
    public <T> T executeWithLock(
            String key, Supplier<T> supplier, long waitTime, long leaseTime, TimeUnit unit) {
        return executeWithLock(key, supplier, waitTime, leaseTime, unit, LockType.REENTRANT);
    }

    @Override
    public <T> T executeWithLock(
            String key,
            Supplier<T> supplier,
            long waitTime,
            long leaseTime,
            TimeUnit unit,
            LockType lockType) {
        RLock lock = getLockByType(key, lockType);
        boolean acquired = false;

        try {
            // 直接使用单次尝试确保严格的超时控制
            acquired = lock.tryLock(waitTime, leaseTime, unit);
            if (!acquired) {
                throw new RedisLockException(
                        RedisErrorCodes.LOCK_TIMEOUT, "在超时时间内未能获取锁，键为: " + key);
            }

            // 注册到恢复处理器
            recoveryHandler.registerLock(key, lock, leaseTime, unit);
            log.debug("使用 {} 类型锁执行操作，键为: {}", lockType, key);
            return supplier.get();

        } catch (RedisLockException e) {
            throw e;
        } catch (Exception e) {
            throw new RedisLockException(
                    RedisErrorCodes.LOCK_OPERATION_FAILED, "持有锁期间操作失败，键为: " + key, e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                try {
                    lock.unlock();
                    recoveryHandler.unregisterLock(key);
                    log.debug("操作完成后释放 {} 类型锁，键为: {}", lockType, key);
                } catch (Exception e) {
                    log.error("操作完成后释放锁失败，键为: {}", key, e);
                }
            }
        }
    }

    @Override
    public void executeWithLock(
            String key, Runnable runnable, long waitTime, long leaseTime, TimeUnit unit) {
        executeWithLock(key, runnable, waitTime, leaseTime, unit, LockType.REENTRANT);
    }

    @Override
    public void executeWithLock(
            String key,
            Runnable runnable,
            long waitTime,
            long leaseTime,
            TimeUnit unit,
            LockType lockType) {
        executeWithLock(
                key,
                () -> {
                    runnable.run();
                    return null;
                },
                waitTime,
                leaseTime,
                unit,
                lockType);
    }

    @Override
    public boolean isLocked(String key) {
        RLock lock = getLock(key);
        return lock.isLocked();
    }

    @Override
    public boolean isHeldByCurrentThread(String key) {
        RLock lock = getLock(key);
        return lock.isHeldByCurrentThread();
    }

    @Override
    public int getHoldCount(String key) {
        RLock lock = getLock(key);
        return lock.getHoldCount();
    }

    @Override
    public boolean forceUnlock(String key) {
        RLock lock = getLock(key);
        try {
            boolean result = lock.forceUnlock();
            if (result) {
                log.warn("强制解锁成功，键为: {}", key);
            }
            return result;
        } catch (Exception e) {
            log.error("强制解锁失败，键为: {}", key, e);
            throw new RedisLockException(
                    RedisErrorCodes.LOCK_FORCE_UNLOCK_FAILED, "强制解锁失败，键为: " + key, e);
        }
    }

    /** 根据锁类型获取对应的锁实例 */
    private RLock getLockByType(String key, LockType lockType) {
        return switch (lockType) {
            case REENTRANT -> getLock(key);
            case FAIR -> getFairLock(key);
            case READ -> getReadWriteLock(key).readLock();
            case WRITE -> getReadWriteLock(key).writeLock();
            default ->
                    throw new RedisLockException(
                            RedisErrorCodes.UNSUPPORTED_LOCK_TYPE, "不支持的锁类型: " + lockType);
        };
    }

    /** 构建锁键 */
    private String buildLockKey(String key) {
        return properties.getKeyPrefix() + "lock:" + key;
    }

    /** 关闭锁管理器 */
    @PreDestroy
    public void shutdown() {
        log.info("正在关闭分布式锁管理器");
        if (recoveryHandler != null) {
            recoveryHandler.shutdown();
        }
        lockCache.clear();
    }
}
