package com.david.redis.commons.enums;

/**
 * 缓存更新策略枚举
 * 
 * @author David
 * @since 1.0.0
 */
public enum UpdateStrategy {

    /**
     * 写入时同步更新缓存
     * 数据写入数据库的同时更新缓存，保证缓存与数据库的强一致性
     */
    WRITE_THROUGH("写入时同步更新缓存"),

    /**
     * 异步批量更新缓存
     * 数据先写入数据库，缓存异步批量更新，提高写入性能
     */
    WRITE_BEHIND("异步批量更新缓存"),

    /**
     * 绕过缓存直接写数据库
     * 写入操作直接访问数据库，不更新缓存，适用于写多读少场景
     */
    WRITE_AROUND("绕过缓存直接写数据库"),

    /**
     * 提前刷新即将过期的缓存
     * 在缓存即将过期前主动刷新，避免缓存失效时的性能抖动
     */
    REFRESH_AHEAD("提前刷新即将过期的缓存");

    private final String description;

    UpdateStrategy(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 获取默认更新策略
     * 
     * @return 默认策略为 WRITE_THROUGH
     */
    public static UpdateStrategy getDefault() {
        return WRITE_THROUGH;
    }
}
