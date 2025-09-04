package com.david.commons.redis.cache.protection;

import com.david.commons.redis.config.RedisCommonsProperties;
import com.david.commons.redis.lock.DistributedLockManager;

import com.david.commons.redis.cache.protection.interfaces.CacheBreakdownProtection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.redisson.api.RLock;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * 缓存击穿防护实现
 *
 * @author David
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheBreakdownProtectionImpl implements CacheBreakdownProtection {

    private final DistributedLockManager lockManager;
    private final RedisCommonsProperties properties;

    /** 统计信息缓存 */
    private final ConcurrentHashMap<String, BreakdownProtectionStatsImpl> statsCache = new ConcurrentHashMap<>();

    @Override
    public <T> T executeWithBreakdownProtection(String key, Supplier<T> dataLoader, CacheWriter<T> cacheWriter) {
        long defaultWaitTime = properties.getLock().getDefaultWaitTime() * 1000; // 转换为毫秒
        return executeWithBreakdownProtection(key, dataLoader, cacheWriter, defaultWaitTime);
    }

    @Override
    public <T> T executeWithBreakdownProtection(String key, Supplier<T> dataLoader, CacheWriter<T> cacheWriter,
            long waitTime) {
        BreakdownProtectionStatsImpl stats = getOrCreateStats(key);
        stats.incrementTotalRequests();

        String lockKey = "breakdown:" + key;
        RLock lock = lockManager.getLock(lockKey);

        long startTime = System.currentTimeMillis();

        try {
            // 尝试获取锁
            stats.incrementLockWaitCount();
            boolean acquired = lock.tryLock(waitTime, TimeUnit.MILLISECONDS);

            if (acquired) {
                try {
                    stats.incrementLockAcquiredCount();
                    long waitDuration = System.currentTimeMillis() - startTime;
                    stats.addWaitTime(waitDuration);

                    log.debug("获取缓存击穿防护锁成功，键：{}，等待时间：{}毫秒", key, waitDuration);

                    // 获取锁成功，执行数据加载和缓存写入
                    T data = dataLoader.get();

                    if (data != null) {
                        // 计算随机TTL防止雪崩
                        long baseTtl = properties.getCache().getDefaultTtl();
                        long randomTtl = calculateRandomTtl(baseTtl);

                        cacheWriter.write(key, data, randomTtl);
                        stats.incrementCacheRebuildCount();

                        log.debug("缓存重建成功，键：{}，生存时间：{}秒", key, randomTtl);
                    }

                    return data;
                } finally {
                    lock.unlock();
                }
            } else {
                // 获取锁超时
                stats.incrementLockTimeoutCount();
                log.warn("获取缓存击穿防护锁失败，键：{}，超时时间：{}毫秒", key, waitTime);

                // 锁超时，直接执行数据加载（降级策略）
                return dataLoader.get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("等待缓存击穿防护锁时被中断：{}", key, e);

            // 中断异常，直接执行数据加载
            return dataLoader.get();
        } catch (Exception e) {
            log.error("缓存击穿防护过程中发生错误，键：{}", key, e);

            // 其他异常，直接执行数据加载
            return dataLoader.get();
        }
    }

    @Override
    public BreakdownProtectionStats getStats(String key) {
        return getOrCreateStats(key);
    }

    /**
     * 计算随机TTL防止雪崩
     */
    private long calculateRandomTtl(long baseTtl) {
        // 添加10-30%的随机抖动
        double jitter = 0.1 + Math.random() * 0.2; // 10%-30%
        return (long) (baseTtl * (1 + jitter));
    }

    /**
     * 获取或创建统计信息
     */
    private BreakdownProtectionStatsImpl getOrCreateStats(String key) {
        return statsCache.computeIfAbsent(key, BreakdownProtectionStatsImpl::new);
    }

    /**
     * 击穿防护统计信息实现
     */
    private static class BreakdownProtectionStatsImpl implements BreakdownProtectionStats {

        private final String key;
        private final AtomicLong totalRequests = new AtomicLong(0);
        private final AtomicLong lockWaitCount = new AtomicLong(0);
        private final AtomicLong lockAcquiredCount = new AtomicLong(0);
        private final AtomicLong lockTimeoutCount = new AtomicLong(0);
        private final AtomicLong cacheRebuildCount = new AtomicLong(0);
        private final AtomicLong totalWaitTime = new AtomicLong(0);

        public BreakdownProtectionStatsImpl(String key) {
            this.key = key;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public long getTotalRequests() {
            return totalRequests.get();
        }

        @Override
        public long getLockWaitCount() {
            return lockWaitCount.get();
        }

        @Override
        public long getLockAcquiredCount() {
            return lockAcquiredCount.get();
        }

        @Override
        public long getLockTimeoutCount() {
            return lockTimeoutCount.get();
        }

        @Override
        public long getCacheRebuildCount() {
            return cacheRebuildCount.get();
        }

        @Override
        public double getAverageWaitTime() {
            long count = lockAcquiredCount.get();
            return count > 0 ? (double) totalWaitTime.get() / count : 0.0;
        }

        public void incrementTotalRequests() {
            totalRequests.incrementAndGet();
        }

        public void incrementLockWaitCount() {
            lockWaitCount.incrementAndGet();
        }

        public void incrementLockAcquiredCount() {
            lockAcquiredCount.incrementAndGet();
        }

        public void incrementLockTimeoutCount() {
            lockTimeoutCount.incrementAndGet();
        }

        public void incrementCacheRebuildCount() {
            cacheRebuildCount.incrementAndGet();
        }

        public void addWaitTime(long waitTime) {
            totalWaitTime.addAndGet(waitTime);
        }
    }
}