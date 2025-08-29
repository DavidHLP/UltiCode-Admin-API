package com.david.redis.commons.core.cache;

import com.david.log.commons.core.LogUtils;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存操作指标收集器 用于收集缓存命中率、操作次数等统计信息
 *
 * @author David
 */
@Component
@RequiredArgsConstructor
public class CacheOperationMetrics {
    private final LogUtils logUtils;

    // 缓存命中统计
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final AtomicLong cacheEvictions = new AtomicLong(0);

    // 按缓存名称分组的统计
    private final Map<String, AtomicLong> cacheHitsByName = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> cacheMissesByName = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> cacheEvictionsByName = new ConcurrentHashMap<>();

    // 操作时间统计
    private final Map<String, AtomicLong> operationTimes = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> operationCounts = new ConcurrentHashMap<>();

    /** 记录缓存命中 */
    public void recordCacheHit(String cacheName) {
        cacheHits.incrementAndGet();
        cacheHitsByName.computeIfAbsent(cacheName, k -> new AtomicLong(0)).incrementAndGet();
        logUtils.business().trace("cache_metrics", "hit", "recorded", "cacheName: " + cacheName);
    }

    /** 记录缓存未命中 */
    public void recordCacheMiss(String cacheName) {
        cacheMisses.incrementAndGet();
        cacheMissesByName.computeIfAbsent(cacheName, k -> new AtomicLong(0)).incrementAndGet();
        logUtils.business().trace("cache_metrics", "miss", "recorded", "cacheName: " + cacheName);
    }

    /** 记录缓存驱逐 */
    public void recordCacheEviction(String cacheName) {
        cacheEvictions.incrementAndGet();
        cacheEvictionsByName.computeIfAbsent(cacheName, k -> new AtomicLong(0)).incrementAndGet();
        logUtils.business()
                .trace("cache_metrics", "eviction", "recorded", "cacheName: " + cacheName);
    }

    /** 记录操作执行时间 */
    public void recordOperationTime(String operation, long timeMillis) {
        operationTimes.computeIfAbsent(operation, k -> new AtomicLong(0)).addAndGet(timeMillis);
        operationCounts.computeIfAbsent(operation, k -> new AtomicLong(0)).incrementAndGet();
        logUtils.performance().timing("cache_operation_" + operation, timeMillis);
    }

    /** 获取总缓存命中次数 */
    public long getTotalCacheHits() {
        return cacheHits.get();
    }

    /** 获取总缓存未命中次数 */
    public long getTotalCacheMisses() {
        return cacheMisses.get();
    }

    /** 获取总缓存驱逐次数 */
    public long getTotalCacheEvictions() {
        return cacheEvictions.get();
    }

    /** 获取缓存命中率 */
    public double getCacheHitRatio() {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long total = hits + misses;
        return total > 0 ? (double) hits / total : 0.0;
    }

    /** 获取指定缓存的命中率 */
    public double getCacheHitRatio(String cacheName) {
        long hits = cacheHitsByName.getOrDefault(cacheName, new AtomicLong(0)).get();
        long misses = cacheMissesByName.getOrDefault(cacheName, new AtomicLong(0)).get();
        long total = hits + misses;
        return total > 0 ? (double) hits / total : 0.0;
    }

    /** 获取操作平均执行时间 */
    public double getAverageOperationTime(String operation) {
        long totalTime = operationTimes.getOrDefault(operation, new AtomicLong(0)).get();
        long count = operationCounts.getOrDefault(operation, new AtomicLong(0)).get();
        return count > 0 ? (double) totalTime / count : 0.0;
    }

    /** 获取所有缓存统计信息 */
    public Map<String, Object> getAllMetrics() {
        Map<String, Object> metrics = new ConcurrentHashMap<>();

        // 总体统计
        metrics.put("total.cache.hits", getTotalCacheHits());
        metrics.put("total.cache.misses", getTotalCacheMisses());
        metrics.put("total.cache.evictions", getTotalCacheEvictions());
        metrics.put("total.cache.hit.ratio", getCacheHitRatio());

        // 按缓存名称的统计
        for (String cacheName : cacheHitsByName.keySet()) {
            metrics.put("cache." + cacheName + ".hits", cacheHitsByName.get(cacheName).get());
            metrics.put(
                    "cache." + cacheName + ".misses",
                    cacheMissesByName.getOrDefault(cacheName, new AtomicLong(0)).get());
            metrics.put("cache." + cacheName + ".hit.ratio", getCacheHitRatio(cacheName));
        }

        // 操作时间统计
        for (String operation : operationTimes.keySet()) {
            metrics.put("operation." + operation + ".avg.time", getAverageOperationTime(operation));
            metrics.put(
                    "operation." + operation + ".count",
                    operationCounts.getOrDefault(operation, new AtomicLong(0)).get());
        }

        return metrics;
    }

    /** 重置所有统计信息 */
    public void reset() {
        cacheHits.set(0);
        cacheMisses.set(0);
        cacheEvictions.set(0);
        cacheHitsByName.clear();
        cacheMissesByName.clear();
        cacheEvictionsByName.clear();
        operationTimes.clear();
        operationCounts.clear();
        logUtils.business().event("cache_metrics", "reset", "success", "Cache metrics reset");
    }

    /** 打印统计摘要 */
    public void printSummary() {
        logUtils.business()
                .event("cache_metrics", "summary", "start", "=== Cache Metrics Summary ===");
        logUtils.business()
                .event(
                        "cache_metrics",
                        "summary",
                        "total_hits",
                        "Total Cache Hits: " + getTotalCacheHits());
        logUtils.business()
                .event(
                        "cache_metrics",
                        "summary",
                        "total_misses",
                        "Total Cache Misses: " + getTotalCacheMisses());
        logUtils.business()
                .event(
                        "cache_metrics",
                        "summary",
                        "total_evictions",
                        "Total Cache Evictions: " + getTotalCacheEvictions());
        logUtils.business()
                .event(
                        "cache_metrics",
                        "summary",
                        "hit_ratio",
                        String.format("Cache Hit Ratio: %.2f%%", getCacheHitRatio() * 100));

        if (!cacheHitsByName.isEmpty()) {
            logUtils.business()
                    .event(
                            "cache_metrics",
                            "summary",
                            "cache_specific",
                            "=== Cache-specific Metrics ===");
            for (String cacheName : cacheHitsByName.keySet()) {
                logUtils.business()
                        .event(
                                "cache_metrics",
                                "summary",
                                "cache_detail",
                                "Cache ["
                                        + cacheName
                                        + "] - Hits: "
                                        + cacheHitsByName.get(cacheName).get()
                                        + ", Misses: "
                                        + cacheMissesByName
                                                .getOrDefault(cacheName, new AtomicLong(0))
                                                .get()
                                        + String.format(
                                                ", Hit Ratio: %.2f%%",
                                                getCacheHitRatio(cacheName) * 100));
            }
        }
    }
}
