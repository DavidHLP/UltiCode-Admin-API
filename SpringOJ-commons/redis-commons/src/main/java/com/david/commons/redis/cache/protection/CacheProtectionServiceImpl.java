package com.david.commons.redis.cache.protection;

import com.david.commons.redis.cache.protection.interfaces.CacheAvalancheProtection;
import com.david.commons.redis.cache.protection.interfaces.CacheBreakdownProtection;
import com.david.commons.redis.cache.protection.interfaces.CachePenetrationProtection;
import com.david.commons.redis.cache.protection.interfaces.CacheProtectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * 缓存防护服务实现
 *
 * @author David
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheProtectionServiceImpl implements CacheProtectionService {

    private final CachePenetrationProtection penetrationProtection;
    private final CacheBreakdownProtection breakdownProtection;
    private final CacheAvalancheProtection avalancheProtection;

    private final CombinedProtectionStatsImpl combinedStats = new CombinedProtectionStatsImpl();

    @Override
    public <T> T executeWithFullProtection(String key, Supplier<T> dataLoader,
            CacheBreakdownProtection.CacheWriter<T> cacheWriter) {
        return executeWithFullProtection(key, key, dataLoader, cacheWriter);
    }

    @Override
    public <T> T executeWithFullProtection(String key, String bloomFilterName, Supplier<T> dataLoader,
            CacheBreakdownProtection.CacheWriter<T> cacheWriter) {
        ProtectionConfig<T> config = new ProtectionConfig<>(key, dataLoader, cacheWriter)
                .setBloomFilterName(bloomFilterName);
        return executeWithFullProtection(config);
    }

    @Override
    public <T> T executeWithFullProtection(ProtectionConfig<T> config) {
        combinedStats.incrementTotalRequests();

        try {
            String key = config.getKey();
            String bloomFilterName = config.getBloomFilterName() != null ? config.getBloomFilterName() : key;

            log.debug("正在对键 {} 执行完整的缓存保护操作", key);

            // 1. 穿透防护：检查布隆过滤器
            if (config.isEnablePenetrationProtection()) {
                T result = penetrationProtection.executeWithProtection(
                        bloomFilterName,
                        key,
                        () -> executeWithBreakdownAndAvalancheProtection(config),
                        config.getNullValueHandler());

                if (result != null) {
                    combinedStats.incrementSuccessRequests();
                }

                return result;
            } else {
                // 跳过穿透防护，直接执行击穿和雪崩防护
                T result = executeWithBreakdownAndAvalancheProtection(config);

                if (result != null) {
                    combinedStats.incrementSuccessRequests();
                }

                return result;
            }
        } catch (Exception e) {
            log.error("键 {} 的完整缓存保护操作出错", config.getKey(), e);
            throw e;
        }
    }

    @Override
    public CombinedProtectionStats getCombinedStats() {
        return combinedStats;
    }

    /**
     * 执行击穿和雪崩防护
     */
    private <T> T executeWithBreakdownAndAvalancheProtection(ProtectionConfig<T> config) {
        if (config.isEnableBreakdownProtection()) {
            // 使用击穿防护
            CacheBreakdownProtection.CacheWriter<T> originalWriter = config.getCacheWriter();
            CacheBreakdownProtection.CacheWriter<T> avalancheWriter = createAvalancheProtectedWriter(originalWriter,
                    config);

            if (config.getLockWaitTime() > 0) {
                return breakdownProtection.executeWithBreakdownProtection(
                        config.getKey(),
                        config.getDataLoader(),
                        avalancheWriter,
                        config.getLockWaitTime());
            } else {
                return breakdownProtection.executeWithBreakdownProtection(
                        config.getKey(),
                        config.getDataLoader(),
                        avalancheWriter);
            }
        } else {
            // 跳过击穿防护，直接执行数据加载和雪崩防护写入
            T data = config.getDataLoader().get();

            if (data != null && config.isEnableAvalancheProtection()) {
                CacheBreakdownProtection.CacheWriter<T> avalancheWriter = createAvalancheProtectedWriter(
                        config.getCacheWriter(), config);
                avalancheWriter.write(config.getKey(), data, 0); // TTL将在writer中计算
            } else if (data != null) {
                config.getCacheWriter().write(config.getKey(), data, 0);
            }

            return data;
        }
    }

    /**
     * 创建带雪崩防护的缓存写入器
     */
    private <T> CacheBreakdownProtection.CacheWriter<T> createAvalancheProtectedWriter(
            CacheBreakdownProtection.CacheWriter<T> originalWriter,
            ProtectionConfig<T> config) {

        if (!config.isEnableAvalancheProtection()) {
            return originalWriter;
        }

        return (key, value, ttl) -> {
            // 如果TTL为0，使用默认TTL
            long actualTtl = ttl > 0 ? ttl : 3600; // 默认1小时

            // 应用雪崩防护：计算随机TTL
            long randomTtl = avalancheProtection.calculateRandomTtl(actualTtl);

            log.debug("已应用雪崩防护：原始 TTL={}秒，随机 TTL={}秒", actualTtl, randomTtl);

            originalWriter.write(key, value, randomTtl);
        };
    }

    /**
     * 综合防护统计信息实现
     */
    private class CombinedProtectionStatsImpl implements CombinedProtectionStats {

        private final AtomicLong totalRequests = new AtomicLong(0);
        private final AtomicLong successRequests = new AtomicLong(0);

        @Override
        public CachePenetrationProtection.PenetrationProtectionStats getPenetrationStats(String filterName) {
            return penetrationProtection.getStats(filterName);
        }

        @Override
        public CacheBreakdownProtection.BreakdownProtectionStats getBreakdownStats(String key) {
            return breakdownProtection.getStats(key);
        }

        @Override
        public CacheAvalancheProtection.AvalancheProtectionStats getAvalancheStats() {
            return avalancheProtection.getStats();
        }

        @Override
        public long getTotalProtectionRequests() {
            return totalRequests.get();
        }

        @Override
        public double getProtectionSuccessRate() {
            long total = totalRequests.get();
            return total > 0 ? (double) successRequests.get() / total : 0.0;
        }

        public void incrementTotalRequests() {
            totalRequests.incrementAndGet();
        }

        public void incrementSuccessRequests() {
            successRequests.incrementAndGet();
        }
    }
}