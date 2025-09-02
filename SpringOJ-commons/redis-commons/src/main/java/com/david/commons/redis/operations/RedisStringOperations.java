package com.david.commons.redis.operations;

import com.david.commons.redis.serialization.SerializationType;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Redis 字符串操作接口
 * @author David
 */
public interface RedisStringOperations {

    /**
     * 指定序列化类型，返回可链式调用的操作实例
     *
     * @param serializationType 序列化类型
     * @return 当前或新的操作实例（链式）
     */
    RedisStringOperations using(SerializationType serializationType);

    /**
     * 设置键值
     *
     * @param key 键
     * @param value 值
     * @return 操作结果
     */
    Boolean set(String key, Object value);

    /**
     * 设置键值并指定过期时间
     *
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 操作结果
     */
    Boolean set(String key, Object value, long timeout, TimeUnit unit);

    /**
     * 设置键值并指定过期时间
     *
     * @param key 键
     * @param value 值
     * @param duration 过期时间
     * @return 操作结果
     */
    Boolean set(String key, Object value, Duration duration);

    /**
     * 仅当键不存在时设置
     *
     * @param key 键
     * @param value 值
     * @return 是否设置成功
     */
    Boolean setIfAbsent(String key, Object value);

    /**
     * 仅当键不存在时设置并指定过期时间
     *
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 是否设置成功
     */
    Boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit);

    /**
     * 获取键对应的值
     *
     * @param key 键
     * @param valueType 值类型
     * @return 值
     */
    <T> T get(String key, Class<T> valueType);


    /**
     * 获取并设置新值
     *
     * @param key 键
     * @param value 新值
     * @param valueType 值类型
     * @return 旧值
     */
    <T> T getAndSet(String key, T value, Class<T> valueType);


    /**
     * 递增操作
     *
     * @param key 键
     * @return 递增后的值
     */
    Long increment(String key);

    /**
     * 按指定步长递增
     *
     * @param key 键
     * @param delta 步长
     * @return 递增后的值
     */
    Long increment(String key, long delta);

    /**
     * 递减操作
     *
     * @param key 键
     * @return 递减后的值
     */
    Long decrement(String key);

    /**
     * 按指定步长递减
     *
     * @param key 键
     * @param delta 步长
     * @return 递减后的值
     */
    Long decrement(String key, long delta);

    /**
     * 获取字符串长度
     *
     * @param key 键
     * @return 字符串长度
     */
    Long size(String key);

    /**
     * 删除类型
     *
     * @param keys 键
     * @return 删除个数
     */
    Long delete(String... keys);
}

