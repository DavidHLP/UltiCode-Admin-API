package com.david.redis.commons.core.operations;

import org.springframework.data.redis.core.ZSetOperations;

import java.util.Map;
import java.util.Set;

/**
 * Redis ZSet类型操作接口
 * 
 * <p>定义所有ZSet类型的Redis操作方法
 * 
 * @author David
 */
public interface RedisZSetOperations {

    /**
     * 向有序集合添加一个成员，或者更新已存在成员的分数
     *
     * @param key 有序集合键
     * @param value 成员值
     * @param score 分数
     * @return 如果成员是新成员返回true，如果是更新已存在成员的分数返回false
     */
    Boolean zAdd(String key, Object value, double score);

    /**
     * 批量向有序集合添加成员
     *
     * @param key 有序集合键
     * @param scoreValueMap 分数和值的映射
     * @return 成功添加的新成员数量
     */
    Long zAdd(String key, Map<Object, Double> scoreValueMap);

    /**
     * 从有序集合中移除一个或多个成员
     *
     * @param key 有序集合键
     * @param values 要移除的成员值
     * @return 成功移除的成员数量
     */
    Long zRem(String key, Object... values);

    /**
     * 获取有序集合指定排名范围内的成员（按分数从低到高排序）
     *
     * @param key 有序集合键
     * @param start 开始排名（包含）
     * @param end 结束排名（包含），-1表示到集合末尾
     * @param clazz 成员类型
     * @param <T> 泛型类型
     * @return 指定排名范围内的成员集合
     */
    <T> Set<T> zRange(String key, long start, long end, Class<T> clazz);

    /**
     * 获取有序集合指定排名范围内的成员及其分数（按分数从低到高排序）
     *
     * @param key 有序集合键
     * @param start 开始排名（包含）
     * @param end 结束排名（包含），-1表示到集合末尾
     * @param clazz 成员类型
     * @param <T> 泛型类型
     * @return 包含成员和分数的TypedTuple集合
     */
    <T> Set<ZSetOperations.TypedTuple<T>> zRangeWithScores(String key, long start, long end, Class<T> clazz);

    /**
     * 获取有序集合指定排名范围内的成员（按分数从高到低排序）
     *
     * @param key 有序集合键
     * @param start 开始排名（包含）
     * @param end 结束排名（包含），-1表示到集合末尾
     * @param clazz 成员类型
     * @param <T> 泛型类型
     * @return 指定排名范围内的成员集合（按分数从高到低）
     */
    <T> Set<T> zRevRange(String key, long start, long end, Class<T> clazz);

    /**
     * 获取有序集合中指定成员的分数
     *
     * @param key 有序集合键
     * @param value 成员值
     * @return 成员的分数，如果成员不存在则返回null
     */
    Double zScore(String key, Object value);

    /**
     * 获取有序集合中指定成员的排名（按分数从低到高）
     *
     * @param key 有序集合键
     * @param value 成员值
     * @return 成员的排名（从0开始），如果成员不存在则返回null
     */
    Long zRank(String key, Object value);

    /**
     * 获取有序集合中指定成员的逆序排名（按分数从高到低）
     *
     * @param key 有序集合键
     * @param value 成员值
     * @return 成员的逆序排名（从0开始），如果成员不存在则返回null
     */
    Long zRevRank(String key, Object value);

    /**
     * 获取有序集合的成员数量
     *
     * @param key 有序集合键
     * @return 成员数量
     */
    Long zSize(String key);

    /**
     * 获取有序集合中指定分数范围内的成员数量
     *
     * @param key 有序集合键
     * @param min 最小分数（包含）
     * @param max 最大分数（包含）
     * @return 指定分数范围内的成员数量
     */
    Long zCount(String key, double min, double max);

    /**
     * 为有序集合中指定成员的分数增加增量
     *
     * @param key 有序集合键
     * @param value 成员值
     * @param increment 分数增量
     * @return 增加后的分数
     */
    Double zIncrBy(String key, Object value, double increment);

    /**
     * 获取有序集合中指定分数范围内的成员
     *
     * @param key 有序集合键
     * @param min 最小分数（包含）
     * @param max 最大分数（包含）
     * @param clazz 成员类型
     * @param <T> 泛型类型
     * @return 指定分数范围内的成员集合
     */
    <T> Set<T> zRangeByScore(String key, double min, double max, Class<T> clazz);

    /**
     * 移除有序集合中指定分数范围内的成员
     *
     * @param key 有序集合键
     * @param min 最小分数（包含）
     * @param max 最大分数（包含）
     * @return 移除的成员数量
     */
    Long zRemRangeByScore(String key, double min, double max);

    /**
     * 移除有序集合中指定排名范围内的成员
     *
     * @param key 有序集合键
     * @param start 开始排名（包含）
     * @param end 结束排名（包含）
     * @return 移除的成员数量
     */
    Long zRemRangeByRank(String key, long start, long end);
}
