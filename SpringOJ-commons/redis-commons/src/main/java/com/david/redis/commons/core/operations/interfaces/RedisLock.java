package com.david.redis.commons.core.operations.interfaces;

import java.time.Duration;

/**
 * Redis分布式锁接口
 *
 * <p>提供分布式锁的基本操作，支持自动资源管理（try-with-resources）
 *
 * @author David
 */
public interface RedisLock extends AutoCloseable {

    /**
     * 检查锁是否被当前线程持有
     *
     * @return true如果锁被当前线程持有，否则返回false
     */
    boolean isLocked();

    /**
     * 检查锁是否被任何线程持有
     *
     * @return true如果锁被任何线程持有，否则返回false
     */
    boolean isHeldByAnyThread();

    /**
     * 释放锁 只有持有锁的线程才能释放锁
     *
     * @throws IllegalStateException 如果当前线程没有持有锁
     */
    void unlock();

    /**
     * 尝试延长锁的租约时间
     *
     * @param additionalTime 额外的租约时间
     * @return true如果续期成功，false如果续期失败
     */
    boolean tryExtendLease(Duration additionalTime);

    /**
     * 获取锁的剩余租约时间
     *
     * @return 剩余租约时间，如果锁不存在或已过期返回Duration.ZERO
     */
    Duration getRemainingLeaseTime();

    /**
     * 获取锁的键名
     *
     * @return 锁的键名
     */
    String getLockKey();

    /** 自动关闭资源，释放锁 实现AutoCloseable接口，支持try-with-resources语法 */
    @Override
    void close();
}
