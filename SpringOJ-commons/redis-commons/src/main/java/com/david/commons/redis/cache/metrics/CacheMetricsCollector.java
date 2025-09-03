package com.david.commons.redis.cache.metrics;

import com.david.commons.redis.cache.CacheMetadata;
import com.david.commons.redis.cache.enums.CacheOperation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * 缓存性能指标收集器
 *
 * <p>收集和统计缓存操作的性能指标，包括命中率、响应时间、错误率等。 支持按操作类型、方法名等维度进行统计。
 *
 * @author David
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CacheMetricsCollector {

    // 全局指标
    private final LongAdder totalOperations = new LongAdder();
    private final LongAdder successfulOperations = new LongAdder();
    private final LongAdder failedOperations = new LongAdder();
    private final LongAdder cacheHits = new LongAdder();
    private final LongAdder cacheMisses = new LongAdder();
    private final AtomicLong totalResponseTime = new AtomicLong(0);

    // 按操作类型统计
    private final ConcurrentHashMap<CacheOperation, OperationMetrics> operationMetrics =
            new ConcurrentHashMap<>();

    // 按方法名统计
    private final ConcurrentHashMap<String, MethodMetrics> methodMetrics =
            new ConcurrentHashMap<>();

    // 错误统计
    private final ConcurrentHashMap<String, LongAdder> errorCounts = new ConcurrentHashMap<>();

    /** 记录缓存操作开始 */
    public CacheOperationTimer startOperation(CacheMetadata metadata, String methodName) {
        totalOperations.increment();

        // 更新操作类型指标
        operationMetrics
                .computeIfAbsent(metadata.operation(), k -> new OperationMetrics())
                .totalOperations
                .increment();

        // 更新方法指标
        methodMetrics
                .computeIfAbsent(methodName, k -> new MethodMetrics())
                .totalOperations
                .increment();

        return new CacheOperationTimer(System.nanoTime(), metadata, methodName);
    }

    /** 记录缓存操作成功 */
    public void recordSuccess(CacheOperationTimer timer, boolean cacheHit) {
        long duration = System.nanoTime() - timer.startTime();

        successfulOperations.increment();
        totalResponseTime.addAndGet(duration);

        // 记录缓存命中情况
        if (timer.metadata().operation() == CacheOperation.CACHEABLE) {
            if (cacheHit) {
                cacheHits.increment();
            } else {
                cacheMisses.increment();
            }
        }

        // 更新操作类型指标
        OperationMetrics opMetrics = operationMetrics.get(timer.metadata().operation());
        if (opMetrics != null) {
            opMetrics.successfulOperations.increment();
            opMetrics.totalResponseTime.addAndGet(duration);

            if (timer.metadata().operation() == CacheOperation.CACHEABLE) {
                if (cacheHit) {
                    opMetrics.cacheHits.increment();
                } else {
                    opMetrics.cacheMisses.increment();
                }
            }
        }

        // 更新方法指标
        MethodMetrics methodMetrics = this.methodMetrics.get(timer.methodName());
        if (methodMetrics != null) {
            methodMetrics.successfulOperations.increment();
            methodMetrics.totalResponseTime.addAndGet(duration);

            if (cacheHit) {
                methodMetrics.cacheHits.increment();
            } else {
                methodMetrics.cacheMisses.increment();
            }
        }

        log.debug(
                "缓存操作成功完成。方法: {}, 操作: {}, 耗时: {}纳秒, 缓存命中: {}",
                timer.methodName(),
                timer.metadata().operation(),
                duration,
                cacheHit);
    }

    /** 记录缓存操作失败 */
    public void recordFailure(CacheOperationTimer timer, Exception exception) {
        long duration = System.nanoTime() - timer.startTime();

        failedOperations.increment();
        totalResponseTime.addAndGet(duration);

        // 记录错误类型
        String errorType = exception.getClass().getSimpleName();
        errorCounts.computeIfAbsent(errorType, k -> new LongAdder()).increment();

        // 更新操作类型指标
        OperationMetrics opMetrics = operationMetrics.get(timer.metadata().operation());
        if (opMetrics != null) {
            opMetrics.failedOperations.increment();
            opMetrics.totalResponseTime.addAndGet(duration);
        }

        // 更新方法指标
        MethodMetrics methodMetrics = this.methodMetrics.get(timer.methodName());
        if (methodMetrics != null) {
            methodMetrics.failedOperations.increment();
            methodMetrics.totalResponseTime.addAndGet(duration);
        }

        log.warn(
                "缓存操作失败。方法: {}, 操作: {}, 耗时: {}纳秒, 错误: {}",
                timer.methodName(),
                timer.metadata().operation(),
                duration,
                exception.getMessage());
    }

    /** 获取全局缓存指标 */
    public CacheMetrics getGlobalMetrics() {
        long total = totalOperations.sum();
        long successful = successfulOperations.sum();
        long failed = failedOperations.sum();
        long hits = cacheHits.sum();
        long misses = cacheMisses.sum();
        long totalTime = totalResponseTime.get();

        return CacheMetrics.builder()
                .totalOperations(total)
                .successfulOperations(successful)
                .failedOperations(failed)
                .successRate(total > 0 ? (double) successful / total : 0.0)
                .errorRate(total > 0 ? (double) failed / total : 0.0)
                .cacheHits(hits)
                .cacheMisses(misses)
                .hitRate((hits + misses) > 0 ? (double) hits / (hits + misses) : 0.0)
                .averageResponseTime(
                        successful > 0 ? (double) totalTime / successful / 1_000_000 : 0.0) // 转换为毫秒
                .build();
    }

    /** 获取按操作类型的指标 */
    public CacheMetrics getOperationMetrics(CacheOperation operation) {
        OperationMetrics metrics = operationMetrics.get(operation);
        if (metrics == null) {
            return CacheMetrics.builder().build();
        }

        long total = metrics.totalOperations.sum();
        long successful = metrics.successfulOperations.sum();
        long failed = metrics.failedOperations.sum();
        long hits = metrics.cacheHits.sum();
        long misses = metrics.cacheMisses.sum();
        long totalTime = metrics.totalResponseTime.get();

        return CacheMetrics.builder()
                .totalOperations(total)
                .successfulOperations(successful)
                .failedOperations(failed)
                .successRate(total > 0 ? (double) successful / total : 0.0)
                .errorRate(total > 0 ? (double) failed / total : 0.0)
                .cacheHits(hits)
                .cacheMisses(misses)
                .hitRate((hits + misses) > 0 ? (double) hits / (hits + misses) : 0.0)
                .averageResponseTime(
                        successful > 0 ? (double) totalTime / successful / 1_000_000 : 0.0)
                .build();
    }

    /** 获取按方法名的指标 */
    public CacheMetrics getMethodMetrics(String methodName) {
        MethodMetrics metrics = methodMetrics.get(methodName);
        if (metrics == null) {
            return CacheMetrics.builder().build();
        }

        long total = metrics.totalOperations.sum();
        long successful = metrics.successfulOperations.sum();
        long failed = metrics.failedOperations.sum();
        long hits = metrics.cacheHits.sum();
        long misses = metrics.cacheMisses.sum();
        long totalTime = metrics.totalResponseTime.get();

        return CacheMetrics.builder()
                .totalOperations(total)
                .successfulOperations(successful)
                .failedOperations(failed)
                .successRate(total > 0 ? (double) successful / total : 0.0)
                .errorRate(total > 0 ? (double) failed / total : 0.0)
                .cacheHits(hits)
                .cacheMisses(misses)
                .hitRate((hits + misses) > 0 ? (double) hits / (hits + misses) : 0.0)
                .averageResponseTime(
                        successful > 0 ? (double) totalTime / successful / 1_000_000 : 0.0)
                .build();
    }

    /** 获取错误统计 */
    public ConcurrentHashMap<String, Long> getErrorStatistics() {
        ConcurrentHashMap<String, Long> result = new ConcurrentHashMap<>();
        errorCounts.forEach((errorType, count) -> result.put(errorType, count.sum()));
        return result;
    }

    /** 重置所有指标 */
    public void reset() {
        totalOperations.reset();
        successfulOperations.reset();
        failedOperations.reset();
        cacheHits.reset();
        cacheMisses.reset();
        totalResponseTime.set(0);

        operationMetrics.clear();
        methodMetrics.clear();
        errorCounts.clear();

        log.info("缓存指标已重置");
    }

    /** 操作类型指标 */
    private static class OperationMetrics {
        final LongAdder totalOperations = new LongAdder();
        final LongAdder successfulOperations = new LongAdder();
        final LongAdder failedOperations = new LongAdder();
        final LongAdder cacheHits = new LongAdder();
        final LongAdder cacheMisses = new LongAdder();
        final AtomicLong totalResponseTime = new AtomicLong(0);
    }

    /** 方法指标 */
    private static class MethodMetrics {
        final LongAdder totalOperations = new LongAdder();
        final LongAdder successfulOperations = new LongAdder();
        final LongAdder failedOperations = new LongAdder();
        final LongAdder cacheHits = new LongAdder();
        final LongAdder cacheMisses = new LongAdder();
        final AtomicLong totalResponseTime = new AtomicLong(0);
    }
}
