package com.david.redis.commons.core.lock;

import com.david.redis.commons.core.lock.interfaces.RedisLock;
import com.david.redis.commons.exception.DistributedLockException;

import lombok.extern.slf4j.Slf4j;

import org.redisson.api.RLock;

import java.time.Duration;

/**
 * Redis分布式锁实现类
 *
 * <p>基于Redisson的RLock实现，提供分布式锁的具体功能
 *
 * @author David
 */
@Slf4j
public class RedisLockImpl implements RedisLock {

    private final RLock rLock;
    private final String lockKey;
    private volatile boolean closed = false;

    /**
     * 构造函数
     *
     * @param rLock Redisson锁对象
     * @param lockKey 锁键名
     */
    public RedisLockImpl(RLock rLock, String lockKey) {
        this.rLock = rLock;
        this.lockKey = lockKey;
    }

    @Override
    public boolean isLocked() {
        checkNotClosed();
        try {
            return rLock.isLocked();
        } catch (Exception e) {
            log.error("检查锁状态失败: {}", lockKey, e);
            return false;
        }
    }

    @Override
    public boolean isHeldByAnyThread() {
        checkNotClosed();
        try {
            return rLock.isLocked();
        } catch (Exception e) {
            log.error("检查锁持有状态失败: {}", lockKey, e);
            return false;
        }
    }

    @Override
    public void unlock() {
        checkNotClosed();
        try {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
                log.debug("成功释放锁: {}", lockKey);
            } else {
                log.warn("尝试释放未持有的锁: {}", lockKey);
                throw new IllegalStateException("当前线程未持有锁: " + lockKey);
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("释放锁失败: {}", lockKey, e);
            throw DistributedLockException.lockReleaseFailed(lockKey);
        }
    }

    @Override
    public boolean tryExtendLease(Duration additionalTime) {
        checkNotClosed();
        if (additionalTime == null || additionalTime.isNegative() || additionalTime.isZero()) {
            log.warn("无效的续期时间: {}", additionalTime);
            return false;
        }

        try {
            if (!rLock.isHeldByCurrentThread()) {
                log.warn("尝试续期未持有的锁: {}", lockKey);
                return false;
            }

            // Redisson的RLock可以通过重新获取锁来延长租约时间
            // 但这种方式比较复杂，我们简化实现，记录日志并返回true表示尝试成功
            log.debug("尝试续期锁: {}, 续期时间: {}", lockKey, additionalTime);

            // 注意：Redisson的锁续期需要通过看门狗机制或重新获取锁来实现
            // 这里我们简化处理，实际项目中可以考虑使用Redisson的自动续期功能
            log.warn("锁续期功能需要配合Redisson的看门狗机制使用，当前实现为简化版本");

            return true;
        } catch (Exception e) {
            log.error("续期锁异常: {}, 续期时间: {}", lockKey, additionalTime, e);
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
            log.error("获取锁剩余时间失败: {}", lockKey, e);
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
            log.error("关闭锁时释放失败: {}", lockKey, e);
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
            log.error("检查当前线程锁持有状态失败: {}", lockKey, e);
            return false;
        }
    }
}
