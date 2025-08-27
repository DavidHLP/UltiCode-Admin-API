package com.david.redis.commons.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * 缓存性能指标收集器
 * 收集缓存命中率、响应时间、操作统计等性能指标
 * 
 * @author David
 * @since 1.0.0
 */
@Slf4j
@Component
public class CacheMetricsCollector {

    // 全局统计
    private final LongAdder totalHits = new LongAdder();
    private final LongAdder totalMisses = new LongAdder();
    private final LongAdder totalOperations = new LongAdder();
    private final LongAdder totalResponseTime = new LongAdder();

    // 按键前缀统计
    private final ConcurrentHashMap<String, KeyMetrics> keyMetricsMap = new ConcurrentHashMap<>();

    // 操作类型统计
    private final ConcurrentHashMap<String, OperationMetrics> operationMetricsMap = new ConcurrentHashMap<>();

    /**
     * 记录缓存命中
     * 
     * @param key          缓存键
     * @param responseTime 响应时间（毫秒）
     */
    public void recordHit(String key, long responseTime) {
        totalHits.increment();
        totalOperations.increment();
        totalResponseTime.add(responseTime);

        String keyPrefix = extractKeyPrefix(key);
        keyMetricsMap.computeIfAbsent(keyPrefix, k -> new KeyMetrics()).recordHit(responseTime);

        operationMetricsMap.computeIfAbsent("GET", k -> new OperationMetrics()).recordSuccess(responseTime);
    }

    /**
     * 记录缓存未命中
     * 
     * @param key          缓存键
     * @param responseTime 响应时间（毫秒）
     */
    public void recordMiss(String key, long responseTime) {
        totalMisses.increment();
        totalOperations.increment();
        totalResponseTime.add(responseTime);

        String keyPrefix = extractKeyPrefix(key);
        keyMetricsMap.computeIfAbsent(keyPrefix, k -> new KeyMetrics()).recordMiss(responseTime);

        operationMetricsMap.computeIfAbsent("GET", k -> new OperationMetrics()).recordSuccess(responseTime);
    }

    /**
     * 记录缓存写入
     * 
     * @param key          缓存键
     * @param responseTime 响应时间（毫秒）
     */
    public void recordSet(String key, long responseTime) {
        totalOperations.increment();
        totalResponseTime.add(responseTime);

        String keyPrefix = extractKeyPrefix(key);
        keyMetricsMap.computeIfAbsent(keyPrefix, k -> new KeyMetrics()).recordSet(responseTime);

        operationMetricsMap.computeIfAbsent("SET", k -> new OperationMetrics()).recordSuccess(responseTime);
    }

    /**
     * 记录缓存删除
     * 
     * @param key          缓存键
     * @param responseTime 响应时间（毫秒）
     */
    public void recordDelete(String key, long responseTime) {
        totalOperations.increment();
        totalResponseTime.add(responseTime);

        String keyPrefix = extractKeyPrefix(key);
        keyMetricsMap.computeIfAbsent(keyPrefix, k -> new KeyMetrics()).recordDelete(responseTime);

        operationMetricsMap.computeIfAbsent("DELETE", k -> new OperationMetrics()).recordSuccess(responseTime);
    }

    /**
     * 记录操作错误
     * 
     * @param operation    操作类型
     * @param responseTime 响应时间（毫秒）
     */
    public void recordError(String operation, long responseTime) {
        totalOperations.increment();
        totalResponseTime.add(responseTime);

        operationMetricsMap.computeIfAbsent(operation, k -> new OperationMetrics()).recordError(responseTime);
    }

    /**
     * 获取全局缓存统计
     */
    public CacheStats getGlobalStats() {
        long hits = totalHits.sum();
        long misses = totalMisses.sum();
        long operations = totalOperations.sum();
        long responseTime = totalResponseTime.sum();

        double hitRate = operations > 0 ? (double) hits / (hits + misses) * 100 : 0.0;
        double avgResponseTime = operations > 0 ? (double) responseTime / operations : 0.0;

        return new CacheStats(hits, misses, operations, hitRate, avgResponseTime);
    }

    /**
     * 获取指定键前缀的统计
     */
    public KeyStats getKeyStats(String keyPrefix) {
        KeyMetrics metrics = keyMetricsMap.get(keyPrefix);
        if (metrics == null) {
            return new KeyStats(keyPrefix, 0, 0, 0, 0, 0, 0.0, 0.0);
        }

        return metrics.toStats(keyPrefix);
    }

    /**
     * 获取操作统计
     */
    public OperationStats getOperationStats(String operation) {
        OperationMetrics metrics = operationMetricsMap.get(operation);
        if (metrics == null) {
            return new OperationStats(operation, 0, 0, 0.0, 0.0);
        }

        return metrics.toStats(operation);
    }

    /**
     * 获取所有键前缀统计
     */
    public java.util.Map<String, KeyStats> getAllKeyStats() {
        java.util.Map<String, KeyStats> result = new ConcurrentHashMap<>();
        keyMetricsMap.forEach((prefix, metrics) -> {
            result.put(prefix, metrics.toStats(prefix));
        });
        return result;
    }

    /**
     * 获取所有操作统计
     */
    public java.util.Map<String, OperationStats> getAllOperationStats() {
        java.util.Map<String, OperationStats> result = new ConcurrentHashMap<>();
        operationMetricsMap.forEach((operation, metrics) -> {
            result.put(operation, metrics.toStats(operation));
        });
        return result;
    }

    /**
     * 重置所有统计
     */
    public void reset() {
        totalHits.reset();
        totalMisses.reset();
        totalOperations.reset();
        totalResponseTime.reset();
        keyMetricsMap.clear();
        operationMetricsMap.clear();

        log.info("缓存性能统计已重置");
    }

