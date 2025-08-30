package com.david.redis.commons.enums;

import lombok.Getter;

/**
 * 锁策略枚举
 * 
 * @author David
 * @since 1.0.0
 */
@Getter
public enum LockStrategy {

    /**
     * 自动选择锁类型
     * 根据操作类型自动选择最适合的锁策略
     */
    AUTO("自动选择锁类型"),

    /**
     * 读锁
     * 允许多个线程同时读取，但不允许写入
     */
    READ_LOCK("读锁"),

    /**
     * 写锁
     * 独占锁，不允许其他线程读取或写入
     */
    WRITE_LOCK("写锁"),

    /**
     * 锁升级
     * 从读锁升级为写锁
     */
    UPGRADE("锁升级"),

    /**
     * 锁降级
     * 从写锁降级为读锁
     */
    DOWNGRADE("锁降级");

    private final String description;

    LockStrategy(String description) {
        this.description = description;
    }

    /**
     * 获取默认锁策略
     * 
     * @return 默认为 AUTO
     */
    public static LockStrategy getDefault() {
        return AUTO;
    }
}
