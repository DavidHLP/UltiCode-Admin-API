package com.david.redis.commons.enums;

import lombok.Getter;

/**
 * 缓存预热优先级枚举
 * 
 * @author David
 * @since 1.0.0
 */
@Getter
public enum WarmUpPriority {

    /**
     * 高优先级
     * 核心业务数据，应用启动时立即预热
     */
    HIGH(1, "高优先级"),

    /**
     * 中优先级
     * 常用配置数据，启动后延迟预热
     */
    MEDIUM(2, "中优先级"),

    /**
     * 低优先级
     * 统计分析数据，空闲时预热
     */
    LOW(3, "低优先级");

    private final int level;
    private final String description;

    WarmUpPriority(int level, String description) {
        this.level = level;
        this.description = description;
    }

    /**
     * 获取默认预热优先级
     * 
     * @return 默认为 MEDIUM
     */
    public static WarmUpPriority getDefault() {
        return MEDIUM;
    }
}