    /**
     * 提取键前缀
     */
    private String extractKeyPrefix(String key) {
        if (key == null || key.isEmpty()) {
            return "unknown";
        }

        int colonIndex = key.indexOf(':');
        if (colonIndex > 0) {
            return key.substring(0, colonIndex);
        }

        return "default";
    }

    /**
     * 键级别指标
     */
    private static class KeyMetrics {
        private final LongAdder hits = new LongAdder();
        private final LongAdder misses = new LongAdder();
        private final LongAdder sets = new LongAdder();
        private final LongAdder deletes = new LongAdder();
        private final LongAdder totalResponseTime = new LongAdder();
        private final LongAdder operations = new LongAdder();

        public void recordHit(long responseTime) {
            hits.increment();
            operations.increment();
            totalResponseTime.add(responseTime);
        }

        public void recordMiss(long responseTime) {
            misses.increment();
            operations.increment();
            totalResponseTime.add(responseTime);
        }

        public void recordSet(long responseTime) {
            sets.increment();
            operations.increment();
            totalResponseTime.add(responseTime);
        }

        public void recordDelete(long responseTime) {
            deletes.increment();
            operations.increment();
            totalResponseTime.add(responseTime);
        }

        public KeyStats toStats(String keyPrefix) {
            long hitsCount = hits.sum();
            long missesCount = misses.sum();
            long setsCount = sets.sum();
            long deletesCount = deletes.sum();
            long operationsCount = operations.sum();
            long responseTimeSum = totalResponseTime.sum();

            double hitRate = (hitsCount + missesCount) > 0 ? (double) hitsCount / (hitsCount + missesCount) * 100 : 0.0;
            double avgResponseTime = operationsCount > 0 ? (double) responseTimeSum / operationsCount : 0.0;

            return new KeyStats(keyPrefix, hitsCount, missesCount, setsCount,
                    deletesCount, operationsCount, hitRate, avgResponseTime);
        }
    }

    /**
     * 操作级别指标
     */
    private static class OperationMetrics {
        private final LongAdder successes = new LongAdder();
        private final LongAdder errors = new LongAdder();
        private final LongAdder totalResponseTime = new LongAdder();

        public void recordSuccess(long responseTime) {
            successes.increment();
            totalResponseTime.add(responseTime);
        }

        public void recordError(long responseTime) {
            errors.increment();
            totalResponseTime.add(responseTime);
        }

        public OperationStats toStats(String operation) {
            long successCount = successes.sum();
            long errorCount = errors.sum();
            long totalCount = successCount + errorCount;
            long responseTimeSum = totalResponseTime.sum();

            double successRate = totalCount > 0 ? (double) successCount / totalCount * 100 : 0.0;
            double avgResponseTime = totalCount > 0 ? (double) responseTimeSum / totalCount : 0.0;

            return new OperationStats(operation, successCount, errorCount, successRate, avgResponseTime);
        }
    }

    /**
     * 全局缓存统计
     */
    public static class CacheStats {
        private final long hits;
        private final long misses;
        private final long operations;
        private final double hitRate;
        private final double avgResponseTime;

        public CacheStats(long hits, long misses, long operations, double hitRate, double avgResponseTime) {
            this.hits = hits;
            this.misses = misses;
            this.operations = operations;
            this.hitRate = hitRate;
            this.avgResponseTime = avgResponseTime;
        }

        public long getHits() {
            return hits;
        }

        public long getMisses() {
            return misses;
        }

        public long getOperations() {
            return operations;
        }

        public double getHitRate() {
            return hitRate;
        }

        public double getAvgResponseTime() {
            return avgResponseTime;
        }
    }

    /**
     * 键统计
     */
    public static class KeyStats {
        private final String keyPrefix;
        private final long hits;
        private final long misses;
        private final long sets;
        private final long deletes;
        private final long operations;
        private final double hitRate;
        private final double avgResponseTime;

        public KeyStats(String keyPrefix, long hits, long misses, long sets, long deletes,
                long operations, double hitRate, double avgResponseTime) {
            this.keyPrefix = keyPrefix;
            this.hits = hits;
            this.misses = misses;
            this.sets = sets;
            this.deletes = deletes;
            this.operations = operations;
            this.hitRate = hitRate;
            this.avgResponseTime = avgResponseTime;
        }

        // Getters
        public String getKeyPrefix() {
            return keyPrefix;
        }

        public long getHits() {
            return hits;
        }

        public long getMisses() {
            return misses;
        }

        public long getSets() {
            return sets;
        }

        public long getDeletes() {
            return deletes;
        }

        public long getOperations() {
            return operations;
        }

        public double getHitRate() {
            return hitRate;
        }

        public double getAvgResponseTime() {
            return avgResponseTime;
        }
    }

    /**
     * 操作统计
     */
    public static class OperationStats {
        private final String operation;
        private final long successes;
        private final long errors;
        private final double successRate;
        private final double avgResponseTime;

        public OperationStats(String operation, long successes, long errors,
                double successRate, double avgResponseTime) {
            this.operation = operation;
            this.successes = successes;
            this.errors = errors;
            this.successRate = successRate;
            this.avgResponseTime = avgResponseTime;
        }

        // Getters
        public String getOperation() {
            return operation;
        }

        public long getSuccesses() {
            return successes;
        }

        public long getErrors() {
            return errors;
        }

        public double getSuccessRate() {
            return successRate;
        }

        public double getAvgResponseTime() {
            return avgResponseTime;
        }
    }
}
