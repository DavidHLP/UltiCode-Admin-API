package com.david.redis.commons.enums;

/**
 * 缓存级别枚举
 * 
 * @author David
 * @since 1.0.0
 */
public enum CacheLevel {

    /**
     * L1 本地缓存
     * 应用内存缓存，访问速度最快但容量有限
     */
    L1("本地缓存"),

    /**
     * L2 Redis缓存
     * 分布式缓存，容量大且支持持久化
     */
    L2("Redis缓存"),

    /**
     * L3 数据库
     * 持久化存储，容量最大但访问速度较慢
     */
    L3("数据库");

    private final String description;

    CacheLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 获取默认缓存级别
     * 
     * @return 默认为 L2 (Redis缓存)
     */
    public static CacheLevel getDefault() {
        return L2;
    }
}
