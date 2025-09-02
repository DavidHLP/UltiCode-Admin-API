package com.david.commons.redis.cache.fallback;

import lombok.Builder;
import lombok.Data;

/**
 * 缓存降级指标
 * <p>
 * 记录缓存降级相关的统计信息，用于监控和分析缓存系统的健康状态。
 * </p>
 *
 * @author David
 */
@Data
@Builder
public class FallbackMetrics {

    /**
     * 降级次数
     */
    private long fallbackCount;

    /**
     * 本地缓存大小
     */
    private int localCacheSize;

    /**
     * 熔断器是否开启
     */
    private boolean circuitBreakerOpen;

    /**
     * 熔断器开启时间
     */
    private long circuitBreakerOpenTime;

    /**
     * 本地缓存命中次数
     */
    @Builder.Default
    private long localCacheHits = 0;

    /**
     * 本地缓存未命中次数
     */
    @Builder.Default
    private long localCacheMisses = 0;

    /**
     * 计算本地缓存命中率
     */
    public double getLocalCacheHitRate() {
        long total = localCacheHits + localCacheMisses;
        return total > 0 ? (double) localCacheHits / total : 0.0;
    }

    /**
     * 获取熔断器开启持续时间（毫秒）
     */
    public long getCircuitBreakerOpenDuration() {
        return circuitBreakerOpen ? System.currentTimeMillis() - circuitBreakerOpenTime : 0;
    }
}