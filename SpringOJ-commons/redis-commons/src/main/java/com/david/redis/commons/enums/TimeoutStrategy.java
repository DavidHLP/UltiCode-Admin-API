package com.david.redis.commons.enums;

/**
 * 超时处理策略枚举
 * 
 * @author David
 * @since 1.0.0
 */
public enum TimeoutStrategy {

    /**
     * 回滚事务
     * 超时时自动回滚事务
     */
    ROLLBACK("回滚事务"),

    /**
     * 强制提交
     * 超时时强制提交事务
     */
    FORCE_COMMIT("强制提交"),

    /**
     * 抛出异常
     * 超时时抛出异常，由上层处理
     */
    THROW_EXCEPTION("抛出异常"),

    /**
     * 延长超时
     * 自动延长事务超时时间
     */
    EXTEND_TIMEOUT("延长超时");

    private final String description;

    TimeoutStrategy(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 获取默认超时策略
     * 
     * @return 默认为 ROLLBACK
     */
    public static TimeoutStrategy getDefault() {
        return ROLLBACK;
    }
}
