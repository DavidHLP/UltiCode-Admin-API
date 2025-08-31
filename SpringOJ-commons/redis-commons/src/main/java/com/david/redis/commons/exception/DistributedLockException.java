package com.david.redis.commons.exception;

import lombok.Getter;

import java.time.Duration;

/**
 * 分布式锁异常类
 * <p>
 * 用于处理分布式锁操作过程中发生的异常，包括锁获取超时、
 * 锁释放失败、锁续期失败等场景。
 *
 * @author David
 */
@Getter
public class DistributedLockException extends RedisOperationException {

    /**
     * 锁的键名
     * -- GETTER --
     *  获取锁键名
     *

     */
    private final String lockKey;

    /**
     * 等待时间
     * -- GETTER --
     *  获取等待时间
     *

     */
    private final Duration waitTime;

    /**
     * 租约时间
     * -- GETTER --
     *  获取租约时间
     *
     * @return 租约时间，可能为null

     */
    private final Duration leaseTime;

    /**
     * 构造函数
     *
     * @param message 异常消息
     * @param lockKey 锁键名
     */
    public DistributedLockException(String message, String lockKey) {
        super(message, "DISTRIBUTED_LOCK", lockKey);
        this.lockKey = lockKey;
        this.waitTime = null;
        this.leaseTime = null;
    }

    /**
     * 构造函数，包含等待时间
     *
     * @param message  异常消息
     * @param lockKey  锁键名
     * @param waitTime 等待时间
     */
    public DistributedLockException(String message, String lockKey, Duration waitTime) {
        super(buildLockMessage(message, lockKey, waitTime, null), "DISTRIBUTED_LOCK", lockKey, waitTime);
        this.lockKey = lockKey;
        this.waitTime = waitTime;
        this.leaseTime = null;
    }

    /**
     * 构造函数，包含等待时间和租约时间
     *
     * @param message   异常消息
     * @param lockKey   锁键名
     * @param waitTime  等待时间
     * @param leaseTime 租约时间
     */
    public DistributedLockException(String message, String lockKey, Duration waitTime, Duration leaseTime) {
        super(buildLockMessage(message, lockKey, waitTime, leaseTime), "DISTRIBUTED_LOCK", lockKey, waitTime,
                leaseTime);
        this.lockKey = lockKey;
        this.waitTime = waitTime;
        this.leaseTime = leaseTime;
    }

    /**
     * 构造函数，包含原始异常
     *
     * @param message 异常消息
     * @param cause   原始异常
     * @param lockKey 锁键名
     */
    public DistributedLockException(String message, Throwable cause, String lockKey) {
        super(message, cause, "DISTRIBUTED_LOCK", lockKey);
        this.lockKey = lockKey;
        this.waitTime = null;
        this.leaseTime = null;
    }

    /**
     * 构造函数，包含原始异常和时间参数
     *
     * @param message   异常消息
     * @param cause     原始异常
     * @param lockKey   锁键名
     * @param waitTime  等待时间
     * @param leaseTime 租约时间
     */
    public DistributedLockException(String message, Throwable cause, String lockKey, Duration waitTime,
            Duration leaseTime) {
        super(buildLockMessage(message, lockKey, waitTime, leaseTime), cause, "DISTRIBUTED_LOCK", lockKey, waitTime,
                leaseTime);
        this.lockKey = lockKey;
        this.waitTime = waitTime;
        this.leaseTime = leaseTime;
    }

    /**
     * 创建锁获取超时异常
     *
     * @param lockKey  锁键名
     * @param waitTime 等待时间
     * @return 锁超时异常
     */
    public static DistributedLockException lockTimeout(String lockKey, Duration waitTime) {
        return new DistributedLockException(
                String.format("获取分布式锁超时: %s，等待时间: %s", lockKey, waitTime),
                lockKey, waitTime);
    }

    /**
     * 创建锁释放失败异常
     *
     * @param lockKey 锁键名
     * @return 锁释放失败异常
     */
    public static DistributedLockException lockReleaseFailed(String lockKey) {
        return new DistributedLockException(
                String.format("释放分布式锁失败: %s", lockKey),
                lockKey);
    }

    /**
     * 创建锁续期失败异常
     *
     * @param lockKey        锁键名
     * @param additionalTime 续期时间
     * @return 锁续期失败异常
     */
    public static DistributedLockException lockExtendFailed(String lockKey, Duration additionalTime) {
        return new DistributedLockException(
                String.format("分布式锁续期失败: %s，续期时间: %s", lockKey, additionalTime),
                lockKey);
    }

    /**
     * 创建锁已被其他线程持有异常
     *
     * @param lockKey 锁键名
     * @return 锁竞争异常
     */
    public static DistributedLockException lockAlreadyHeld(String lockKey) {
        return new DistributedLockException(
                String.format("分布式锁已被其他线程持有: %s", lockKey),
                lockKey);
    }

    /**
     * 构建包含锁信息的详细消息
     *
     * @param message   基础消息
     * @param lockKey   锁键名
     * @param waitTime  等待时间
     * @param leaseTime 租约时间
     * @return 详细消息
     */
    private static String buildLockMessage(String message, String lockKey, Duration waitTime, Duration leaseTime) {
        StringBuilder sb = new StringBuilder(message);
        sb.append(" [锁: ").append(lockKey).append("]");

        if (waitTime != null) {
            sb.append(" [等待时间: ").append(waitTime).append("]");
        }

        if (leaseTime != null) {
            sb.append(" [租约时间: ").append(leaseTime).append("]");
        }

        return sb.toString();
    }
}