package com.david.commons.redis.cache.protection.interfaces;

import java.util.function.Supplier;

/**
 * 缓存击穿防护接口
 *
 * <p>
 * 提供互斥锁机制防止缓存击穿，当热点数据缓存过期时，只允许一个线程重建缓存。
 *
 * @author David
 */
public interface CacheBreakdownProtection {

    /**
     * 执行带击穿防护的操作
     *
     * @param key         缓存键
     * @param dataLoader  数据加载器
     * @param cacheWriter 缓存写入器
     * @param <T>         数据类型
     * @return 数据
     */
    <T> T executeWithBreakdownProtection(String key, Supplier<T> dataLoader, CacheWriter<T> cacheWriter);

    /**
     * 执行带击穿防护的操作（带超时）
     *
     * @param key         缓存键
     * @param dataLoader  数据加载器
     * @param cacheWriter 缓存写入器
     * @param waitTime    等待时间（毫秒）
     * @param <T>         数据类型
     * @return 数据
     */
    <T> T executeWithBreakdownProtection(String key, Supplier<T> dataLoader, CacheWriter<T> cacheWriter, long waitTime);

    /**
     * 获取击穿防护统计信息
     *
     * @param key 缓存键
     * @return 统计信息
     */
    BreakdownProtectionStats getStats(String key);

    /**
     * 缓存写入器接口
     */
    @FunctionalInterface
    interface CacheWriter<T> {
        /**
         * 写入缓存
         *
         * @param key   缓存键
         * @param value 缓存值
         * @param ttl   TTL（秒）
         */
        void write(String key, T value, long ttl);
    }

    /**
     * 击穿防护统计信息
     */
    interface BreakdownProtectionStats {

        /**
         * 获取缓存键
         *
         * @return 缓存键
         */
        String getKey();

        /**
         * 获取总请求次数
         *
         * @return 总请求次数
         */
        long getTotalRequests();

        /**
         * 获取锁等待次数
         *
         * @return 锁等待次数
         */
        long getLockWaitCount();

        /**
         * 获取锁获取成功次数
         *
         * @return 锁获取成功次数
         */
        long getLockAcquiredCount();

        /**
         * 获取锁超时次数
         *
         * @return 锁超时次数
         */
        long getLockTimeoutCount();

        /**
         * 获取缓存重建次数
         *
         * @return 缓存重建次数
         */
        long getCacheRebuildCount();

        /**
         * 获取平均等待时间（毫秒）
         *
         * @return 平均等待时间
         */
        double getAverageWaitTime();
    }
}