package com.david.commons.redis.operations;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis 列表操作接口
 *
 * @param <T> 元素类型
 * @author David
 */
public interface RedisListOperations<T> {

    /**
     * 从左侧推入元素
     *
     * @param key   键
     * @param value 值
     * @return 列表长度
     */
    Long leftPush(String key, T value);

    /**
     * 从左侧批量推入元素
     *
     * @param key    键
     * @param values 值集合
     * @return 列表长度
     */
    Long leftPushAll(String key, T... values);

    /**
     * 从左侧批量推入元素
     *
     * @param key    键
     * @param values 值集合
     * @return 列表长度
     */
    Long leftPushAll(String key, Collection<T> values);

    /**
     * 仅当列表存在时从左侧推入
     *
     * @param key   键
     * @param value 值
     * @return 列表长度
     */
    Long leftPushIfPresent(String key, T value);

    /**
     * 从右侧推入元素
     *
     * @param key   键
     * @param value 值
     * @return 列表长度
     */
    Long rightPush(String key, T value);

    /**
     * 从右侧批量推入元素
     *
     * @param key    键
     * @param values 值集合
     * @return 列表长度
     */
    Long rightPushAll(String key, T... values);

    /**
     * 从右侧批量推入元素
     *
     * @param key    键
     * @param values 值集合
     * @return 列表长度
     */
    Long rightPushAll(String key, Collection<T> values);

    /**
     * 仅当列表存在时从右侧推入
     *
     * @param key   键
     * @param value 值
     * @return 列表长度
     */
    Long rightPushIfPresent(String key, T value);

    /**
     * 从左侧弹出元素
     *
     * @param key 键
     * @return 弹出的元素
     */
    T leftPop(String key);

    /**
     * 从左侧阻塞弹出元素
     *
     * @param key     键
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return 弹出的元素
     */
    T leftPop(String key, long timeout, TimeUnit unit);

    /**
     * 从右侧弹出元素
     *
     * @param key 键
     * @return 弹出的元素
     */
    T rightPop(String key);

    /**
     * 从右侧阻塞弹出元素
     *
     * @param key     键
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return 弹出的元素
     */
    T rightPop(String key, long timeout, TimeUnit unit);

    /**
     * 获取指定范围的元素
     *
     * @param key   键
     * @param start 开始索引
     * @param end   结束索引
     * @return 元素列表
     */
    List<T> range(String key, long start, long end);

    /**
     * 获取指定索引的元素
     *
     * @param key   键
     * @param index 索引
     * @return 元素
     */
    T index(String key, long index);

    /**
     * 设置指定索引的元素
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     */
    void set(String key, long index, T value);

    /**
     * 移除指定值的元素
     *
     * @param key   键
     * @param count 移除数量
     * @param value 值
     * @return 实际移除数量
     */
    Long remove(String key, long count, T value);

    /**
     * 修剪列表
     *
     * @param key   键
     * @param start 开始索引
     * @param end   结束索引
     */
    void trim(String key, long start, long end);

    /**
     * 获取列表长度
     *
     * @param key 键
     * @return 列表长度
     */
    Long size(String key);
}