package com.david.commons.redis.cache.metrics;

import lombok.Builder;
import lombok.Data;

/**
 * 缓存性能指标
 * <p>
 * 包含缓存操作的各种统计指标，如操作次数、成功率、命中率、响应时间等。
 * </p>
 *
 * @author David
 */
@Data
@Builder
public class CacheMetrics {

    /**
     * 总操作次数
     */
    @Builder.Default
    private long totalOperations = 0;

    /**
     * 成功操作次数
     */
    @Builder.Default
    private long successfulOperations = 0;

    /**
     * 失败操作次数
     */
    @Builder.Default
    private long failedOperations = 0;

    /**
     * 成功率
     */
    @Builder.Default
    private double successRate = 0.0;

    /**
     * 错误率
     */
    @Builder.Default
    private double errorRate = 0.0;

    /**
     * 缓存命中次数
     */
    @Builder.Default
    private long cacheHits = 0;

    /**
     * 缓存未命中次数
     */
    @Builder.Default
    private long cacheMisses = 0;

    /**
     * 缓存命中率
     */
    @Builder.Default
    private double hitRate = 0.0;

    /**
     * 平均响应时间（毫秒）
     */
    @Builder.Default
    private double averageResponseTime = 0.0;

    /**
     * 最大响应时间（毫秒）
     */
    @Builder.Default
    private double maxResponseTime = 0.0;

    /**
     * 最小响应时间（毫秒）
     */
    @Builder.Default
    private double minResponseTime = 0.0;

    /**
     * 获取缓存未命中率
     */
    public double getMissRate() {
        return 1.0 - hitRate;
    }

    /**
     * 获取总缓存访问次数
     */
    public long getTotalCacheAccess() {
        return cacheHits + cacheMisses;
    }

    /**
     * 判断指标是否健康
     */
    public boolean isHealthy() {
        return errorRate < 0.05 && // 错误率小于5%
                (getTotalCacheAccess() == 0 || hitRate > 0.5); // 命中率大于50%（如果有缓存访问）
    }

    /**
     * 获取性能等级
     */
    public PerformanceLevel getPerformanceLevel() {
        if (!isHealthy()) {
            return PerformanceLevel.POOR;
        }

        if (hitRate > 0.9 && averageResponseTime < 10) {
            return PerformanceLevel.EXCELLENT;
        } else if (hitRate > 0.7 && averageResponseTime < 50) {
            return PerformanceLevel.GOOD;
        } else {
            return PerformanceLevel.FAIR;
        }
    }

    /**
     * 性能等级枚举
     */
    public enum PerformanceLevel {
        EXCELLENT("优秀"),
        GOOD("良好"),
        FAIR("一般"),
        POOR("较差");

        private final String description;

        PerformanceLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}