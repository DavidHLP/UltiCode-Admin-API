package com.david.redis.commons.core.operations;

import java.util.List;

/**
 * Redis List类型操作接口
 * 
 * <p>定义所有List类型的Redis操作方法
 * 
 * @author David
 */
public interface RedisListOperations {

    /**
     * 从列表左侧推入一个或多个元素
     *
     * @param key 列表键
     * @param values 要推入的值
     * @return 推入后列表的长度
     */
    Long lPush(String key, Object... values);

    /**
     * 从列表右侧推入一个或多个元素
     *
     * @param key 列表键
     * @param values 要推入的值
     * @return 推入后列表的长度
     */
    Long rPush(String key, Object... values);

    /**
     * 从列表左侧弹出一个元素
     *
     * @param key 列表键
     * @param clazz 返回值类型
     * @param <T> 泛型类型
     * @return 弹出的元素，如果列表为空则返回null
     */
    <T> T lPop(String key, Class<T> clazz);

    /**
     * 从列表右侧弹出一个元素
     *
     * @param key 列表键
     * @param clazz 返回值类型
     * @param <T> 泛型类型
     * @return 弹出的元素，如果列表为空则返回null
     */
    <T> T rPop(String key, Class<T> clazz);

    /**
     * 获取列表指定范围内的元素
     *
     * @param key 列表键
     * @param start 开始索引（包含）
     * @param end 结束索引（包含），-1表示到列表末尾
     * @param clazz 元素类型
     * @param <T> 泛型类型
     * @return 指定范围内的元素列表
     */
    <T> List<T> lRange(String key, long start, long end, Class<T> clazz);

    /**
     * 获取列表长度
     *
     * @param key 列表键
     * @return 列表长度
     */
    Long lSize(String key);

    /**
     * 获取列表指定索引的元素
     *
     * @param key 列表键
     * @param index 索引
     * @param clazz 元素类型
     * @param <T> 泛型类型
     * @return 指定索引的元素，如果索引超出范围则返回null
     */
    <T> T lIndex(String key, long index, Class<T> clazz);

    /**
     * 设置列表指定索引的元素值
     *
     * @param key 列表键
     * @param index 索引
     * @param value 新值
     */
    void lSet(String key, long index, Object value);
}
