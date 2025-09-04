package com.david.commons.redis.cache.protection.interfaces;

/**
 * 限流器接口
 *
 * <p>
 * 提供令牌桶和滑动窗口限流算法实现。
 *
 * @author David
 */
public interface RateLimiter {

    /**
     * 尝试获取许可
     *
     * @param key 限流键
     * @return 是否获取成功
     */
    boolean tryAcquire(String key);

    /**
     * 尝试获取指定数量的许可
     *
     * @param key     限流键
     * @param permits 许可数量
     * @return 是否获取成功
     */
    boolean tryAcquire(String key, int permits);

    /**
     * 尝试获取许可（带超时）
     *
     * @param key       限流键
     * @param timeoutMs 超时时间（毫秒）
     * @return 是否获取成功
     */
    boolean tryAcquire(String key, long timeoutMs);

    /**
     * 获取剩余许可数
     *
     * @param key 限流键
     * @return 剩余许可数
     */
    long getAvailablePermits(String key);

    /**
     * 获取限流统计信息
     *
     * @param key 限流键
     * @return 统计信息
     */
    RateLimitStats getStats(String key);

    /**
     * 限流统计信息
     */
    interface RateLimitStats {

        /**
         * 获取限流键
         *
         * @return 限流键
         */
        String getKey();

        /**
         * 获取总请求数
         *
         * @return 总请求数
         */
        long getTotalRequests();

        /**
         * 获取允许的请求数
         *
         * @return 允许的请求数
         */
        long getAllowedRequests();

        /**
         * 获取被拒绝的请求数
         *
         * @return 被拒绝的请求数
         */
        long getRejectedRequests();

        /**
         * 获取通过率
         *
         * @return 通过率
         */
        double getPassRate();

        /**
         * 获取当前QPS
         *
         * @return 当前QPS
         */
        double getCurrentQps();
    }
}