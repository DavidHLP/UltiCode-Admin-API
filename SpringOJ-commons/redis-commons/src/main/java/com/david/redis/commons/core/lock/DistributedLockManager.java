package com.david.redis.commons.core.lock;

import com.david.redis.commons.core.interfaces.RedisLock;
import com.david.redis.commons.exception.DistributedLockException;
import com.david.redis.commons.properties.RedisCommonsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockManager {

    private final RedissonClient redissonClient;
    private final RedisCommonsProperties redisCommonsProperties;

    /**
     * 尝试获取分布式锁
     *
     * @param lockKey 锁键名
     * @param waitTime 等待时间
     * @param leaseTime 租约时间
     * @return RedisLock实例，如果获取失败返回null
     * @throws DistributedLockException 当锁操作失败时抛出
     */
    public RedisLock tryLock(String lockKey, Duration waitTime, Duration leaseTime) {
        validateLockParameters(lockKey, waitTime, leaseTime);

        String fullLockKey = buildLockKey(lockKey);
        RLock rLock = redissonClient.getLock(fullLockKey);

        try {
            log.debug("尝试获取分布式锁: {}, 等待时间: {}, 租约时间: {}", fullLockKey, waitTime, leaseTime);

            boolean acquired =
                    rLock.tryLock(waitTime.toMillis(), leaseTime.toMillis(), TimeUnit.MILLISECONDS);

            if (acquired) {
                log.debug("成功获取分布式锁: {}", fullLockKey);
                return new RedisLockImpl(rLock, fullLockKey);
            } else {
                log.debug("获取分布式锁超时: {}", fullLockKey);
                throw DistributedLockException.lockTimeout(fullLockKey, waitTime);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断: {}", fullLockKey, e);
            throw new DistributedLockException("获取分布式锁被中断", e, fullLockKey, waitTime, leaseTime);
        } catch (Exception e) {
            log.error("获取分布式锁异常: {}", fullLockKey, e);
            throw new DistributedLockException("获取分布式锁失败", e, fullLockKey, waitTime, leaseTime);
        }
    }

    /**
     * 使用默认配置尝试获取分布式锁
     *
     * @param lockKey 锁键名
     * @return RedisLock实例，如果获取失败返回null
     */
    public RedisLock tryLock(String lockKey) {
        return tryLock(
                lockKey,
                redisCommonsProperties.getLock().getDefaultWaitTime(),
                redisCommonsProperties.getLock().getDefaultLeaseTime());
    }

    /**
     * 在分布式锁保护下执行操作（有返回值）
     *
     * @param <T> 返回值类型
     * @param lockKey 锁键名
     * @param waitTime 等待时间
     * @param leaseTime 租约时间
     * @param action 要执行的操作
     * @return 操作的返回值
     * @throws DistributedLockException 当锁操作失败时抛出
     */
    public <T> T executeWithLock(
            String lockKey, Duration waitTime, Duration leaseTime, Supplier<T> action) {
        if (action == null) {
            throw new IllegalArgumentException("执行操作不能为null");
        }

        String fullLockKey = buildLockKey(lockKey);
        RedisLock lock = null;

        try {
            // 获取锁
            lock = tryLock(lockKey, waitTime, leaseTime);
            log.debug("在锁保护下执行操作: {}", fullLockKey);

            // 执行业务操作
            T result = action.get();

            log.debug("锁保护下的操作执行成功: {}", fullLockKey);
            return result;

        } catch (DistributedLockException e) {
            log.error("获取分布式锁失败: {}", fullLockKey, e);
            throw e;
        } catch (Exception e) {
            log.error("在锁保护下执行操作失败: {}", fullLockKey, e);
            throw new DistributedLockException("在锁保护下执行操作失败", e, fullLockKey, waitTime, leaseTime);
        } finally {
            // 确保锁被正确释放
            if (lock != null) {
                try {
                    lock.close();
                } catch (Exception e) {
                    log.error("释放锁时发生异常: {}", fullLockKey, e);
                }
            }
        }
    }

    /**
     * 在分布式锁保护下执行操作（无返回值）
     *
     * @param lockKey 锁键名
     * @param waitTime 等待时间
     * @param leaseTime 租约时间
     * @param action 要执行的操作
     * @throws DistributedLockException 当锁操作失败时抛出
     */
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

    /**
     * 使用默认配置在分布式锁保护下执行操作（有返回值）
     *
     * @param <T> 返回值类型
     * @param lockKey 锁键名
     * @param action 要执行的操作
     * @return 操作的返回值
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> action) {
        return executeWithLock(
                lockKey,
                redisCommonsProperties.getLock().getDefaultWaitTime(),
                redisCommonsProperties.getLock().getDefaultLeaseTime(),
                action);
    }

    /**
     * 使用默认配置在分布式锁保护下执行操作（无返回值）
     *
     * @param lockKey 锁键名
     * @param action 要执行的操作
     */
    public void executeWithLock(String lockKey, Runnable action) {
        executeWithLock(
                lockKey,
                redisCommonsProperties.getLock().getDefaultWaitTime(),
                redisCommonsProperties.getLock().getDefaultLeaseTime(),
                action);
    }

    /**
     * 检查锁是否存在
     *
     * @param lockKey 锁键名
     * @return true如果锁存在，否则返回false
     */
    public boolean isLockExists(String lockKey) {
        validateLockKey(lockKey);

        String fullLockKey = buildLockKey(lockKey);
        try {
            RLock rLock = redissonClient.getLock(fullLockKey);
            return rLock.isLocked();
        } catch (Exception e) {
            log.error("检查锁是否存在失败: {}", fullLockKey, e);
            return false;
        }
    }

    /**
     * 带重试机制的锁执行方法（有返回值）
     *
     * @param <T> 返回值类型
     * @param lockKey 锁键名
     * @param waitTime 等待时间
     * @param leaseTime 租约时间
     * @param action 要执行的操作
     * @return 操作的返回值
     * @throws DistributedLockException 当所有重试都失败时抛出
     */
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
                log.debug("尝试获取锁并执行操作，第{}次尝试: {}", attempt, fullLockKey);
                return executeWithLock(lockKey, waitTime, leaseTime, action);

            } catch (DistributedLockException e) {
                lastException = e;
                log.warn("第{}次尝试获取锁失败: {}, 错误: {}", attempt, fullLockKey, e.getMessage());

                if (attempt < maxRetries) {
                    try {
                        // 指数退避策略
                        long backoffMs = (long) (100 * Math.pow(2, attempt - 1));
                        Thread.sleep(Math.min(backoffMs, 1000)); // 最大等待1秒
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new DistributedLockException(
                                "重试过程中被中断", ie, fullLockKey, waitTime, leaseTime);
                    }
                }
            }
        }

        log.error("所有重试都失败，无法获取锁: {}, 重试次数: {}", fullLockKey, maxRetries);
        throw new DistributedLockException(
                String.format("经过%d次重试后仍无法获取锁", maxRetries),
                lastException,
                fullLockKey,
                waitTime,
                leaseTime);
    }

    /**
     * 带重试机制的锁执行方法（无返回值）
     *
     * @param lockKey 锁键名
     * @param waitTime 等待时间
     * @param leaseTime 租约时间
     * @param action 要执行的操作
     * @throws DistributedLockException 当所有重试都失败时抛出
     */
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

    /**
     * 使用默认配置带重试机制的锁执行方法（有返回值）
     *
     * @param <T> 返回值类型
     * @param lockKey 锁键名
     * @param action 要执行的操作
     * @return 操作的返回值
     */
    public <T> T executeWithLockRetry(String lockKey, Supplier<T> action) {
        return executeWithLockRetry(
                lockKey,
                redisCommonsProperties.getLock().getDefaultWaitTime(),
                redisCommonsProperties.getLock().getDefaultLeaseTime(),
                action);
    }

    /**
     * 使用默认配置带重试机制的锁执行方法（无返回值）
     *
     * @param lockKey 锁键名
     * @param action 要执行的操作
     */
    public void executeWithLockRetry(String lockKey, Runnable action) {
        executeWithLockRetry(
                lockKey,
                redisCommonsProperties.getLock().getDefaultWaitTime(),
                redisCommonsProperties.getLock().getDefaultLeaseTime(),
                action);
    }

    /**
     * 尝试执行操作，如果无法获取锁则执行回退操作（有返回值）
     *
     * @param <T> 返回值类型
     * @param lockKey 锁键名
     * @param waitTime 等待时间
     * @param leaseTime 租约时间
     * @param action 主要操作
     * @param fallbackAction 回退操作
     * @return 操作的返回值
     */
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
            log.warn("无法获取锁，执行回退操作: {}, 错误: {}", fullLockKey, e.getMessage());
            try {
                return fallbackAction.get();
            } catch (Exception fallbackException) {
                log.error("回退操作也失败了: {}", fullLockKey, fallbackException);
                throw new DistributedLockException(
                        "主要操作和回退操作都失败", fallbackException, fullLockKey, waitTime, leaseTime);
            }
        }
    }

    /**
     * 尝试执行操作，如果无法获取锁则执行回退操作（无返回值）
     *
     * @param lockKey 锁键名
     * @param waitTime 等待时间
     * @param leaseTime 租约时间
     * @param action 主要操作
     * @param fallbackAction 回退操作
     */
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

    /**
     * 强制释放锁（管理员操作） 注意：这个操作会强制释放锁，即使不是当前线程持有的锁
     *
     * @param lockKey 锁键名
     * @return true如果释放成功，否则返回false
     */
    public boolean forceUnlock(String lockKey) {
        validateLockKey(lockKey);

        String fullLockKey = buildLockKey(lockKey);
        try {
            RLock rLock = redissonClient.getLock(fullLockKey);
            boolean result = rLock.forceUnlock();

            if (result) {
                log.warn("强制释放锁成功: {}", fullLockKey);
            } else {
                log.warn("强制释放锁失败，锁可能不存在: {}", fullLockKey);
            }

            return result;
        } catch (Exception e) {
            log.error("强制释放锁异常: {}", fullLockKey, e);
            return false;
        }
    }

    /**
     * 构建完整的锁键名
     *
     * @param lockKey 原始锁键名
     * @return 带前缀的完整锁键名
     */
    private String buildLockKey(String lockKey) {
        String prefix = redisCommonsProperties.getLock().getKeyPrefix();
        if (StringUtils.hasText(prefix)) {
            return prefix + lockKey;
        }
        return lockKey;
    }

    /**
     * 验证锁参数
     *
     * @param lockKey 锁键名
     * @param waitTime 等待时间
     * @param leaseTime 租约时间
     */
    private void validateLockParameters(String lockKey, Duration waitTime, Duration leaseTime) {
        validateLockKey(lockKey);

        if (waitTime == null || waitTime.isNegative()) {
            throw new IllegalArgumentException("等待时间不能为null或负数");
        }

        if (leaseTime == null || leaseTime.isNegative() || leaseTime.isZero()) {
            throw new IllegalArgumentException("租约时间不能为null、负数或零");
        }

        // 检查租约时间是否过长（防止死锁）
        Duration maxLeaseTime = Duration.ofMinutes(10);
        if (leaseTime.compareTo(maxLeaseTime) > 0) {
            log.warn("租约时间过长: {}, 建议不超过: {}", leaseTime, maxLeaseTime);
        }
    }

    /**
     * 验证锁键名
     *
     * @param lockKey 锁键名
     */
    private void validateLockKey(String lockKey) {
        if (!StringUtils.hasText(lockKey)) {
            throw new IllegalArgumentException("锁键名不能为空");
        }

        // 检查键名长度
        if (lockKey.length() > 250) {
            throw new IllegalArgumentException("锁键名过长，不能超过250个字符");
        }

        // 检查键名是否包含非法字符
        if (lockKey.contains(" ")
                || lockKey.contains("\n")
                || lockKey.contains("\r")
                || lockKey.contains("\t")) {
            throw new IllegalArgumentException("锁键名不能包含空格、换行符或制表符");
        }
    }
}
