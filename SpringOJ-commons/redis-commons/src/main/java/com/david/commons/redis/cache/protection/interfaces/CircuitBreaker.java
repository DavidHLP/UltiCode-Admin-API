package com.david.commons.redis.cache.protection.interfaces;

import java.util.function.Supplier;

/**
 * 熔断器接口
 *
 * <p>
 * 提供熔断器模式实现，防止级联故障。
 *
 * @author David
 */
public interface CircuitBreaker {

    /**
     * 执行带熔断保护的操作
     *
     * @param key       熔断器键
     * @param operation 操作
     * @param <T>       返回类型
     * @return 操作结果
     */
    <T> T execute(String key, Supplier<T> operation);

    /**
     * 执行带熔断保护的操作（带降级）
     *
     * @param key       熔断器键
     * @param operation 操作
     * @param fallback  降级操作
     * @param <T>       返回类型
     * @return 操作结果
     */
    <T> T execute(String key, Supplier<T> operation, Supplier<T> fallback);

    /**
     * 记录成功
     *
     * @param key 熔断器键
     */
    void recordSuccess(String key);

    /**
     * 记录失败
     *
     * @param key 熔断器键
     */
    void recordFailure(String key);

    /**
     * 获取熔断器状态
     *
     * @param key 熔断器键
     * @return 熔断器状态
     */
    CircuitBreakerState getState(String key);

    /**
     * 强制打开熔断器
     *
     * @param key 熔断器键
     */
    void forceOpen(String key);

    /**
     * 强制关闭熔断器
     *
     * @param key 熔断器键
     */
    void forceClose(String key);

    /**
     * 获取熔断器统计信息
     *
     * @param key 熔断器键
     * @return 统计信息
     */
    CircuitBreakerStats getStats(String key);

    /**
     * 熔断器状态枚举
     */
    enum CircuitBreakerState {
        /** 关闭状态：正常处理请求 */
        CLOSED,
        /** 打开状态：拒绝所有请求 */
        OPEN,
        /** 半开状态：允许少量请求测试服务是否恢复 */
        HALF_OPEN
    }

    /**
     * 熔断器统计信息
     */
    interface CircuitBreakerStats {

        /**
         * 获取熔断器键
         *
         * @return 熔断器键
         */
        String getKey();

        /**
         * 获取当前状态
         *
         * @return 当前状态
         */
        CircuitBreakerState getState();

        /**
         * 获取总请求数
         *
         * @return 总请求数
         */
        long getTotalRequests();

        /**
         * 获取成功请求数
         *
         * @return 成功请求数
         */
        long getSuccessfulRequests();

        /**
         * 获取失败请求数
         *
         * @return 失败请求数
         */
        long getFailedRequests();

        /**
         * 获取被熔断的请求数
         *
         * @return 被熔断的请求数
         */
        long getCircuitOpenRequests();

        /**
         * 获取失败率
         *
         * @return 失败率
         */
        double getFailureRate();

        /**
         * 获取上次状态变更时间
         *
         * @return 上次状态变更时间
         */
        long getLastStateChangeTime();

        /**
         * 获取下次允许请求时间（仅在OPEN状态有效）
         *
         * @return 下次允许请求时间
         */
        long getNextAllowedTime();
    }
}