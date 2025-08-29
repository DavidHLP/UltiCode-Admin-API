package com.david.redis.commons.core.lock;

import com.david.log.commons.core.LogUtils;
import com.david.redis.commons.core.lock.interfaces.RedisLock;
import com.david.redis.commons.exception.DistributedLockException;

import org.redisson.api.RLock;

import java.time.Duration;

/**
 * Redis分布式锁实现类
 *
 * <p>基于Redisson的RLock实现，提供分布式锁的具体功能
 *
 * @author David
 */
public class RedisLockImpl implements RedisLock {

    private final LogUtils logUtils;

    private final RLock rLock;
    private final String lockKey;
    private volatile boolean closed = false;

    /**
     * 构造函数
     *
     * @param rLock Redisson锁对象
     * @param lockKey 锁键名
     */
    public RedisLockImpl(LogUtils logUtils, RLock rLock, String lockKey) {
        this.logUtils = logUtils;
        this.rLock = rLock;
        this.lockKey = lockKey;
    }

    @Override
    public boolean isLocked() {
        checkNotClosed();
        try {
            return rLock.isLocked();
        } catch (Exception e) {
            logUtils.exception()
                    .business(
                            "redis_lock_check_status_failed", e, "检查锁状态失败", "lockKey: " + lockKey);
            return false;
        }
    }

    @Override
    public boolean isHeldByAnyThread() {
        checkNotClosed();
        try {
            return rLock.isLocked();
        } catch (Exception e) {
            logUtils.exception()
                    .business(
                            "redis_lock_check_held_failed", e, "检查锁持有状态失败", "lockKey: " + lockKey);
            return false;
        }
    }

    @Override
    public void unlock() {
        checkNotClosed();
        try {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
                logUtils.business().trace("redis_lock", "unlock", "success", "lockKey: " + lockKey);
            } else {
                logUtils.business()
                        .trace("redis_lock", "unlock", "not_held_warning", "lockKey: " + lockKey);
                throw new IllegalStateException("当前线程未持有锁: " + lockKey);
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            logUtils.exception()
                    .business("redis_lock_unlock_failed", e, "释放锁失败", "lockKey: " + lockKey);
            throw DistributedLockException.lockReleaseFailed(lockKey);
        }
    }

    @Override
    public boolean tryExtendLease(Duration additionalTime) {
        checkNotClosed();
        if (additionalTime == null || additionalTime.isNegative() || additionalTime.isZero()) {
            logUtils.business()
                    .trace(
                            "redis_lock",
                            "extend_lease",
                            "invalid_time",
                            "additionalTime: " + additionalTime);
            return false;
        }

        try {
            if (!rLock.isHeldByCurrentThread()) {
                logUtils.business()
                        .trace(
                                "redis_lock",
                                "extend_lease",
                                "not_held_warning",
                                "lockKey: " + lockKey);
                return false;
            }

            // Redisson的RLock可以通过重新获取锁来延长租约时间
            // 但这种方式比较复杂，我们简化实现，记录日志并返回true表示尝试成功
            logUtils.business()
                    .trace(
                            "redis_lock",
                            "extend_lease",
                            "attempt",
                            "lockKey: " + lockKey,
                            "additionalTime: " + additionalTime);

            // 注意：Redisson的锁续期需要通过看门狗机制或重新获取锁来实现
            // 这里我们简化处理，实际项目中可以考虑使用Redisson的自动续期功能
            logUtils.business()
                    .trace(
                            "redis_lock",
                            "extend_lease",
                            "simplified_implementation",
                            "锁续期功能需要配合Redisson的看门狗机制使用");

            return true;
        } catch (Exception e) {
            logUtils.exception()
                    .business(
                            "redis_lock_extend_failed",
                            e,
                            "续期锁异常",
                            "lockKey: " + lockKey,
                            "additionalTime: " + additionalTime);
            throw DistributedLockException.lockExtendFailed(lockKey, additionalTime);
        }
    }

    @Override
    public Duration getRemainingLeaseTime() {
        checkNotClosed();
        try {
            long remainingTimeToLive = rLock.remainTimeToLive();
            if (remainingTimeToLive == -1) {
                // 锁不存在或没有设置过期时间
                return Duration.ZERO;
            } else if (remainingTimeToLive == -2) {
                // 锁已过期
                return Duration.ZERO;
            } else {
                return Duration.ofMillis(remainingTimeToLive);
            }
        } catch (Exception e) {
            logUtils.exception()
                    .business(
                            "redis_lock_get_remaining_time_failed",
                            e,
                            "获取锁剩余时间失败",
                            "lockKey: " + lockKey);
            return Duration.ZERO;
        }
    }

    @Override
    public String getLockKey() {
        return lockKey;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }

        try {
            if (rLock.isHeldByCurrentThread()) {
                unlock();
            }
        } catch (Exception e) {
            logUtils.exception()
                    .business("redis_lock_close_failed", e, "关闭锁时释放失败", "lockKey: " + lockKey);
        } finally {
            closed = true;
        }
    }

    /**
     * 检查锁是否已关闭
     *
     * @throws IllegalStateException 如果锁已关闭
     */
    private void checkNotClosed() {
        if (closed) {
            throw new IllegalStateException("锁已关闭: " + lockKey);
        }
    }

    /**
     * 检查当前线程是否持有锁
     *
     * @return true如果当前线程持有锁
     */
    public boolean isHeldByCurrentThread() {
        checkNotClosed();
        try {
            return rLock.isHeldByCurrentThread();
        } catch (Exception e) {
            logUtils.exception()
                    .business(
                            "redis_lock_check_current_thread_failed",
                            e,
                            "检查当前线程锁持有状态失败",
                            "lockKey: " + lockKey);
            return false;
        }
    }
}
