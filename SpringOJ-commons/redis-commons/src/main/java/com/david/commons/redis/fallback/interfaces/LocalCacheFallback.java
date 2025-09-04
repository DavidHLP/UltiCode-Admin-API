package com.david.commons.redis.fallback.interfaces;

import java.util.function.Supplier;

/**
 * 本地缓存降级接口
 *
 * <p>
 * 当Redis不可用时，提供本地缓存作为降级方案。
 *
 * @author David
 */
public interface LocalCacheFallback {

    /**
     * 获取缓存值
     *
     * @param key  缓存键
     * @param type 值类型
     * @param <T>  值类型
     * @return 缓存值，如果不存在则返回null
     */
    <T> T get(String key, Class<T> type);

    /**
     * 设置缓存值
     *
     * @param key        缓存键
     * @param value      缓存值
     * @param ttlSeconds TTL（秒）
     * @param <T>        值类型
     */
    <T> void put(String key, T value, long ttlSeconds);

    /**
     * 删除缓存值
     *
     * @param key 缓存键
     */
    void evict(String key);

    /**
     * 清空所有缓存
     */
    void clear();

    /**
     * 执行带本地缓存降级的操作
     *
     * @param key        缓存键
     * @param dataLoader 数据加载器
     * @param ttlSeconds TTL（秒）
     * @param type       值类型
     * @param <T>        值类型
     * @return 数据
     */
    <T> T executeWithFallback(String key, Supplier<T> dataLoader, long ttlSeconds, Class<T> type);

    /**
     * 获取本地缓存统计信息
     *
     * @return 统计信息
     */
    LocalCacheStats getStats();

    /**
     * 本地缓存统计信息
     */
    interface LocalCacheStats {

        /**
         * 获取缓存大小
         *
         * @return 缓存大小
         */
        long getSize();

        /**
         * 获取最大大小
         *
         * @return 最大大小
         */
        long getMaxSize();

        /**
         * 获取命中次数
         *
         * @return 命中次数
         */
        long getHitCount();

        /**
         * 获取未命中次数
         *
         * @return 未命中次数
         */
        long getMissCount();

        /**
         * 获取命中率
         *
         * @return 命中率
         */
        double getHitRate();

        /**
         * 获取驱逐次数
         *
         * @return 驱逐次数
         */
        long getEvictionCount();

        /**
         * 获取平均加载时间（纳秒）
         *
         * @return 平均加载时间
         */
        double getAverageLoadTime();
    }
}