package com.david.redis.commons.core.operations.lock;

import com.david.log.commons.LogUtils;
import com.david.redis.commons.core.operations.interfaces.RedisLock;
import com.david.redis.commons.exception.DistributedLockException;
import com.david.redis.commons.properties.RedisCommonsProperties;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁管理器
 *
 * <p>基于Redisson实现的分布式锁管理，提供锁的获取、释放和高级操作功能
 *
 * @author David
 */
@Component
@RequiredArgsConstructor
public class DistributedLockManager {

    private final RedissonClient redissonClient;
    private final RedisCommonsProperties redisCommonsProperties;

    /** 尝试获取分布式锁 */
    public RedisLock tryLock(String lockKey, Duration waitTime, Duration leaseTime) {
        validateLockParameters(lockKey, waitTime, leaseTime);

        String fullLockKey = buildLockKey(lockKey);
        RLock rLock = redissonClient.getLock(fullLockKey);

        try {
            LogUtils.business()
                    .auto()
                    .message("尝试获取分布式锁，锁键:{} 等待时间:{} 租约时间:{}", fullLockKey, waitTime, leaseTime)
                    .info();

            boolean acquired =
                    rLock.tryLock(waitTime.toMillis(), leaseTime.toMillis(), TimeUnit.MILLISECONDS);

            if (acquired) {
                LogUtils.business().auto().message("分布式锁获取成功，锁键:{}", fullLockKey).info();
                return new RedisLockImpl(rLock, fullLockKey);
            } else {
                LogUtils.business().auto().message("分布式锁获取超时，锁键:{}", fullLockKey).info();
                throw DistributedLockException.lockTimeout(fullLockKey, waitTime);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LogUtils.error("获取分布式锁被中断: {}", "lockKey: " + fullLockKey, e);
            throw new DistributedLockException("获取分布式锁被中断", e, fullLockKey, waitTime, leaseTime);
        } catch (Exception e) {
            LogUtils.error("获取分布式锁失败: {}", "lockKey: " + fullLockKey, e);
            throw new DistributedLockException("获取分布式锁失败", e, fullLockKey, waitTime, leaseTime);
        }
    }

    public RedisLock tryLock(String lockKey) {
        return tryLock(
                lockKey,
                redisCommonsProperties.getLock().getDefaultWaitTime(),
                redisCommonsProperties.getLock().getDefaultLeaseTime());
    }

    /** 在分布式锁保护下执行操作（有返回值） */
    public <T> T executeWithLock(
            String lockKey, Duration waitTime, Duration leaseTime, Supplier<T> action) {
        if (action == null) {
            throw new IllegalArgumentException("执行操作不能为null");
        }

        String fullLockKey = buildLockKey(lockKey);

        try (RedisLock lock = tryLock(lockKey, waitTime, leaseTime)) {
            LogUtils.business().auto().message("开始在分布式锁保护下执行操作，锁键:{}", fullLockKey).info();

            T result = action.get();

            LogUtils.business().auto().message("分布式锁保护下执行操作成功，锁键:{}", fullLockKey).info();
            return result;

        } catch (DistributedLockException e) {
            LogUtils.error("分布式锁获取失败: {}", "lockKey: " + fullLockKey, e);
            throw e;
        } catch (Exception e) {
            LogUtils.error("在分布式锁保护下执行操作失败: {}", "lockKey: " + fullLockKey, e);
            throw new DistributedLockException("在锁保护下执行操作失败", e, fullLockKey, waitTime, leaseTime);
        }
    }

    public void executeWithLock(
            String lockKey, Duration waitTime, Duration leaseTime, Runnable action) {
        if (action == null) {
            throw new IllegalArgumentException("执行操作不能为null");
        }
        executeWithLock(
                lockKey,
                waitTime,
                leaseTime,
                () -> {
                    action.run();
                    return null;
                });
    }

    public <T> T executeWithLock(String lockKey, Supplier<T> action) {
        return executeWithLock(
                lockKey,
                redisCommonsProperties.getLock().getDefaultWaitTime(),
                redisCommonsProperties.getLock().getDefaultLeaseTime(),
                action);
    }

    public void executeWithLock(String lockKey, Runnable action) {
        executeWithLock(
                lockKey,
                redisCommonsProperties.getLock().getDefaultWaitTime(),
                redisCommonsProperties.getLock().getDefaultLeaseTime(),
                action);
    }

    /** 检查锁是否存在 */
    public boolean isLockExists(String lockKey) {
        validateLockKey(lockKey);
        String fullLockKey = buildLockKey(lockKey);
        try {
            RLock rLock = redissonClient.getLock(fullLockKey);
            return rLock.isLocked();
        } catch (Exception e) {
            LogUtils.error("检查分布式锁存在性失败: {}", "lockKey: " + fullLockKey, e);
            return false;
        }
    }

    /** 带重试机制的锁执行方法（有返回值） */
    public <T> T executeWithLockRetry(
            String lockKey, Duration waitTime, Duration leaseTime, Supplier<T> action) {
        if (action == null) {
            throw new IllegalArgumentException("执行操作不能为null");
        }

        int maxRetries = redisCommonsProperties.getLock().getRetryAttempts();
        String fullLockKey = buildLockKey(lockKey);

        DistributedLockException lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                LogUtils.business()
                        .auto()
                        .message("尝试第{}次获取分布式锁，锁键:{}", attempt, fullLockKey)
                        .info();
                return executeWithLock(lockKey, waitTime, leaseTime, action);
            } catch (DistributedLockException e) {
                lastException = e;
                LogUtils.error("第{}次获取分布式锁失败: {}", "lockKey: " + fullLockKey, e);
                if (attempt < maxRetries) {
                    try {
                        long backoffMs = (long) (100 * Math.pow(2, attempt - 1));
                        Thread.sleep(Math.min(backoffMs, 1000));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new DistributedLockException(
                                "重试过程中被中断", ie, fullLockKey, waitTime, leaseTime);
                    }
                }
            }
        }

