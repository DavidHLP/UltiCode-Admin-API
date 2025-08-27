package com.david.redis.commons.core.operations.interfaces;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis Hash类型操作接口
 * 
 * <p>定义所有Hash类型的Redis操作方法
 * 
 * @author David
 */
public interface RedisHashOperations {

    /**
     * 设置Hash字段值
     *
     * @param key Hash键
     * @param hashKey Hash字段
     * @param value 值
     */
    void hSet(String key, String hashKey, Object value);

    /**
     * 获取Hash字段值
     *
     * @param key Hash键
     * @param hashKey Hash字段
     * @param clazz 返回值类型
     * @param <T> 泛型类型
     * @return 字段值，如果字段不存在则返回null
     */
    <T> T hGet(String key, String hashKey, Class<T> clazz);

    /**
     * 获取Hash字段的字符串值（便捷方法）
     *
     * @param key Hash键
     * @param hashKey Hash字段
     * @return 字符串值
     */
    String hGetString(String key, String hashKey);

    /**
     * 获取Hash的所有字段和值
     *
     * @param key Hash键
     * @return 包含所有字段和值的Map，如果Hash不存在则返回空Map
     */
    Map<String, Object> hGetAll(String key);

    /**
     * 删除Hash中的一个或多个字段
     *
     * @param key Hash键
     * @param hashKeys 要删除的Hash字段
     * @return 成功删除的字段数量
     */
    Long hDelete(String key, String... hashKeys);

    /**
     * 检查Hash字段是否存在
     *
     * @param key Hash键
     * @param hashKey Hash字段
     * @return 是否存在
     */
    Boolean hExists(String key, String hashKey);

    /**
     * 获取Hash中字段的数量
     *
     * @param key Hash键
     * @return 字段数量
     */
    Long hSize(String key);

    /**
     * 获取Hash中所有的字段名
     *
     * @param key Hash键
     * @return 所有字段名的集合
     */
    Set<String> hKeys(String key);

    /**
     * 获取Hash中所有的值
     *
     * @param key Hash键
     * @return 所有值的列表
     */
    List<Object> hValues(String key);

    /**
     * 为Hash字段的数值增加指定的增量
     *
     * @param key Hash键
     * @param hashKey Hash字段
     * @param increment 增量值
     * @return 增加后的值
     */
    Long hIncrBy(String key, String hashKey, long increment);

    /**
     * 为Hash字段的浮点数值增加指定的增量
     *
     * @param key Hash键
     * @param hashKey Hash字段
     * @param increment 增量值
     * @return 增加后的值
     */
    Double hIncrByFloat(String key, String hashKey, double increment);

    /**
     * 批量设置Hash字段
     *
     * @param key Hash键
     * @param map 包含字段和值的Map
     */
    void hMSet(String key, Map<String, Object> map);

    /**
     * 批量获取Hash字段值
     *
     * @param key Hash键
     * @param hashKeys 要获取的Hash字段
     * @return 字段值列表，顺序与输入的字段顺序一致
     */
    List<Object> hMGet(String key, String... hashKeys);
}
