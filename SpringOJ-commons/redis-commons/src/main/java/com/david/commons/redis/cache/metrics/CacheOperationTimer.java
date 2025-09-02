package com.david.commons.redis.cache.metrics;

import com.david.commons.redis.cache.CacheMetadata;

/**
 * 缓存操作计时器
 *
 * <p>用于记录缓存操作的开始时间和相关元数据，便于计算操作耗时。
 *
 * @author David
 * @param startTime 操作开始时间（纳秒）
 * @param metadata 缓存元数据
 * @param methodName 方法名
 */
public record CacheOperationTimer(long startTime, CacheMetadata metadata, String methodName) {}
