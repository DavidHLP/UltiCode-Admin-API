package com.david.commons.redis.cache.protection.interfaces;

/**
 * 布隆过滤器管理器接口
 *
 * <p>
 * 提供布隆过滤器的创建、管理和操作功能，用于防止缓存穿透。
 *
 * @author David
 */
public interface BloomFilterManager {

    /**
     * 获取或创建布隆过滤器
     *
     * @param filterName 过滤器名称
     * @return 布隆过滤器实例
     */
    <T> BloomFilter<T> getOrCreateBloomFilter(String filterName);

    /**
     * 获取或创建布隆过滤器
     *
     * @param filterName         过滤器名称
     * @param expectedInsertions 预期插入数量
     * @param fpp                误判率
     * @return 布隆过滤器实例
     */
    <T> BloomFilter<T> getOrCreateBloomFilter(String filterName, long expectedInsertions, double fpp);

    /**
     * 删除布隆过滤器
     *
     * @param filterName 过滤器名称
     * @return 是否删除成功
     */
    boolean deleteBloomFilter(String filterName);

    /**
     * 检查布隆过滤器是否存在
     *
     * @param filterName 过滤器名称
     * @return 是否存在
     */
    boolean exists(String filterName);

    /**
     * 获取布隆过滤器统计信息
     *
     * @param filterName 过滤器名称
     * @return 统计信息
     */
    BloomFilterStats getStats(String filterName);

    /**
     * 布隆过滤器接口
     */
    interface BloomFilter<T> {

        /**
         * 添加元素
         *
         * @param element 元素
         * @return 是否添加成功
         */
        boolean add(T element);

        /**
         * 检查元素是否可能存在
         *
         * @param element 元素
         * @return 是否可能存在
         */
        boolean mightContain(T element);

        /**
         * 获取过滤器名称
         *
         * @return 过滤器名称
         */
        String getName();

        /**
         * 获取预期插入数量
         *
         * @return 预期插入数量
         */
        long getExpectedInsertions();

        /**
         * 获取误判率
         *
         * @return 误判率
         */
        double getFalsePositiveProbability();

        /**
         * 获取当前大小
         *
         * @return 当前大小
         */
        long size();

        /**
         * 清空过滤器
         */
        void clear();
    }

    /**
     * 布隆过滤器统计信息
     */
    interface BloomFilterStats {

        /**
         * 获取过滤器名称
         *
         * @return 过滤器名称
         */
        String getName();

        /**
         * 获取当前大小
         *
         * @return 当前大小
         */
        long getSize();

        /**
         * 获取预期插入数量
         *
         * @return 预期插入数量
         */
        long getExpectedInsertions();

        /**
         * 获取误判率
         *
         * @return 误判率
         */
        double getFalsePositiveProbability();

        /**
         * 获取查询次数
         *
         * @return 查询次数
         */
        long getQueryCount();

        /**
         * 获取命中次数
         *
         * @return 命中次数
         */
        long getHitCount();

        /**
         * 获取命中率
         *
         * @return 命中率
         */
        double getHitRate();
    }
}