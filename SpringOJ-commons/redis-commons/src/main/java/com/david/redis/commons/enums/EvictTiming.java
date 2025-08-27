package com.david.redis.commons.enums;

/**
 * 缓存删除时机枚举
 * 
 * @author David
 * @since 1.0.0
 */
public enum EvictTiming {

    /**
     * 立即删除
     * 缓存删除操作立即执行，保证数据一致性
     */
    IMMEDIATE("立即删除"),

    /**
     * 延迟删除
     * 延迟一定时间后删除缓存，避免缓存雪崩
     */
    DELAYED("延迟删除"),

    /**
     * 批量删除
     * 将多个删除操作合并为批量操作，提高性能
     */
    BATCH("批量删除"),

    /**
     * 级联删除
     * 删除缓存时同时清理相关联的缓存数据
     */
    CASCADE("级联删除");

    private final String description;

    EvictTiming(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 获取默认删除时机
     * 
     * @return 默认为 IMMEDIATE
     */
    public static EvictTiming getDefault() {
        return IMMEDIATE;
    }
}
