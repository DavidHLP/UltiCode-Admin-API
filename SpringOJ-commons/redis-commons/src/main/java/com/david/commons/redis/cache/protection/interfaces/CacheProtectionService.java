package com.david.commons.redis.cache.protection.interfaces;

import java.util.function.Supplier;

/**
 * 缓存防护服务接口
 *
 * <p>
 * 统一的缓存防护服务，集成穿透、击穿、雪崩防护功能。
 *
 * @author David
 */
public interface CacheProtectionService {

    /**
     * 执行带全面防护的缓存操作
     *
     * @param key         缓存键
     * @param dataLoader  数据加载器
     * @param cacheWriter 缓存写入器
     * @param <T>         数据类型
     * @return 数据
     */
    <T> T executeWithFullProtection(String key, Supplier<T> dataLoader,
            CacheBreakdownProtection.CacheWriter<T> cacheWriter);

    /**
     * 执行带全面防护的缓存操作
     *
     * @param key             缓存键
     * @param bloomFilterName 布隆过滤器名称
     * @param dataLoader      数据加载器
     * @param cacheWriter     缓存写入器
     * @param <T>             数据类型
     * @return 数据
     */
    <T> T executeWithFullProtection(String key, String bloomFilterName, Supplier<T> dataLoader,
            CacheBreakdownProtection.CacheWriter<T> cacheWriter);

    /**
     * 执行带全面防护的缓存操作（带配置）
     *
     * @param config 防护配置
     * @param <T>    数据类型
     * @return 数据
     */
    <T> T executeWithFullProtection(ProtectionConfig<T> config);

    /**
     * 获取综合防护统计信息
     *
     * @return 统计信息
     */
    CombinedProtectionStats getCombinedStats();

    /**
     * 防护配置
     */
    class ProtectionConfig<T> {
        private String key;
        private String bloomFilterName;
        private Supplier<T> dataLoader;
        private CacheBreakdownProtection.CacheWriter<T> cacheWriter;
        private Runnable nullValueHandler;
        private long lockWaitTime = -1; // -1表示使用默认值
        private boolean enablePenetrationProtection = true;
        private boolean enableBreakdownProtection = true;
        private boolean enableAvalancheProtection = true;

        public ProtectionConfig(String key, Supplier<T> dataLoader,
                CacheBreakdownProtection.CacheWriter<T> cacheWriter) {
            this.key = key;
            this.dataLoader = dataLoader;
            this.cacheWriter = cacheWriter;
        }

        // Getters and Setters
        public String getKey() {
            return key;
        }

        public ProtectionConfig<T> setKey(String key) {
            this.key = key;
            return this;
        }

        public String getBloomFilterName() {
            return bloomFilterName;
        }

        public ProtectionConfig<T> setBloomFilterName(String bloomFilterName) {
            this.bloomFilterName = bloomFilterName;
            return this;
        }

        public Supplier<T> getDataLoader() {
            return dataLoader;
        }

        public ProtectionConfig<T> setDataLoader(Supplier<T> dataLoader) {
            this.dataLoader = dataLoader;
            return this;
        }

        public CacheBreakdownProtection.CacheWriter<T> getCacheWriter() {
            return cacheWriter;
        }

        public ProtectionConfig<T> setCacheWriter(CacheBreakdownProtection.CacheWriter<T> cacheWriter) {
            this.cacheWriter = cacheWriter;
            return this;
        }

        public Runnable getNullValueHandler() {
            return nullValueHandler;
        }

        public ProtectionConfig<T> setNullValueHandler(Runnable nullValueHandler) {
            this.nullValueHandler = nullValueHandler;
            return this;
        }

        public long getLockWaitTime() {
            return lockWaitTime;
        }

        public ProtectionConfig<T> setLockWaitTime(long lockWaitTime) {
            this.lockWaitTime = lockWaitTime;
            return this;
        }

        public boolean isEnablePenetrationProtection() {
            return enablePenetrationProtection;
        }

        public ProtectionConfig<T> setEnablePenetrationProtection(boolean enablePenetrationProtection) {
            this.enablePenetrationProtection = enablePenetrationProtection;
            return this;
        }

        public boolean isEnableBreakdownProtection() {
            return enableBreakdownProtection;
        }

        public ProtectionConfig<T> setEnableBreakdownProtection(boolean enableBreakdownProtection) {
            this.enableBreakdownProtection = enableBreakdownProtection;
            return this;
        }

        public boolean isEnableAvalancheProtection() {
            return enableAvalancheProtection;
        }

        public ProtectionConfig<T> setEnableAvalancheProtection(boolean enableAvalancheProtection) {
            this.enableAvalancheProtection = enableAvalancheProtection;
            return this;
        }
    }

    /**
     * 综合防护统计信息
     */
    interface CombinedProtectionStats {

        /**
         * 获取穿透防护统计
         *
         * @param filterName 过滤器名称
         * @return 穿透防护统计
         */
        CachePenetrationProtection.PenetrationProtectionStats getPenetrationStats(String filterName);

        /**
         * 获取击穿防护统计
         *
         * @param key 缓存键
         * @return 击穿防护统计
         */
        CacheBreakdownProtection.BreakdownProtectionStats getBreakdownStats(String key);

        /**
         * 获取雪崩防护统计
         *
         * @return 雪崩防护统计
         */
        CacheAvalancheProtection.AvalancheProtectionStats getAvalancheStats();

        /**
         * 获取总防护请求数
         *
         * @return 总请求数
         */
        long getTotalProtectionRequests();

        /**
         * 获取防护成功率
         *
         * @return 成功率
         */
        double getProtectionSuccessRate();
    }
}