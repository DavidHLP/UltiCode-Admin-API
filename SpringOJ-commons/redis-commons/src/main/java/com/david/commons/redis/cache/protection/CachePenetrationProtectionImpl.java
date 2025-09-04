package com.david.commons.redis.cache.protection;

import com.david.commons.redis.config.RedisCommonsProperties;

import com.david.commons.redis.cache.protection.interfaces.BloomFilterManager;
import com.david.commons.redis.cache.protection.interfaces.CachePenetrationProtection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * 缓存穿透防护实现
 *
 * @author David
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CachePenetrationProtectionImpl implements CachePenetrationProtection {

    private final BloomFilterManager bloomFilterManager;
    private final RedisCommonsProperties properties;

    /** 统计信息缓存 */
    private final ConcurrentHashMap<String, PenetrationProtectionStatsImpl> statsCache = new ConcurrentHashMap<>();

    @Override
    public boolean mightExist(String filterName, Object key) {
        if (!properties.getProtection().isEnableBloomFilter()) {
            return true; // 如果未启用布隆过滤器，则认为都可能存在
        }

        try {
            PenetrationProtectionStatsImpl stats = getOrCreateStats(filterName);
            stats.incrementTotalQueries();

            BloomFilterManager.BloomFilter<Object> bloomFilter = bloomFilterManager.getOrCreateBloomFilter(filterName);
            boolean mightExist = bloomFilter.mightContain(key);

            if (mightExist) {
                stats.incrementPassedQueries();
                log.debug("键可能存在于布隆过滤器中: {}, 键: {}", filterName, key);
            } else {
                stats.incrementFilteredQueries();
                log.debug("键被布隆过滤器过滤: {}, 键: {}", filterName, key);
            }

            return mightExist;
        } catch (Exception e) {
            log.error("检查布隆过滤器时发生错误: {}, 键: {}", filterName, key, e);
            // 发生异常时，为了安全起见，认为可能存在
            return true;
        }
    }

    @Override
    public boolean addKey(String filterName, Object key) {
        if (!properties.getProtection().isEnableBloomFilter()) {
            return true; // 如果未启用布隆过滤器，则认为添加成功
        }

        try {
            BloomFilterManager.BloomFilter<Object> bloomFilter = bloomFilterManager.getOrCreateBloomFilter(filterName);
            boolean added = bloomFilter.add(key);

            if (added) {
                log.debug("已将键添加到布隆过滤器: {}, 键: {}", filterName, key);
            }

            return added;
        } catch (Exception e) {
            log.error("向布隆过滤器添加键时发生错误: {}, 键: {}", filterName, key, e);
            return false;
        }
    }

    @Override
    public <T> T executeWithProtection(String filterName, Object key, Supplier<T> dataLoader) {
        return executeWithProtection(filterName, key, dataLoader, null);
    }

    @Override
    public <T> T executeWithProtection(String filterName, Object key, Supplier<T> dataLoader,
            Runnable nullValueHandler) {
        PenetrationProtectionStatsImpl stats = getOrCreateStats(filterName);

        // 检查布隆过滤器
        if (!mightExist(filterName, key)) {
            // 布隆过滤器认为不存在，直接返回null
            stats.incrementNullQueries();
            if (nullValueHandler != null) {
                nullValueHandler.run();
            }
            return null;
        }

        // 布隆过滤器认为可能存在，执行数据加载
        try {
            stats.incrementDatabaseQueries();
            T result = dataLoader.get();

            if (result != null) {
                // 数据存在，添加到布隆过滤器（如果还没有的话）
                addKey(filterName, key);
                stats.incrementCacheHits();
                log.debug("数据加载成功: {}, 键: {}", filterName, key);
            } else {
                // 数据不存在，记录空值查询
                stats.incrementNullQueries();
                if (nullValueHandler != null) {
                    nullValueHandler.run();
                }
                log.debug("未找到数据: {}, 键: {}", filterName, key);
            }

            return result;
        } catch (Exception e) {
            log.error("加载数据时发生错误: {}, 键: {}", filterName, key, e);
            throw e;
        }
    }

    @Override
    public long addKeys(String filterName, Iterable<?> keys) {
        if (!properties.getProtection().isEnableBloomFilter()) {
            return 0;
        }

        long addedCount = 0;
        BloomFilterManager.BloomFilter<Object> bloomFilter = bloomFilterManager.getOrCreateBloomFilter(filterName);

        for (Object key : keys) {
            try {
                if (bloomFilter.add(key)) {
                    addedCount++;
                }
            } catch (Exception e) {
                log.error("向布隆过滤器添加键时发生错误: {}, 键: {}", filterName, key, e);
            }
        }

        log.info("已将 {} 个键添加到布隆过滤器: {}", addedCount, filterName);
        return addedCount;
    }

    @Override
    public long warmUp(String filterName, Supplier<Iterable<?>> keyProvider) {
        if (!properties.getProtection().isEnableBloomFilter()) {
            return 0;
        }

        try {
            log.info("开始预热布隆过滤器: {}", filterName);
            Iterable<?> keys = keyProvider.get();
            long addedCount = addKeys(filterName, keys);
            log.info("完成布隆过滤器预热: {}, 添加了 {} 个键", filterName, addedCount);
            return addedCount;
        } catch (Exception e) {
            log.error("布隆过滤器预热过程中发生错误: {}", filterName, e);
            return 0;
        }
    }

    @Override
    public PenetrationProtectionStats getStats(String filterName) {
        return getOrCreateStats(filterName);
    }

    /**
     * 获取或创建统计信息
     */
    private PenetrationProtectionStatsImpl getOrCreateStats(String filterName) {
        return statsCache.computeIfAbsent(filterName, PenetrationProtectionStatsImpl::new);
    }

    /**
     * 防穿透统计信息实现
     */
    private static class PenetrationProtectionStatsImpl implements PenetrationProtectionStats {

        private final String filterName;
        private final AtomicLong totalQueries = new AtomicLong(0);
        private final AtomicLong filteredQueries = new AtomicLong(0);
        private final AtomicLong passedQueries = new AtomicLong(0);
        private final AtomicLong databaseQueries = new AtomicLong(0);
        private final AtomicLong cacheHits = new AtomicLong(0);
        private final AtomicLong nullQueries = new AtomicLong(0);

        public PenetrationProtectionStatsImpl(String filterName) {
            this.filterName = filterName;
        }

        @Override
        public String getFilterName() {
            return filterName;
        }

        @Override
        public long getTotalQueries() {
            return totalQueries.get();
        }

        @Override
        public long getFilteredQueries() {
            return filteredQueries.get();
        }

        @Override
        public long getPassedQueries() {
            return passedQueries.get();
        }

        @Override
        public double getFilterRate() {
            long total = totalQueries.get();
            return total > 0 ? (double) filteredQueries.get() / total : 0.0;
        }

        @Override
        public long getDatabaseQueries() {
            return databaseQueries.get();
        }

        @Override
        public long getCacheHits() {
            return cacheHits.get();
        }

        @Override
        public long getNullQueries() {
            return nullQueries.get();
        }

        public void incrementTotalQueries() {
            totalQueries.incrementAndGet();
        }

        public void incrementFilteredQueries() {
            filteredQueries.incrementAndGet();
        }

        public void incrementPassedQueries() {
            passedQueries.incrementAndGet();
        }

        public void incrementDatabaseQueries() {
            databaseQueries.incrementAndGet();
        }

        public void incrementCacheHits() {
            cacheHits.incrementAndGet();
        }

        public void incrementNullQueries() {
            nullQueries.incrementAndGet();
        }
    }
}