package com.david.commons.redis.operations;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis 集合操作接口
 *
 * @param <T> 元素类型
 * @author David
 */
public interface RedisSetOperations<T> {

    /**
     * 添加元素到集合
     *
     * @param key    键
     * @param values 值集合
     * @return 添加的元素数量
     */
    Long add(String key, T... values);

    /**
     * 移除集合中的元素
     *
     * @param key    键
     * @param values 值集合
     * @return 移除的元素数量
     */
    Long remove(String key, T... values);

    /**
     * 随机移除并返回一个元素
     *
     * @param key 键
     * @return 移除的元素
     */
    T pop(String key);

    /**
     * 随机移除并返回多个元素
     *
     * @param key   键
     * @param count 数量
     * @return 移除的元素列表
     */
    List<T> pop(String key, long count);

    /**
     * 将元素从源集合移动到目标集合
     *
     * @param key     源集合键
     * @param value   元素
     * @param destKey 目标集合键
     * @return 是否移动成功
     */
    Boolean move(String key, T value, String destKey);

    /**
     * 获取集合大小
     *
     * @param key 键
     * @return 集合大小
     */
    Long size(String key);

    /**
     * 判断元素是否在集合中
     *
     * @param key   键
     * @param value 值
     * @return 是否存在
     */
    Boolean isMember(String key, T value);

    /**
     * 批量判断元素是否在集合中
     *
     * @param key    键
     * @param values 值集合
     * @return 存在性映射
     */
    Map<Object, Boolean> isMember(String key, Object... values);

    /**
     * 获取集合的所有元素
     *
     * @param key 键
     * @return 元素集合
     */
    Set<T> members(String key);

    /**
     * 随机获取一个元素
     *
     * @param key 键
     * @return 随机元素
     */
    T randomMember(String key);

    /**
     * 随机获取多个元素
     *
     * @param key   键
     * @param count 数量
     * @return 随机元素列表
     */
    List<T> randomMembers(String key, long count);

    /**
     * 随机获取多个不重复元素
     *
     * @param key   键
     * @param count 数量
     * @return 随机元素集合
     */
    Set<T> distinctRandomMembers(String key, long count);

    /**
     * 获取多个集合的交集
     *
     * @param key       键
     * @param otherKeys 其他键
     * @return 交集
     */
    Set<T> intersect(String key, String... otherKeys);

    /**
     * 获取多个集合的交集
     *
     * @param key       键
     * @param otherKeys 其他键集合
     * @return 交集
     */
    Set<T> intersect(String key, Collection<String> otherKeys);

    /**
     * 获取多个集合的并集
     *
     * @param key       键
     * @param otherKeys 其他键
     * @return 并集
     */
    Set<T> union(String key, String... otherKeys);

    /**
     * 获取多个集合的并集
     *
     * @param key       键
     * @param otherKeys 其他键集合
     * @return 并集
     */
    Set<T> union(String key, Collection<String> otherKeys);

    /**
     * 获取多个集合的差集
     *
     * @param key       键
     * @param otherKeys 其他键
     * @return 差集
     */
    Set<T> difference(String key, String... otherKeys);

    /**
     * 获取多个集合的差集
     *
     * @param key       键
     * @param otherKeys 其他键集合
     * @return 差集
     */
    Set<T> difference(String key, Collection<String> otherKeys);
}