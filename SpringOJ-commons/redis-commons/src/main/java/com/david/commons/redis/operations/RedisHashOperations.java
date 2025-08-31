package com.david.commons.redis.operations;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis 哈希操作接口
 *
 * @param <T> 值类型
 * @author David
 */
public interface RedisHashOperations<T> {

    /**
     * 设置哈希字段值
     *
     * @param key     键
     * @param hashKey 哈希字段
     * @param value   值
     */
    void put(String key, String hashKey, T value);

    /**
     * 批量设置哈希字段值
     *
     * @param key 键
     * @param map 字段值映射
     */
    void putAll(String key, Map<String, T> map);

    /**
     * 仅当字段不存在时设置
     *
     * @param key     键
     * @param hashKey 哈希字段
     * @param value   值
     * @return 是否设置成功
     */
    Boolean putIfAbsent(String key, String hashKey, T value);

    /**
     * 获取哈希字段值
     *
     * @param key     键
     * @param hashKey 哈希字段
     * @return 字段值
     */
    T get(String key, String hashKey);

    /**
     * 批量获取哈希字段值
     *
     * @param key      键
     * @param hashKeys 哈希字段集合
     * @return 字段值列表
     */
    List<T> multiGet(String key, Collection<String> hashKeys);

    /**
     * 获取所有字段值映射
     *
     * @param key 键
     * @return 字段值映射
     */
    Map<String, T> entries(String key);

    /**
     * 获取所有字段
     *
     * @param key 键
     * @return 字段集合
     */
    Set<String> keys(String key);

    /**
     * 获取所有值
     *
     * @param key 键
     * @return 值集合
     */
    List<T> values(String key);

    /**
     * 删除哈希字段
     *
     * @param key      键
     * @param hashKeys 哈希字段
     * @return 删除的字段数量
     */
    Long delete(String key, String... hashKeys);

    /**
     * 判断字段是否存在
     *
     * @param key     键
     * @param hashKey 哈希字段
     * @return 是否存在
     */
    Boolean hasKey(String key, String hashKey);

    /**
     * 获取哈希大小
     *
     * @param key 键
     * @return 哈希大小
     */
    Long size(String key);

    /**
     * 字段值递增
     *
     * @param key     键
     * @param hashKey 哈希字段
     * @param delta   增量
     * @return 递增后的值
     */
    Long increment(String key, String hashKey, long delta);

    /**
     * 字段值递增（浮点数）
     *
     * @param key     键
     * @param hashKey 哈希字段
     * @param delta   增量
     * @return 递增后的值
     */
    Double increment(String key, String hashKey, double delta);
}