package com.david.commons.redis.cache.protection.nexts;

import com.david.commons.redis.config.RedisCommonsProperties;
import com.david.commons.redis.exception.RedisCommonsException;

import com.david.commons.redis.cache.protection.interfaces.BloomFilterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于 Redisson 的布隆过滤器管理器实现
 *
 * @author David
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonBloomFilterManager implements BloomFilterManager {

    private final RedissonClient redissonClient;
    private final RedisCommonsProperties properties;

    /** 布隆过滤器缓存 */
    private final ConcurrentHashMap<String, RedissonBloomFilter<?>> bloomFilterCache = new ConcurrentHashMap<>();

    /** 统计信息缓存 */
    private final ConcurrentHashMap<String, BloomFilterStatsImpl> statsCache = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> BloomFilter<T> getOrCreateBloomFilter(String filterName) {
        return (BloomFilter<T>) bloomFilterCache.computeIfAbsent(filterName, name -> {
            long expectedInsertions = properties.getProtection().getBloomFilterExpectedInsertions();
            double fpp = properties.getProtection().getBloomFilterFpp();
            return createBloomFilter(name, expectedInsertions, fpp);
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> BloomFilter<T> getOrCreateBloomFilter(String filterName, long expectedInsertions, double fpp) {
        return (BloomFilter<T>) bloomFilterCache.computeIfAbsent(filterName,
                name -> createBloomFilter(name, expectedInsertions, fpp));
    }

    @Override
    public boolean deleteBloomFilter(String filterName) {
        try {
            // 从缓存中移除
            bloomFilterCache.remove(filterName);
            statsCache.remove(filterName);

            // 删除 Redis 中的布隆过滤器
            RBloomFilter<Object> rBloomFilter = redissonClient.getBloomFilter(getRedisKey(filterName));
            boolean deleted = rBloomFilter.delete();

            if (deleted) {
                log.info("Successfully deleted bloom filter: {}", filterName);
            } else {
                log.warn("Bloom filter not found for deletion: {}", filterName);
            }

            return deleted;
        } catch (Exception e) {
            log.error("Failed to delete bloom filter: {}", filterName, e);
            throw new RedisCommonsException("Failed to delete bloom filter: " + filterName, e);
        }
    }

    @Override
    public boolean exists(String filterName) {
        try {
            RBloomFilter<Object> rBloomFilter = redissonClient.getBloomFilter(getRedisKey(filterName));
            return rBloomFilter.isExists();
        } catch (Exception e) {
            log.error("Failed to check bloom filter existence: {}", filterName, e);
            return false;
        }
    }

    @Override
    public BloomFilterStats getStats(String filterName) {
        return statsCache.computeIfAbsent(filterName, name -> new BloomFilterStatsImpl(name));
    }

    /**
     * 创建布隆过滤器
     */
    private <T> RedissonBloomFilter<T> createBloomFilter(String filterName, long expectedInsertions, double fpp) {
        try {
            String redisKey = getRedisKey(filterName);
            RBloomFilter<T> rBloomFilter = redissonClient.getBloomFilter(redisKey);

            // 如果布隆过滤器不存在，则初始化
            if (!rBloomFilter.isExists()) {
                boolean initialized = rBloomFilter.tryInit(expectedInsertions, fpp);
                if (!initialized) {
                    throw new RedisCommonsException("Failed to initialize bloom filter: " + filterName);
                }
                log.info("Initialized bloom filter: {} with expectedInsertions={}, fpp={}",
                        filterName, expectedInsertions, fpp);
            }

            // 创建统计信息
            BloomFilterStatsImpl stats = new BloomFilterStatsImpl(filterName);
            stats.setExpectedInsertions(expectedInsertions);
            stats.setFalsePositiveProbability(fpp);
            statsCache.put(filterName, stats);

            return new RedissonBloomFilter<>(filterName, rBloomFilter, stats);
        } catch (Exception e) {
            log.error("Failed to create bloom filter: {}", filterName, e);
            throw new RedisCommonsException("Failed to create bloom filter: " + filterName, e);
        }
    }

    /**
     * 获取 Redis 键名
     */
    private String getRedisKey(String filterName) {
        return properties.getKeyPrefix() + "bloom:" + filterName;
    }

    /**
     * Redisson 布隆过滤器包装类
     */
    private static class RedissonBloomFilter<T> implements BloomFilter<T> {

        private final String name;
        private final RBloomFilter<T> rBloomFilter;
        private final BloomFilterStatsImpl stats;

        public RedissonBloomFilter(String name, RBloomFilter<T> rBloomFilter, BloomFilterStatsImpl stats) {
            this.name = name;
            this.rBloomFilter = rBloomFilter;
            this.stats = stats;
        }

        @Override
        public boolean add(T element) {
            try {
                boolean added = rBloomFilter.add(element);
                if (added) {
                    stats.incrementSize();
                }
                return added;
            } catch (Exception e) {
                log.error("Failed to add element to bloom filter: {}", name, e);
                throw new RedisCommonsException("Failed to add element to bloom filter: " + name, e);
            }
        }

        @Override
        public boolean mightContain(T element) {
            try {
                stats.incrementQueryCount();
                boolean contains = rBloomFilter.contains(element);
                if (contains) {
                    stats.incrementHitCount();
                }
                return contains;
            } catch (Exception e) {
                log.error("Failed to check element in bloom filter: {}", name, e);
                throw new RedisCommonsException("Failed to check element in bloom filter: " + name, e);
            }
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public long getExpectedInsertions() {
            return stats.getExpectedInsertions();
        }

        @Override
        public double getFalsePositiveProbability() {
            return stats.getFalsePositiveProbability();
        }

        @Override
        public long size() {
            return rBloomFilter.count();
        }

        @Override
        public void clear() {
            try {
                rBloomFilter.delete();
                stats.reset();
                log.info("Cleared bloom filter: {}", name);
            } catch (Exception e) {
                log.error("Failed to clear bloom filter: {}", name, e);
                throw new RedisCommonsException("Failed to clear bloom filter: " + name, e);
            }
        }
    }

    /**
     * 布隆过滤器统计信息实现
     */
    private static class BloomFilterStatsImpl implements BloomFilterStats {

        private final String name;
        private final AtomicLong size = new AtomicLong(0);
        private final AtomicLong queryCount = new AtomicLong(0);
        private final AtomicLong hitCount = new AtomicLong(0);

        private volatile long expectedInsertions;
        private volatile double falsePositiveProbability;

        public BloomFilterStatsImpl(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public long getSize() {
            return size.get();
        }

        @Override
        public long getExpectedInsertions() {
            return expectedInsertions;
        }

        @Override
        public double getFalsePositiveProbability() {
            return falsePositiveProbability;
        }

        @Override
        public long getQueryCount() {
            return queryCount.get();
        }

        @Override
        public long getHitCount() {
            return hitCount.get();
        }

        @Override
        public double getHitRate() {
            long queries = queryCount.get();
            return queries > 0 ? (double) hitCount.get() / queries : 0.0;
        }

        public void setExpectedInsertions(long expectedInsertions) {
            this.expectedInsertions = expectedInsertions;
        }

        public void setFalsePositiveProbability(double falsePositiveProbability) {
            this.falsePositiveProbability = falsePositiveProbability;
        }

        public void incrementSize() {
            size.incrementAndGet();
        }

        public void incrementQueryCount() {
            queryCount.incrementAndGet();
        }

        public void incrementHitCount() {
            hitCount.incrementAndGet();
        }

        public void reset() {
            size.set(0);
            queryCount.set(0);
            hitCount.set(0);
        }
    }
}