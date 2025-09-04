package com.david.commons.redis.fallback;

import com.david.commons.redis.config.RedisCommonsProperties;
import com.david.commons.redis.fallback.interfaces.LocalCacheFallback;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * 基于内存的本地缓存降级实现
 *
 * @author David
 */
@Slf4j
@Component
public class CaffeineLocalCacheFallback implements LocalCacheFallback {

    private final RedisCommonsProperties properties;
    private final ObjectMapper objectMapper;

    /** 缓存存储 */
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /** 统计信息 */
    private final LocalCacheStatsImpl stats = new LocalCacheStatsImpl();

    /** 定时清理器 */
    private final ScheduledExecutorService cleanupExecutor =
            Executors.newSingleThreadScheduledExecutor(
                    r -> {
                        Thread t = new Thread(r, "local-cache-cleanup");
                        t.setDaemon(true);
                        return t;
                    });

    public CaffeineLocalCacheFallback(RedisCommonsProperties properties) {
        this.properties = properties;
        this.objectMapper = new ObjectMapper();

        // 启动定时清理任务
        cleanupExecutor.scheduleWithFixedDelay(this::cleanup, 60, 60, TimeUnit.SECONDS);
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        CacheEntry entry = cache.get(key);

        if (entry == null) {
            stats.incrementMissCount();
            return null;
        }

        if (entry.isExpired()) {
            cache.remove(key);
            stats.incrementMissCount();
            stats.incrementEvictionCount();
            return null;
        }

        stats.incrementHitCount();

        try {
            if (type.isInstance(entry.getValue())) {
                return type.cast(entry.getValue());
            } else {
                // 尝试JSON转换
                String json = objectMapper.writeValueAsString(entry.getValue());
                return objectMapper.readValue(json, type);
            }
        } catch (Exception e) {
            log.error("Error deserializing cached value for key: {}", key, e);
            cache.remove(key);
            return null;
        }
    }

    @Override
    public <T> void put(String key, T value, long ttlSeconds) {
        if (value == null) {
            return;
        }

        // 检查缓存大小限制
        int maxSize = properties.getCache().getLocalCacheMaxSize();
        if (cache.size() >= maxSize) {
            // 简单的LRU：移除最旧的条目
            evictOldest();
        }

        long expirationTime = System.currentTimeMillis() + (ttlSeconds * 1000);
        cache.put(key, new CacheEntry(value, expirationTime));

        log.debug("Put value to local cache: key={}, ttl={}s", key, ttlSeconds);
    }

    @Override
    public void evict(String key) {
        CacheEntry removed = cache.remove(key);
        if (removed != null) {
            stats.incrementEvictionCount();
            log.debug("Evicted from local cache: key={}", key);
        }
    }

    @Override
    public void clear() {
        int size = cache.size();
        cache.clear();
        stats.addEvictionCount(size);
        log.info("Cleared local cache, removed {} entries", size);
    }

    @Override
    public <T> T executeWithFallback(
            String key, Supplier<T> dataLoader, long ttlSeconds, Class<T> type) {
        // 首先尝试从本地缓存获取
        T cachedValue = get(key, type);
        if (cachedValue != null) {
            return cachedValue;
        }

        // 本地缓存未命中，执行数据加载
        long startTime = System.nanoTime();
        try {
            T value = dataLoader.get();
            long loadTime = System.nanoTime() - startTime;
            stats.addLoadTime(loadTime);

            if (value != null) {
                put(key, value, ttlSeconds);
            }

            return value;
        } catch (Exception e) {
            long loadTime = System.nanoTime() - startTime;
            stats.addLoadTime(loadTime);
            throw e;
        }
    }

    @Override
    public LocalCacheStats getStats() {
        stats.updateSize(cache.size());
        stats.updateMaxSize(properties.getCache().getLocalCacheMaxSize());
        return stats;
    }

    /** 清理过期条目 */
    private void cleanup() {
        long now = System.currentTimeMillis();
        int removedCount = 0;

        for (var iterator = cache.entrySet().iterator(); iterator.hasNext(); ) {
            var entry = iterator.next();
            if (entry.getValue().isExpired(now)) {
                iterator.remove();
                removedCount++;
            }
        }

        if (removedCount > 0) {
            stats.addEvictionCount(removedCount);
            log.debug("Cleaned up {} expired entries from local cache", removedCount);
        }
    }

    /** 驱逐最旧的条目 */
    private void evictOldest() {
        String oldestKey = null;
        long oldestTime = Long.MAX_VALUE;

        for (var entry : cache.entrySet()) {
            long creationTime = entry.getValue().getCreationTime();
            if (creationTime < oldestTime) {
                oldestTime = creationTime;
                oldestKey = entry.getKey();
            }
        }

        if (oldestKey != null) {
            evict(oldestKey);
        }
    }

    /** 缓存条目 */
    private static class CacheEntry {
        @Getter
        private final Object value;
        private final long expirationTime;
        @Getter
        private final long creationTime;

        public CacheEntry(Object value, long expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
            this.creationTime = System.currentTimeMillis();
        }

        public boolean isExpired() {
            return isExpired(System.currentTimeMillis());
        }

        public boolean isExpired(long now) {
            return now >= expirationTime;
        }
    }

    /** 本地缓存统计信息实现 */
    private static class LocalCacheStatsImpl implements LocalCacheStats {

        private final AtomicLong hitCount = new AtomicLong(0);
        private final AtomicLong missCount = new AtomicLong(0);
        private final AtomicLong evictionCount = new AtomicLong(0);
        private final AtomicLong totalLoadTime = new AtomicLong(0);
        private final AtomicLong loadCount = new AtomicLong(0);

        private volatile long size = 0;
        private volatile long maxSize = 0;

        @Override
        public long getSize() {
            return size;
        }

        @Override
        public long getMaxSize() {
            return maxSize;
        }

        @Override
        public long getHitCount() {
            return hitCount.get();
        }

        @Override
        public long getMissCount() {
            return missCount.get();
        }

        @Override
        public double getHitRate() {
            long hits = hitCount.get();
            long total = hits + missCount.get();
            return total > 0 ? (double) hits / total : 0.0;
        }

        @Override
        public long getEvictionCount() {
            return evictionCount.get();
        }

        @Override
        public double getAverageLoadTime() {
            long count = loadCount.get();
            return count > 0 ? (double) totalLoadTime.get() / count : 0.0;
        }

        public void incrementHitCount() {
            hitCount.incrementAndGet();
        }

        public void incrementMissCount() {
            missCount.incrementAndGet();
        }

        public void incrementEvictionCount() {
            evictionCount.incrementAndGet();
        }

        public void addEvictionCount(long count) {
            evictionCount.addAndGet(count);
        }

        public void addLoadTime(long nanos) {
            totalLoadTime.addAndGet(nanos);
            loadCount.incrementAndGet();
        }

        public void updateSize(long size) {
            this.size = size;
        }

        public void updateMaxSize(long maxSize) {
            this.maxSize = maxSize;
        }
    }
}
