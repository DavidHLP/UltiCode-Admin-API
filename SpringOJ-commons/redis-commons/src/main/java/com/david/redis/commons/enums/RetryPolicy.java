package com.david.redis.commons.enums;

import lombok.Getter;

/**
 * 重试策略枚举
 * 
 * @author David
 * @since 1.0.0
 */
@Getter
public enum RetryPolicy {

    /**
     * 默认重试策略
     * 失败时重试3次，每次间隔1秒
     */
    DEFAULT("默认重试策略"),

    /**
     * 不重试
     * 失败后立即返回错误
     */
    NONE("不重试"),

    /**
     * 固定间隔重试
     * 失败后按固定时间间隔重试
     */
    FIXED_INTERVAL("固定间隔重试"),

    /**
     * 指数退避重试
     * 重试间隔呈指数增长
     */
    EXPONENTIAL_BACKOFF("指数退避重试"),

    /**
     * 线性退避重试
     * 重试间隔线性增长
     */
    LINEAR_BACKOFF("线性退避重试");

    private final String description;

    RetryPolicy(String description) {
        this.description = description;
    }

    /**
     * 获取默认重试策略
     * 
     * @return 默认为 DEFAULT
     */
    public static RetryPolicy getDefault() {
        return DEFAULT;
    }
}
