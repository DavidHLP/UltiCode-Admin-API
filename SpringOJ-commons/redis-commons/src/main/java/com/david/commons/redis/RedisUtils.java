package com.david.commons.redis;

import com.david.commons.redis.operations.*;
import com.david.commons.redis.lock.DistributedLockManager;
import com.david.commons.redis.serialization.SerializationType;

/**
 * Redis 工具类门面接口
 *
 * @author David
 */
public interface RedisUtils {

    /**
     * 获取字符串操作接口
     *
     * @param <T> 值类型
     * @return 字符串操作接口
     */
    <T> RedisStringOperations<T> string();

    /**
     * 获取字符串操作接口（指定序列化类型）
     *
     * @param serializationType 序列化类型
     * @param <T>               值类型
     * @return 字符串操作接口
     */
    <T> RedisStringOperations<T> string(SerializationType serializationType);

    /**
     * 获取哈希操作接口
     *
     * @param <T> 值类型
     * @return 哈希操作接口
     */
    <T> RedisHashOperations<T> hash();

    /**
     * 获取哈希操作接口（指定序列化类型）
     *
     * @param serializationType 序列化类型
     * @param <T>               值类型
     * @return 哈希操作接口
     */
    <T> RedisHashOperations<T> hash(SerializationType serializationType);

    /**
     * 获取列表操作接口
     *
     * @param <T> 元素类型
     * @return 列表操作接口
     */
    <T> RedisListOperations<T> list();

    /**
     * 获取列表操作接口（指定序列化类型）
     *
     * @param serializationType 序列化类型
     * @param <T>               元素类型
     * @return 列表操作接口
     */
    <T> RedisListOperations<T> list(SerializationType serializationType);

    /**
     * 获取集合操作接口
     *
     * @param <T> 元素类型
     * @return 集合操作接口
     */
    <T> RedisSetOperations<T> set();

    /**
     * 获取集合操作接口（指定序列化类型）
     *
     * @param serializationType 序列化类型
     * @param <T>               元素类型
     * @return 集合操作接口
     */
    <T> RedisSetOperations<T> set(SerializationType serializationType);

    /**
     * 获取有序集合操作接口
     *
     * @param <T> 元素类型
     * @return 有序集合操作接口
     */
    <T> RedisZSetOperations<T> zset();

    /**
     * 获取有序集合操作接口（指定序列化类型）
     *
     * @param serializationType 序列化类型
     * @param <T>               元素类型
     * @return 有序集合操作接口
     */
    <T> RedisZSetOperations<T> zset(SerializationType serializationType);

    /**
     * 获取通用操作接口
     *
     * @return 通用操作接口
     */
    RedisCommonOperations common();

    /**
     * 获取分布式锁管理器
     *
     * @return 分布式锁管理器
     */
    DistributedLockManager lock();

    /**
     * 设置全局键前缀
     *
     * @param prefix 键前缀
     */
    void setKeyPrefix(String prefix);

    /**
     * 获取全局键前缀
     *
     * @return 键前缀
     */
    String getKeyPrefix();

    /**
     * 构建完整的键名（添加前缀）
     *
     * @param key 原始键名
     * @return 完整键名
     */
    String buildKey(String key);

    /**
     * 设置默认序列化类型
     *
     * @param serializationType 序列化类型
     */
    void setDefaultSerializationType(SerializationType serializationType);

    /**
     * 获取默认序列化类型
     *
     * @return 序列化类型
     */
    SerializationType getDefaultSerializationType();
}