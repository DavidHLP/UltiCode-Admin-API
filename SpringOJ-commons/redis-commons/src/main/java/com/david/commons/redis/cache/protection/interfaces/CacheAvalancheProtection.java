package com.david.commons.redis.cache.protection.interfaces;

/**
 * 缓存雪崩防护接口
 *
 * <p>
 * 提供随机TTL和缓存预热机制防止缓存雪崩。
 *
 * @author David
 */
public interface CacheAvalancheProtection {

    /**
     * 计算随机TTL
     *
     * @param baseTtl 基础TTL（秒）
     * @return 随机TTL（秒）
     */
    long calculateRandomTtl(long baseTtl);

    /**
     * 计算随机TTL
     *
     * @param baseTtl       基础TTL（秒）
     * @param jitterPercent 抖动百分比（0-100）
     * @return 随机TTL（秒）
     */
    long calculateRandomTtl(long baseTtl, int jitterPercent);

    /**
     * 预热缓存
     *
     * @param warmupTask 预热任务
     * @return 预热结果
     */
    WarmupResult warmupCache(WarmupTask warmupTask);

    /**
     * 异步预热缓存
     *
     * @param warmupTask 预热任务
     */
    void warmupCacheAsync(WarmupTask warmupTask);

    /**
     * 检查是否需要预热
     *
     * @param key       缓存键
     * @param ttl       当前TTL（秒）
     * @param threshold 预热阈值（秒）
     * @return 是否需要预热
     */
    boolean shouldWarmup(String key, long ttl, long threshold);

    /**
     * 获取雪崩防护统计信息
     *
     * @return 统计信息
     */
    AvalancheProtectionStats getStats();

    /**
     * 预热任务接口
     */
    @FunctionalInterface
    interface WarmupTask {
        /**
         * 执行预热
         *
         * @return 预热结果
         */
        WarmupResult execute();
    }

    /**
     * 预热结果
     */
    interface WarmupResult {

        /**
         * 是否成功
         *
         * @return 是否成功
         */
        boolean success();

        /**
         * 预热的键数量
         *
         * @return 键数量
         */
        long warmupCount();

        /**
         * 预热耗时（毫秒）
         *
         * @return 耗时
         */
        long duration();

        /**
         * 错误信息
         *
         * @return 错误信息
         */
        String errorMessage();
    }

    /**
     * 雪崩防护统计信息
     */
    interface AvalancheProtectionStats {

        /**
         * 获取TTL计算次数
         *
         * @return TTL计算次数
         */
        long getTtlCalculationCount();

        /**
         * 获取预热任务执行次数
         *
         * @return 预热任务执行次数
         */
        long getWarmupTaskCount();

        /**
         * 获取预热成功次数
         *
         * @return 预热成功次数
         */
        long getWarmupSuccessCount();

        /**
         * 获取预热失败次数
         *
         * @return 预热失败次数
         */
        long getWarmupFailureCount();

        /**
         * 获取总预热键数量
         *
         * @return 总预热键数量
         */
        long getTotalWarmupKeys();

        /**
         * 获取平均预热耗时（毫秒）
         *
         * @return 平均预热耗时
         */
        double getAverageWarmupDuration();
    }
}