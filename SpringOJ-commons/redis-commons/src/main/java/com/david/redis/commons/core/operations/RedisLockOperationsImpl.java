package com.david.redis.commons.core.operations;

import com.david.log.commons.LogUtils;
import com.david.redis.commons.core.operations.lock.DistributedLockManager;
import com.david.redis.commons.core.operations.interfaces.RedisLock;
import com.david.redis.commons.core.operations.interfaces.RedisLockOperations;
import com.david.redis.commons.core.operations.abstracts.AbstractRedisOperations;
import com.david.redis.commons.core.operations.support.RedisOperationExecutor;
import com.david.redis.commons.core.operations.enums.RedisOperationType;
import com.david.redis.commons.core.operations.support.RedisResultProcessor;
import com.david.redis.commons.properties.RedisCommonsProperties;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Redis 分布式锁操作实现类
 *
 * <p>实现所有分布式锁相关的操作方法，继承抽象基类以复用通用逻辑
 *
 * @author David
 */
@Component
public class RedisLockOperationsImpl extends AbstractRedisOperations
        implements RedisLockOperations {

    private final DistributedLockManager distributedLockManager;
    private final RedissonClient redissonClient;
    private final RedisCommonsProperties redisCommonsProperties;

    public RedisLockOperationsImpl(
            RedisTemplate<String, Object> redisTemplate,
            RedisOperationExecutor executor,
            RedisResultProcessor resultProcessor,
            DistributedLockManager distributedLockManager,
            RedissonClient redissonClient,
            RedisCommonsProperties redisCommonsProperties) {
        super(redisTemplate, executor, resultProcessor);
        this.distributedLockManager = distributedLockManager;
        this.redissonClient = redissonClient;
        this.redisCommonsProperties = redisCommonsProperties;
    }

    @Override
    public RedisLock tryLock(String lockKey, Duration waitTime, Duration leaseTime) {
        return executeOperation(
                RedisOperationType.TRY_LOCK,
                lockKey,
                new Object[] {waitTime, leaseTime},
                RedisLock.class,
                () -> distributedLockManager.tryLock(lockKey, waitTime, leaseTime));
    }

    @Override
    public RedisLock tryLock(String lockKey) {
        return executeOperation(
                RedisOperationType.TRY_LOCK_DEFAULT,
                lockKey,
                RedisLock.class,
                () -> distributedLockManager.tryLock(lockKey));
    }

    @Override
    public <T> T executeWithLock(
            String lockKey, Duration waitTime, Duration leaseTime, Supplier<T> action) {
        return executeOperation(
                "EXECUTE_WITH_LOCK",
                lockKey,
                () -> {
                    return distributedLockManager.executeWithLock(
                            lockKey, waitTime, leaseTime, action);
                });
    }

    @Override
    public void executeWithLock(
            String lockKey, Duration waitTime, Duration leaseTime, Runnable action) {
        executeOperation(
                "EXECUTE_WITH_LOCK_VOID",
                lockKey,
                () -> {
                    distributedLockManager.executeWithLock(lockKey, waitTime, leaseTime, action);
                    return null;
                });
    }

    @Override
    public <T> T executeWithLock(String lockKey, Supplier<T> action) {
        return executeOperation(
                "EXECUTE_WITH_LOCK_DEFAULT",
                lockKey,
                () -> {
                    return distributedLockManager.executeWithLock(lockKey, action);
                });
    }

    @Override
    public void executeWithLock(String lockKey, Runnable action) {
        executeOperation(
                "EXECUTE_WITH_LOCK_DEFAULT_VOID",
                lockKey,
                () -> {
                    distributedLockManager.executeWithLock(lockKey, action);
                    return null;
                });
    }

    @Override
    public <T> T executeWithLockRetry(
            String lockKey, Duration waitTime, Duration leaseTime, Supplier<T> action) {
        return executeOperation(
                "EXECUTE_WITH_LOCK_RETRY",
                lockKey,
                () -> {
                    return distributedLockManager.executeWithLockRetry(
                            lockKey, waitTime, leaseTime, action);
                });
    }

    @Override
    public void executeWithLockRetry(
            String lockKey, Duration waitTime, Duration leaseTime, Runnable action) {
        executeOperation(
                "EXECUTE_WITH_LOCK_RETRY_VOID",
                lockKey,
                () -> {
                    distributedLockManager.executeWithLockRetry(
                            lockKey, waitTime, leaseTime, action);
                    return null;
                });
    }

    @Override
    public <T> T executeWithLockRetry(String lockKey, Supplier<T> action) {
        return executeOperation(
                "EXECUTE_WITH_LOCK_RETRY_DEFAULT",
                lockKey,
                () -> {
                    return distributedLockManager.executeWithLockRetry(lockKey, action);
                });
    }

    @Override
    public void executeWithLockRetry(String lockKey, Runnable action) {
        executeOperation(
                "EXECUTE_WITH_LOCK_RETRY_DEFAULT_VOID",
                lockKey,
                () -> {
                    distributedLockManager.executeWithLockRetry(lockKey, action);
                    return null;
                });
    }

    @Override
    public <T> T executeWithLockOrFallback(
            String lockKey,
            Duration waitTime,
            Duration leaseTime,
            Supplier<T> action,
            Supplier<T> fallbackAction) {
        return executeOperation(
                "EXECUTE_WITH_LOCK_OR_FALLBACK",
                lockKey,
                () -> {
                    return distributedLockManager.executeWithLockOrFallback(
                            lockKey, waitTime, leaseTime, action, fallbackAction);
                });
    }

    @Override
    public void executeWithLockOrFallback(
            String lockKey,
            Duration waitTime,
            Duration leaseTime,
            Runnable action,
            Runnable fallbackAction) {
        executeOperation(
                "EXECUTE_WITH_LOCK_OR_FALLBACK_VOID",
                lockKey,
                () -> {
                    distributedLockManager.executeWithLockOrFallback(
                            lockKey, waitTime, leaseTime, action, fallbackAction);
                    return null;
                });
    }

    @Override
    public <T> T executeWithLockOrFallback(
            String lockKey, Supplier<T> action, Supplier<T> fallbackAction) {
        return executeOperation(
                "EXECUTE_WITH_LOCK_OR_FALLBACK_DEFAULT",
                lockKey,
                () -> {
                    Duration defaultWaitTime =
                            redisCommonsProperties.getLock().getDefaultWaitTime();
                    Duration defaultLeaseTime =
                            redisCommonsProperties.getLock().getDefaultLeaseTime();
                    return distributedLockManager.executeWithLockOrFallback(
                            lockKey, defaultWaitTime, defaultLeaseTime, action, fallbackAction);
                });
    }

    @Override
    public void executeWithLockOrFallback(
            String lockKey, Runnable action, Runnable fallbackAction) {
        executeOperation(
                "EXECUTE_WITH_LOCK_OR_FALLBACK_DEFAULT_VOID",
                lockKey,
                () -> {
                    Duration defaultWaitTime =
                            redisCommonsProperties.getLock().getDefaultWaitTime();
                    Duration defaultLeaseTime =
                            redisCommonsProperties.getLock().getDefaultLeaseTime();
                    distributedLockManager.executeWithLockOrFallback(
                            lockKey, defaultWaitTime, defaultLeaseTime, action, fallbackAction);
                    return null;
                });
    }

    @Override
    public boolean isLockExists(String lockKey) {
        return executeBooleanOperation(
                RedisOperationType.IS_LOCK_EXISTS,
                lockKey,
                () -> distributedLockManager.isLockExists(lockKey));
    }

    @Override
    public boolean forceUnlock(String lockKey) {
        return executeBooleanOperation(
                RedisOperationType.FORCE_UNLOCK,
                lockKey,
                () -> distributedLockManager.forceUnlock(lockKey));
    }

    @Override
    public long getRemainingTimeToLive(String lockKey) {
        return executeLongOperation(
                RedisOperationType.GET_REMAINING_TTL,
                lockKey,
                () -> {
                    validateLockKey(lockKey);
                    String fullLockKey = buildLockKey(lockKey);

                    try {
                        RLock rLock = redissonClient.getLock(fullLockKey);
                        return rLock.remainTimeToLive();
                    } catch (Exception e) {
                        LogUtils.error("获取锁剩余租约时间失败: " + "lockKey: " + fullLockKey, e);
                        return -1L;
                    }
                });
    }

    @Override
    public boolean isHeldByCurrentThread(String lockKey) {
        return executeOperation(
                "IS_HELD_BY_CURRENT_THREAD",
                lockKey,
                () -> {
                    validateLockKey(lockKey);
                    String fullLockKey = buildLockKey(lockKey);

                    try {
                        RLock rLock = redissonClient.getLock(fullLockKey);
                        return rLock.isHeldByCurrentThread();
                    } catch (Exception e) {
                        LogUtils.error("检查当前线程是否持有锁失败: " + "lockKey: " + fullLockKey, e);
                        return false;
                    }
                });
    }

    @Override
    public int getHoldCount(String lockKey) {
        return executeOperation(
                "GET_HOLD_COUNT",
                lockKey,
                () -> {
                    validateLockKey(lockKey);
                    String fullLockKey = buildLockKey(lockKey);

                    try {
                        RLock rLock = redissonClient.getLock(fullLockKey);
                        return rLock.getHoldCount();
                    } catch (Exception e) {
                        LogUtils.error("获取锁持有计数失败: " + "lockKey: " + fullLockKey, e);
                        return 0;
                    }
                });
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
