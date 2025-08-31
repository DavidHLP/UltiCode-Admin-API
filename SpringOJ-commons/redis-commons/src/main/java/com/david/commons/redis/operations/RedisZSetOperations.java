package com.david.commons.redis.operations;

import java.util.Collection;
import java.util.Set;

/**
 * Redis 有序集合操作接口
 *
 * @param <T> 元素类型
 * @author David
 */
public interface RedisZSetOperations<T> {

    /**
     * 添加元素到有序集合
     *
     * @param key   键
     * @param value 值
     * @param score 分数
     * @return 是否添加成功
     */
    Boolean add(String key, T value, double score);

    /**
     * 批量添加元素到有序集合
     *
     * @param key    键
     * @param tuples 元素分数对集合
     * @return 添加的元素数量
     */
    Long add(String key, Set<ZSetTuple<T>> tuples);

    /**
     * 移除有序集合中的元素
     *
     * @param key    键
     * @param values 值集合
     * @return 移除的元素数量
     */
    Long remove(String key, T... values);

    /**
     * 按分数范围移除元素
     *
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 移除的元素数量
     */
    Long removeRangeByScore(String key, double min, double max);

    /**
     * 按排名范围移除元素
     *
     * @param key   键
     * @param start 开始排名
     * @param end   结束排名
     * @return 移除的元素数量
     */
    Long removeRange(String key, long start, long end);

    /**
     * 增加元素的分数
     *
     * @param key   键
     * @param value 值
     * @param delta 增量
     * @return 增加后的分数
     */
    Double incrementScore(String key, T value, double delta);

    /**
     * 获取元素的分数
     *
     * @param key   键
     * @param value 值
     * @return 分数
     */
    Double score(String key, T value);

    /**
     * 获取元素的排名（从小到大）
     *
     * @param key   键
     * @param value 值
     * @return 排名
     */
    Long rank(String key, T value);

    /**
     * 获取元素的排名（从大到小）
     *
     * @param key   键
     * @param value 值
     * @return 排名
     */
    Long reverseRank(String key, T value);

    /**
     * 按排名范围获取元素（从小到大）
     *
     * @param key   键
     * @param start 开始排名
     * @param end   结束排名
     * @return 元素集合
     */
    Set<T> range(String key, long start, long end);

    /**
     * 按排名范围获取元素和分数（从小到大）
     *
     * @param key   键
     * @param start 开始排名
     * @param end   结束排名
     * @return 元素分数对集合
     */
    Set<ZSetTuple<T>> rangeWithScores(String key, long start, long end);

    /**
     * 按排名范围获取元素（从大到小）
     *
     * @param key   键
     * @param start 开始排名
     * @param end   结束排名
     * @return 元素集合
     */
    Set<T> reverseRange(String key, long start, long end);

    /**
     * 按排名范围获取元素和分数（从大到小）
     *
     * @param key   键
     * @param start 开始排名
     * @param end   结束排名
     * @return 元素分数对集合
     */
    Set<ZSetTuple<T>> reverseRangeWithScores(String key, long start, long end);

    /**
     * 按分数范围获取元素
     *
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 元素集合
     */
    Set<T> rangeByScore(String key, double min, double max);

    /**
     * 按分数范围获取元素和分数
     *
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 元素分数对集合
     */
    Set<ZSetTuple<T>> rangeByScoreWithScores(String key, double min, double max);

    /**
     * 按分数范围获取元素（限制数量）
     *
     * @param key    键
     * @param min    最小分数
     * @param max    最大分数
     * @param offset 偏移量
     * @param count  数量
     * @return 元素集合
     */
    Set<T> rangeByScore(String key, double min, double max, long offset, long count);

    /**
     * 按分数范围获取元素和分数（限制数量）
     *
     * @param key    键
     * @param min    最小分数
     * @param max    最大分数
     * @param offset 偏移量
     * @param count  数量
     * @return 元素分数对集合
     */
    Set<ZSetTuple<T>> rangeByScoreWithScores(String key, double min, double max, long offset, long count);

    /**
     * 统计分数范围内的元素数量
     *
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 元素数量
     */
    Long count(String key, double min, double max);

    /**
     * 获取有序集合大小
     *
     * @param key 键
     * @return 集合大小
     */
    Long size(String key);

    /**
     * 获取多个有序集合的交集
     *
     * @param key       键
     * @param otherKeys 其他键
     * @return 交集大小
     */
    Long intersectAndStore(String key, String destKey, String... otherKeys);

    /**
     * 获取多个有序集合的交集
     *
     * @param key       键
     * @param otherKeys 其他键集合
     * @return 交集大小
     */
    Long intersectAndStore(String key, String destKey, Collection<String> otherKeys);

    /**
     * 获取多个有序集合的并集
     *
     * @param key       键
     * @param otherKeys 其他键
     * @return 并集大小
     */
    Long unionAndStore(String key, String destKey, String... otherKeys);

    /**
     * 获取多个有序集合的并集
     *
     * @param key       键
     * @param otherKeys 其他键集合
     * @return 并集大小
     */
    Long unionAndStore(String key, String destKey, Collection<String> otherKeys);

    /**
     * 有序集合元素分数对
     *
     * @param <V> 值类型
     */
    interface ZSetTuple<V> {
        /**
         * 获取值
         *
         * @return 值
         */
        V getValue();

        /**
         * 获取分数
         *
         * @return 分数
         */
        Double getScore();
    }
}