        throw new DistributedLockException(
                String.format("经过%d次重试后仍无法获取锁", maxRetries),
                lastException,
                fullLockKey,
                waitTime,
                leaseTime);
    }

    public void executeWithLockRetry(
            String lockKey, Duration waitTime, Duration leaseTime, Runnable action) {
        if (action == null) {
            throw new IllegalArgumentException("执行操作不能为null");
        }
        executeWithLockRetry(
                lockKey,
                waitTime,
                leaseTime,
                () -> {
                    action.run();
                    return null;
                });
    }

    public <T> T executeWithLockRetry(String lockKey, Supplier<T> action) {
        return executeWithLockRetry(
                lockKey,
                redisCommonsProperties.getLock().getDefaultWaitTime(),
                redisCommonsProperties.getLock().getDefaultLeaseTime(),
                action);
    }

    public void executeWithLockRetry(String lockKey, Runnable action) {
        executeWithLockRetry(
                lockKey,
                redisCommonsProperties.getLock().getDefaultWaitTime(),
                redisCommonsProperties.getLock().getDefaultLeaseTime(),
                action);
    }

    /** 尝试执行操作，如果无法获取锁则执行回退操作（有返回值） */
    public <T> T executeWithLockOrFallback(
            String lockKey,
            Duration waitTime,
            Duration leaseTime,
            Supplier<T> action,
            Supplier<T> fallbackAction) {
        if (action == null) {
            throw new IllegalArgumentException("主要操作不能为null");
        }
        if (fallbackAction == null) {
            throw new IllegalArgumentException("回退操作不能为null");
        }

        String fullLockKey = buildLockKey(lockKey);

        try {
            return executeWithLock(lockKey, waitTime, leaseTime, action);
        } catch (DistributedLockException e) {
            LogUtils.error("主要操作执行失败，执行回退操作: {}", "lockKey: " + fullLockKey, e);
            try {
                return fallbackAction.get();
            } catch (Exception fallbackException) {
                LogUtils.error("回退操作执行失败: {}", "lockKey: " + fullLockKey, fallbackException);
                throw new DistributedLockException(
                        "主要操作和回退操作都失败", fallbackException, fullLockKey, waitTime, leaseTime);
            }
        }
    }

    public void executeWithLockOrFallback(
            String lockKey,
            Duration waitTime,
            Duration leaseTime,
            Runnable action,
            Runnable fallbackAction) {
        if (action == null) {
            throw new IllegalArgumentException("主要操作不能为null");
        }
        if (fallbackAction == null) {
            throw new IllegalArgumentException("回退操作不能为null");
        }

        executeWithLockOrFallback(
                lockKey,
                waitTime,
                leaseTime,
                () -> {
                    action.run();
                    return null;
                },
                () -> {
                    fallbackAction.run();
                    return null;
                });
    }

    /** 强制释放锁 */
    public boolean forceUnlock(String lockKey) {
        validateLockKey(lockKey);
        String fullLockKey = buildLockKey(lockKey);
        try {
            RLock rLock = redissonClient.getLock(fullLockKey);
            boolean result = rLock.forceUnlock();

            if (result) {
                LogUtils.business().auto().message("强制释放分布式锁成功，锁键:{}", fullLockKey).info();
            } else {
                LogUtils.business().auto().message("强制释放分布式锁失败，锁可能不存在，锁键:{}", fullLockKey).info();
            }
            return result;
        } catch (Exception e) {
            LogUtils.error("强制释放分布式锁失败: {}", "lockKey: " + fullLockKey, e);
            return false;
        }
    }

    /** 构建完整的锁键名 */
    private String buildLockKey(String lockKey) {
        String prefix = redisCommonsProperties.getLock().getKeyPrefix();
        if (StringUtils.hasText(prefix)) {
            return prefix + lockKey;
        }
        return lockKey;
    }

    /** 验证锁参数 */
    private void validateLockParameters(String lockKey, Duration waitTime, Duration leaseTime) {
        validateLockKey(lockKey);

        if (waitTime == null || waitTime.isNegative()) {
            throw new IllegalArgumentException("等待时间不能为null或负数");
        }

        if (leaseTime == null || leaseTime.isNegative() || leaseTime.isZero()) {
            throw new IllegalArgumentException("租约时间不能为null、负数或零");
        }

        Duration maxLeaseTime = Duration.ofMinutes(10);
        if (leaseTime.compareTo(maxLeaseTime) > 0) {
            LogUtils.business()
                    .auto()
                    .message("警告: 租约时间过长:{}，建议不超过:{}", leaseTime, maxLeaseTime)
                    .info();
        }
    }

    /** 验证锁键名 */
    private void validateLockKey(String lockKey) {
        if (!StringUtils.hasText(lockKey)) {
            throw new IllegalArgumentException("锁键名不能为空");
        }
        if (lockKey.length() > 250) {
            throw new IllegalArgumentException("锁键名过长，不能超过250个字符");
        }
        if (lockKey.contains(" ")
                || lockKey.contains("\n")
                || lockKey.contains("\r")
                || lockKey.contains("\t")) {
            throw new IllegalArgumentException("锁键名不能包含空格、换行符或制表符");
        }
    }
}
