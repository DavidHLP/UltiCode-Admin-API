package com.david.commons.redis.operations;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Redis 字符串操作接口
 *
 * @param <T> 值类型
 * @author David
 */
public interface RedisStringOperations<T> {

    /**
     * 设置键值
     *
     * @param key 键
     * @param value 值
     * @return 操作结果
     */
    Boolean set(String key, T value);

    /**
     * 设置键值并指定过期时间
     *
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 操作结果
     */
    Boolean set(String key, T value, long timeout, TimeUnit unit);

    /**
     * 设置键值并指定过期时间
     *
     * @param key 键
     * @param value 值
     * @param duration 过期时间
     * @return 操作结果
     */
    Boolean set(String key, T value, Duration duration);

    /**
     * 仅当键不存在时设置
     *
     * @param key 键
     * @param value 值
     * @return 是否设置成功
     */
    Boolean setIfAbsent(String key, T value);

    /**
     * 仅当键不存在时设置并指定过期时间
     *
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 是否设置成功
     */
    Boolean setIfAbsent(String key, T value, long timeout, TimeUnit unit);

    /**
     * 获取键对应的值
     *
     * @param key 键
     * @return 值
     */
    T get(String key);

    /**
     * 获取并设置新值
     *
     * @param key 键
     * @param value 新值
     * @return 旧值
     */
    T getAndSet(String key, T value);

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
}
