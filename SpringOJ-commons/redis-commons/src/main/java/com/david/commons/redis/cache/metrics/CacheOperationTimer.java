package com.david.commons.redis.cache.metrics;

import com.david.commons.redis.cache.CacheMetadata;
import lombok.Data;

/**
 * 缓存操作计时器
 * <p>
 * 用于记录缓存操作的开始时间和相关元数据，便于计算操作耗时。
 * </p>
 *
 * @author David
 */
@Data
public class CacheOperationTimer {

    /**
     * 操作开始时间（纳秒）
     */
    private final long startTime;

    /**
     * 缓存元数据
     */
    private final CacheMetadata metadata;

    /**
     * 方法名
     */
    private final String methodName;

    public CacheOperationTimer(long startTime, CacheMetadata metadata, String methodName) {
        this.startTime = startTime;
        this.metadata = metadata;
        this.methodName = methodName;
    }

    /**
     * 获取已经过的时间（纳秒）
     */
    public long getElapsedTime() {
        return System.nanoTime() - startTime;
    }

    /**
     * 获取已经过的时间（毫秒）
     */
    public double getElapsedTimeMillis() {
        return getElapsedTime() / 1_000_000.0;
    }
}