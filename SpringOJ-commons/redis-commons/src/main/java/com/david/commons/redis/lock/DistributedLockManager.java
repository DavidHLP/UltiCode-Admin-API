package com.david.commons.redis.lock;

import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁管理器接口
 *
 * @author David
 */
public interface DistributedLockManager {

        /**
         * 获取可重入锁
         *
         * @param key 锁键
         * @return 可重入锁
         */
        RLock getLock(String key);

        /**
         * 获取公平锁
         *
         * @param key 锁键
         * @return 公平锁
         */
        RLock getFairLock(String key);

        /**
         * 获取读写锁
         *
         * @param key 锁键
         * @return 读写锁
         */
        RReadWriteLock getReadWriteLock(String key);

        /**
         * 尝试获取锁
         *
         * @param key       锁键
         * @param waitTime  等待时间
         * @param leaseTime 持有时间
         * @param unit      时间单位
         * @return 是否获取成功
         */
        boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit);

        /**
         * 尝试获取锁
         *
         * @param key       锁键
         * @param waitTime  等待时间
         * @param leaseTime 持有时间
         * @param unit      时间单位
         * @param lockType  锁类型
         * @return 是否获取成功
         */
        boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit, LockType lockType);

        /**
         * 释放锁
         *
         * @param key 锁键
         */
        void unlock(String key);

        /**
         * 释放锁
         *
         * @param key      锁键
         * @param lockType 锁类型
         */
        void unlock(String key, LockType lockType);

        /**
         * 执行带锁的操作
         *
         * @param key       锁键
         * @param supplier  操作供应商
         * @param waitTime  等待时间
         * @param leaseTime 持有时间
         * @param unit      时间单位
         * @param <T>       返回类型
         * @return 操作结果
         */
        <T> T executeWithLock(String key, Supplier<T> supplier, long waitTime, long leaseTime, TimeUnit unit);

        /**
         * 执行带锁的操作
         *
         * @param key       锁键
         * @param supplier  操作供应商
         * @param waitTime  等待时间
         * @param leaseTime 持有时间
         * @param unit      时间单位
         * @param lockType  锁类型
         * @param <T>       返回类型
         * @return 操作结果
         */
        <T> T executeWithLock(String key, Supplier<T> supplier, long waitTime, long leaseTime, TimeUnit unit,
                        LockType lockType);

        /**
         * 执行带锁的操作（无返回值）
         *
         * @param key       锁键
         * @param runnable  操作
         * @param waitTime  等待时间
         * @param leaseTime 持有时间
         * @param unit      时间单位
         */
        void executeWithLock(String key, Runnable runnable, long waitTime, long leaseTime, TimeUnit unit);

        /**
         * 执行带锁的操作（无返回值）
         *
         * @param key       锁键
         * @param runnable  操作
         * @param waitTime  等待时间
         * @param leaseTime 持有时间
         * @param unit      时间单位
         * @param lockType  锁类型
         */
        void executeWithLock(String key, Runnable runnable, long waitTime, long leaseTime, TimeUnit unit,
                        LockType lockType);

        /**
         * 检查锁是否被持有
         *
         * @param key 锁键
         * @return 是否被持有
         */
        boolean isLocked(String key);

        /**
         * 检查锁是否被当前线程持有
         *
         * @param key 锁键
         * @return 是否被当前线程持有
         */
        boolean isHeldByCurrentThread(String key);

        /**
         * 获取锁的持有数量（可重入次数）
         *
         * @param key 锁键
         * @return 持有数量
         */
        int getHoldCount(String key);

        /**
         * 强制释放锁
         *
         * @param key 锁键
         * @return 是否释放成功
         */
        boolean forceUnlock(String key);
}