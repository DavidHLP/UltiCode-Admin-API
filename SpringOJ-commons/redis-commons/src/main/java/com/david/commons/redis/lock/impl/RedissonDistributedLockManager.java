package com.david.commons.redis.lock.impl;

import com.david.commons.redis.config.RedisCommonsProperties;
import com.david.commons.redis.exception.RedisErrorCodes;
import com.david.commons.redis.exception.RedisLockException;
import com.david.commons.redis.lock.DistributedLockManager;
import com.david.commons.redis.lock.LockRecoveryHandler;
import com.david.commons.redis.lock.LockRetryStrategy;
import com.david.commons.redis.lock.LockType;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
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

    public RedissonDistributedLockManager(RedissonClient redissonClient, RedisCommonsProperties properties) {
        this.redissonClient = redissonClient;
        this.properties = properties;
        this.retryStrategy = LockRetryStrategy.exponentialBackoff();
        this.recoveryHandler = new LockRecoveryHandler();
    }

    @Override
    public RLock getLock(String key) {
        String lockKey = buildLockKey(key);
        return lockCache.computeIfAbsent(lockKey, k -> redissonClient.getLock(k));
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
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit, LockType lockType) {
        return tryLockWithRetry(key, waitTime, leaseTime, unit, lockType, 3);
    }

    /**
     * 带重试机制的锁获取
     */
    public boolean tryLockWithRetry(String key, long waitTime, long leaseTime, TimeUnit unit, LockType lockType,
            int maxAttempts) {
        RLock lock = getLockByType(key, lockType);
        long startTime = System.currentTimeMillis();
        long maxWaitTimeMs = unit.toMillis(waitTime);

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                long elapsedTime = System.currentTimeMillis() - startTime;
                if (elapsedTime >= maxWaitTimeMs) {
                    log.warn("Lock acquisition timeout for key: {} after {}ms", key, elapsedTime);
                    break;
                }

                // 计算本次尝试的等待时间
                long remainingTime = maxWaitTimeMs - elapsedTime;
                long attemptWaitTime = Math.min(remainingTime, unit.toMillis(waitTime) / maxAttempts);

                boolean acquired = lock.tryLock(attemptWaitTime, leaseTime, TimeUnit.MILLISECONDS);
                if (acquired) {
                    log.debug("Successfully acquired {} lock for key: {} on attempt {}", lockType, key, attempt);
                    // 注册到恢复处理器
                    recoveryHandler.registerLock(key, lock, leaseTime, unit);
                    return true;
                }

                // 检查是否应该重试
                elapsedTime = System.currentTimeMillis() - startTime;
                if (!retryStrategy.shouldRetry(attempt, maxAttempts, elapsedTime, maxWaitTimeMs)) {
                    break;
                }

                // 计算重试延迟
                if (attempt < maxAttempts) {
                    long retryDelay = retryStrategy.calculateDelay(attempt,
                            properties.getLock().getRetryInterval(), TimeUnit.MILLISECONDS);

                    log.debug("Lock acquisition failed for key: {}, retrying in {}ms (attempt {}/{})",
                            key, retryDelay, attempt, maxAttempts);

                    Thread.sleep(retryDelay);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RedisLockException(RedisErrorCodes.LOCK_INTERRUPTED,
                        "Lock acquisition interrupted for key: " + key, e);
            } catch (Exception e) {
                log.warn("Exception during lock acquisition attempt {} for key: {}", attempt, key, e);
                if (attempt == maxAttempts) {
                    throw new RedisLockException(RedisErrorCodes.LOCK_ACQUISITION_FAILED,
                            "Failed to acquire lock for key: " + key + " after " + maxAttempts + " attempts", e);
                }
            }
        }

        log.warn("Failed to acquire {} lock for key: {} after {} attempts", lockType, key, maxAttempts);
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
                log.debug("Successfully released {} lock for key: {}", lockType, key);
            } else {
                log.warn("Attempted to unlock {} lock not held by current thread for key: {}", lockType, key);
            }
        } catch (Exception e) {
            log.error("Failed to release {} lock for key: {}", lockType, key, e);

            // 尝试恢复
            boolean recovered = recoveryHandler.tryRecoverLock(key, lock, e);
            if (!recovered) {
                throw new RedisLockException(RedisErrorCodes.LOCK_RELEASE_FAILED,
                        "Failed to release lock for key: " + key, e);
            }
        }
    }

    @Override
    public <T> T executeWithLock(String key, Supplier<T> supplier, long waitTime, long leaseTime, TimeUnit unit) {
        return executeWithLock(key, supplier, waitTime, leaseTime, unit, LockType.REENTRANT);
    }

    @Override
    public <T> T executeWithLock(String key, Supplier<T> supplier, long waitTime, long leaseTime, TimeUnit unit,
            LockType lockType) {
        RLock lock = getLockByType(key, lockType);
        boolean acquired = false;

        try {
            acquired = lock.tryLock(waitTime, leaseTime, unit);
            if (!acquired) {
                throw new RedisLockException(RedisErrorCodes.LOCK_TIMEOUT,
                        "Failed to acquire lock within timeout for key: " + key);
            }

            log.debug("Executing operation with {} lock for key: {}", lockType, key);
            return supplier.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RedisLockException(RedisErrorCodes.LOCK_INTERRUPTED,
                    "Lock acquisition interrupted for key: " + key, e);
        } catch (RedisLockException e) {
            throw e;
        } catch (Exception e) {
            throw new RedisLockException(RedisErrorCodes.LOCK_OPERATION_FAILED,
                    "Operation failed while holding lock for key: " + key, e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                try {
                    lock.unlock();
                    log.debug("Released {} lock after operation for key: {}", lockType, key);
                } catch (Exception e) {
                    log.error("Failed to release lock after operation for key: {}", key, e);
                }
            }
        }
    }

    @Override
    public void executeWithLock(String key, Runnable runnable, long waitTime, long leaseTime, TimeUnit unit) {
        executeWithLock(key, runnable, waitTime, leaseTime, unit, LockType.REENTRANT);
    }

    @Override
    public void executeWithLock(String key, Runnable runnable, long waitTime, long leaseTime, TimeUnit unit,
            LockType lockType) {
        executeWithLock(key, () -> {
            runnable.run();
            return null;
        }, waitTime, leaseTime, unit, lockType);
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
                log.warn("Force unlocked key: {}", key);
            }
            return result;
        } catch (Exception e) {
            log.error("Failed to force unlock key: {}", key, e);
            throw new RedisLockException(RedisErrorCodes.LOCK_FORCE_UNLOCK_FAILED,
                    "Failed to force unlock key: " + key, e);
        }
    }

    /**
     * 根据锁类型获取对应的锁实例
     */
    private RLock getLockByType(String key, LockType lockType) {
        return switch (lockType) {
            case REENTRANT -> getLock(key);
            case FAIR -> getFairLock(key);
            case READ -> getReadWriteLock(key).readLock();
            case WRITE -> getReadWriteLock(key).writeLock();
            default -> throw new RedisLockException(RedisErrorCodes.UNSUPPORTED_LOCK_TYPE,
                    "Unsupported lock type: " + lockType);
        };
    }

    /**
     * 构建锁键
     */
    private String buildLockKey(String key) {
        return properties.getKeyPrefix() + "lock:" + key;
    }

    /**
     * 关闭锁管理器
     */
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down distributed lock manager");
        if (recoveryHandler != null) {
            recoveryHandler.shutdown();
        }
        lockCache.clear();
    }
}