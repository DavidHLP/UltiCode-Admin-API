package com.david.commons.redis.cache.protection.interfaces;

import java.util.function.Supplier;

/**
 * 缓存穿透防护接口
 *
 * <p>
 * 提供布隆过滤器防穿透功能，防止大量请求查询不存在的数据直接访问数据库。
 *
 * @author David
 */
public interface CachePenetrationProtection {

    /**
     * 检查键是否可能存在
     *
     * @param filterName 过滤器名称
     * @param key        键
     * @return 是否可能存在
     */
    boolean mightExist(String filterName, Object key);

    /**
     * 添加键到布隆过滤器
     *
     * @param filterName 过滤器名称
     * @param key        键
     * @return 是否添加成功
     */
    boolean addKey(String filterName, Object key);

    /**
     * 执行带防穿透保护的操作
     *
     * @param filterName 过滤器名称
     * @param key        键
     * @param dataLoader 数据加载器
     * @param <T>        数据类型
     * @return 数据或null
     */
    <T> T executeWithProtection(String filterName, Object key, Supplier<T> dataLoader);

    /**
     * 执行带防穿透保护的操作（支持空值缓存）
     *
     * @param filterName       过滤器名称
     * @param key              键
     * @param dataLoader       数据加载器
     * @param nullValueHandler 空值处理器
     * @param <T>              数据类型
     * @return 数据或null
     */
    <T> T executeWithProtection(String filterName, Object key, Supplier<T> dataLoader, Runnable nullValueHandler);

    /**
     * 批量添加键到布隆过滤器
     *
     * @param filterName 过滤器名称
     * @param keys       键集合
     * @return 成功添加的数量
     */
    long addKeys(String filterName, Iterable<?> keys);

    /**
     * 预热布隆过滤器
     *
     * @param filterName  过滤器名称
     * @param keyProvider 键提供器
     * @return 预热的键数量
     */
    long warmUp(String filterName, Supplier<Iterable<?>> keyProvider);

    /**
     * 获取防穿透统计信息
     *
     * @param filterName 过滤器名称
     * @return 统计信息
     */
    PenetrationProtectionStats getStats(String filterName);

    /**
     * 防穿透统计信息
     */
    interface PenetrationProtectionStats {

        /**
         * 获取过滤器名称
         *
         * @return 过滤器名称
         */
        String getFilterName();

        /**
         * 获取总查询次数
         *
         * @return 总查询次数
         */
        long getTotalQueries();

        /**
         * 获取被过滤的查询次数
         *
         * @return 被过滤的查询次数
         */
        long getFilteredQueries();

        /**
         * 获取通过的查询次数
         *
         * @return 通过的查询次数
         */
        long getPassedQueries();

        /**
         * 获取过滤率
         *
         * @return 过滤率
         */
        double getFilterRate();

        /**
         * 获取数据库查询次数
         *
         * @return 数据库查询次数
         */
        long getDatabaseQueries();

        /**
         * 获取缓存命中次数
         *
         * @return 缓存命中次数
         */
        long getCacheHits();

        /**
         * 获取空值查询次数
         *
         * @return 空值查询次数
         */
        long getNullQueries();
    }
}