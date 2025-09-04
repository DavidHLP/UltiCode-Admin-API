package com.david.commons.redis.cache.protection;

import com.david.commons.redis.cache.protection.interfaces.CacheAvalancheProtection;

import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存雪崩防护实现
 *
 * @author David
 */
@Slf4j
@Service
public class CacheAvalancheProtectionImpl implements CacheAvalancheProtection {

    private final Random random = new Random();
    private final AvalancheProtectionStatsImpl stats = new AvalancheProtectionStatsImpl();

    /**
     * 创建成功的预热结果
     */
    public static WarmupResult createSuccessResult(long warmupCount, long duration) {
        return new WarmupResultImpl(true, warmupCount, duration, null);
    }

    /**
     * 创建失败的预热结果
     */
    public static WarmupResult createFailureResult(String errorMessage, long duration) {
        return new WarmupResultImpl(false, 0, duration, errorMessage);
    }

    @Override
    public long calculateRandomTtl(long baseTtl) {
        return calculateRandomTtl(baseTtl, 20); // 默认20%抖动
    }

    @Override
    public long calculateRandomTtl(long baseTtl, int jitterPercent) {
        stats.incrementTtlCalculationCount();

        if (jitterPercent <= 0 || jitterPercent > 100) {
            log.warn("抖动百分比无效: {}，使用默认值20%", jitterPercent);
            jitterPercent = 20;
        }

        // 计算抖动范围
        double jitterRatio = jitterPercent / 100.0;
        long jitterRange = (long) (baseTtl * jitterRatio);

        // 生成随机抖动 [-jitterRange/2, +jitterRange/2]
        long jitter = (long) (random.nextGaussian() * jitterRange / 2);
        long randomTtl = baseTtl + jitter;

        // 确保TTL不小于基础TTL的50%
        long minTtl = baseTtl / 2;
        randomTtl = Math.max(randomTtl, minTtl);

        log.debug("计算随机TTL: {} (基础: {}, 抖动: {}%)", randomTtl, baseTtl, jitterPercent);
        return randomTtl;
    }

    @Override
    public WarmupResult warmupCache(WarmupTask warmupTask) {
        stats.incrementWarmupTaskCount();
        long startTime = System.currentTimeMillis();

        try {
            log.info("开始缓存预热任务");
            WarmupResult result = warmupTask.execute();

            long duration = System.currentTimeMillis() - startTime;

            if (result.success()) {
                stats.incrementWarmupSuccessCount();
                stats.addWarmupKeys(result.warmupCount());
                stats.addWarmupDuration(result.duration());

                log.info("缓存预热成功完成: {} 个键，耗时 {}ms",
                        result.warmupCount(), result.duration());
            } else {
                stats.incrementWarmupFailureCount();
                log.error("缓存预热失败: {}", result.errorMessage());
            }

            return result;
        } catch (Exception e) {
            stats.incrementWarmupFailureCount();
            long duration = System.currentTimeMillis() - startTime;

            log.error("缓存预热任务在 {}ms 后失败", duration, e);
            return new WarmupResultImpl(false, 0, duration, e.getMessage());
        }
    }

    @Override
    @Async
    public void warmupCacheAsync(WarmupTask warmupTask) {
        CompletableFuture.runAsync(() -> warmupCache(warmupTask))
                .exceptionally(throwable -> {
                    log.error("异步缓存预热失败", throwable);
                    return null;
                });
    }

    @Override
    public boolean shouldWarmup(String key, long ttl, long threshold) {
        boolean shouldWarmup = ttl <= threshold;

        if (shouldWarmup) {
            log.debug("缓存键 {} 需要预热: ttl={}s, 阈值={}s", key, ttl, threshold);
        }

        return shouldWarmup;
    }

    @Override
    public AvalancheProtectionStats getStats() {
        return stats;
    }

    /**
         * 预热结果实现
         */
        private record WarmupResultImpl(boolean success, long warmupCount, long duration, String errorMessage) implements WarmupResult {

    }

    /**
     * 雪崩防护统计信息实现
     */
    private static class AvalancheProtectionStatsImpl implements AvalancheProtectionStats {

        private final AtomicLong ttlCalculationCount = new AtomicLong(0);
        private final AtomicLong warmupTaskCount = new AtomicLong(0);
        private final AtomicLong warmupSuccessCount = new AtomicLong(0);
        private final AtomicLong warmupFailureCount = new AtomicLong(0);
        private final AtomicLong totalWarmupKeys = new AtomicLong(0);
        private final AtomicLong totalWarmupDuration = new AtomicLong(0);

        @Override
        public long getTtlCalculationCount() {
            return ttlCalculationCount.get();
        }

        @Override
        public long getWarmupTaskCount() {
            return warmupTaskCount.get();
        }

        @Override
        public long getWarmupSuccessCount() {
            return warmupSuccessCount.get();
        }

        @Override
        public long getWarmupFailureCount() {
            return warmupFailureCount.get();
        }

        @Override
        public long getTotalWarmupKeys() {
            return totalWarmupKeys.get();
        }

        @Override
        public double getAverageWarmupDuration() {
            long count = warmupSuccessCount.get();
            return count > 0 ? (double) totalWarmupDuration.get() / count : 0.0;
        }

        public void incrementTtlCalculationCount() {
            ttlCalculationCount.incrementAndGet();
        }

        public void incrementWarmupTaskCount() {
            warmupTaskCount.incrementAndGet();
        }

        public void incrementWarmupSuccessCount() {
            warmupSuccessCount.incrementAndGet();
        }

        public void incrementWarmupFailureCount() {
            warmupFailureCount.incrementAndGet();
        }

        public void addWarmupKeys(long keys) {
            totalWarmupKeys.addAndGet(keys);
        }

        public void addWarmupDuration(long duration) {
            totalWarmupDuration.addAndGet(duration);
        }
    }
}