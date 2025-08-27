package com.david.redis.commons.core.operations.interfaces;

import com.david.redis.commons.core.lock.interfaces.RedisLock;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Redis 分布式锁操作接口
 * 
 * <p>定义所有分布式锁相关的操作方法，提供统一的锁操作API
 * 
 * @author David
 */
public interface RedisLockOperations {

    /**
     * 尝试获取分布式锁
     *
     * @param lockKey 锁键名
     * @param waitTime 等待时间
     * @param leaseTime 租约时间
     * @return RedisLock实例，如果获取失败抛出异常
     */
    RedisLock tryLock(String lockKey, Duration waitTime, Duration leaseTime);

    /**
     * 使用默认配置尝试获取分布式锁
     *
     * @param lockKey 锁键名
     * @return RedisLock实例，如果获取失败抛出异常
     */
    RedisLock tryLock(String lockKey);

    /**
     * 在分布式锁保护下执行操作（有返回值）
     *
     * @param <T> 返回值类型
     * @param lockKey 锁键名
     * @param waitTime 等待时间
     * @param leaseTime 租约时间
     * @param action 要执行的操作
     * @return 操作的返回值
     */
    <T> T executeWithLock(String lockKey, Duration waitTime, Duration leaseTime, Supplier<T> action);

    /**
     * 在分布式锁保护下执行操作（无返回值）
     *
     * @param lockKey 锁键名
     * @param waitTime 等待时间
     * @param leaseTime 租约时间
     * @param action 要执行的操作
     */
    void executeWithLock(String lockKey, Duration waitTime, Duration leaseTime, Runnable action);

    /**
     * 使用默认配置在分布式锁保护下执行操作（有返回值）
     *
     * @param <T> 返回值类型
     * @param lockKey 锁键名
     * @param action 要执行的操作
     * @return 操作的返回值
     */
    <T> T executeWithLock(String lockKey, Supplier<T> action);

    /**
     * 使用默认配置在分布式锁保护下执行操作（无返回值）
     *
     * @param lockKey 锁键名
     * @param action 要执行的操作
     */
    void executeWithLock(String lockKey, Runnable action);

    /**
     * 带重试机制的锁执行方法（有返回值）
     *
     * @param <T> 返回值类型
     * @param lockKey 锁键名
     * @param waitTime 等待时间
     * @param leaseTime 租约时间
     * @param action 要执行的操作
     * @return 操作的返回值
     */
    <T> T executeWithLockRetry(String lockKey, Duration waitTime, Duration leaseTime, Supplier<T> action);

    /**
     * 带重试机制的锁执行方法（无返回值）
     *
     * @param lockKey 锁键名
     * @param waitTime 等待时间
     * @param leaseTime 租约时间
     * @param action 要执行的操作
     */
    void executeWithLockRetry(String lockKey, Duration waitTime, Duration leaseTime, Runnable action);

    /**
     * 使用默认配置带重试机制的锁执行方法（有返回值）
     *
     * @param <T> 返回值类型
     * @param lockKey 锁键名
     * @param action 要执行的操作
     * @return 操作的返回值
     */
    <T> T executeWithLockRetry(String lockKey, Supplier<T> action);

    /**
     * 使用默认配置带重试机制的锁执行方法（无返回值）
     *
     * @param lockKey 锁键名
     * @param action 要执行的操作
     */
    void executeWithLockRetry(String lockKey, Runnable action);

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
    <T> T executeWithLockOrFallback(String lockKey, Duration waitTime, Duration leaseTime, 
                                   Supplier<T> action, Supplier<T> fallbackAction);

    /**
     * 尝试执行操作，如果无法获取锁则执行回退操作（无返回值）
     *
     * @param lockKey 锁键名
     * @param waitTime 等待时间
     * @param leaseTime 租约时间
     * @param action 主要操作
     * @param fallbackAction 回退操作
     */
    void executeWithLockOrFallback(String lockKey, Duration waitTime, Duration leaseTime, 
                                  Runnable action, Runnable fallbackAction);

    /**
     * 使用默认配置尝试执行操作，如果无法获取锁则执行回退操作（有返回值）
     *
     * @param <T> 返回值类型
     * @param lockKey 锁键名
     * @param action 主要操作
     * @param fallbackAction 回退操作
     * @return 操作的返回值
     */
    <T> T executeWithLockOrFallback(String lockKey, Supplier<T> action, Supplier<T> fallbackAction);

    /**
     * 使用默认配置尝试执行操作，如果无法获取锁则执行回退操作（无返回值）
     *
     * @param lockKey 锁键名
     * @param action 主要操作
     * @param fallbackAction 回退操作
     */
    void executeWithLockOrFallback(String lockKey, Runnable action, Runnable fallbackAction);

    /**
     * 检查锁是否存在
     *
     * @param lockKey 锁键名
     * @return true如果锁存在，否则返回false
     */
    boolean isLockExists(String lockKey);

    /**
     * 强制释放锁（管理员操作）
     * 注意：这个操作会强制释放锁，即使不是当前线程持有的锁
     *
     * @param lockKey 锁键名
     * @return true如果释放成功，否则返回false
     */
    boolean forceUnlock(String lockKey);

    /**
     * 获取锁的剩余租约时间
     *
     * @param lockKey 锁键名
     * @return 剩余租约时间（毫秒），-1表示锁不存在或永不过期
     */
    long getRemainingTimeToLive(String lockKey);

    /**
     * 检查当前线程是否持有指定的锁
     *
     * @param lockKey 锁键名
     * @return true如果当前线程持有该锁，否则返回false
     */
    boolean isHeldByCurrentThread(String lockKey);

    /**
     * 获取锁的持有计数（可重入锁的重入次数）
     *
     * @param lockKey 锁键名
     * @return 持有计数，0表示锁未被持有
     */
    int getHoldCount(String lockKey);
}
