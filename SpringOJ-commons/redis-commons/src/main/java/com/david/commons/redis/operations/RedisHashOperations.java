package com.david.commons.redis.operations;

import com.david.commons.redis.serialization.enums.SerializationType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis Hash 操作接口
 *
 * <p>提供链式调用和方法级泛型的强类型读取能力
 *
 * @author David
 */
public interface RedisHashOperations {

    /**
     * 指定序列化类型，返回可链式调用的操作实例
     *
     * @param serializationType 序列化类型
     * @return 当前或新的操作实例（链式）
     */
    RedisHashOperations using(SerializationType serializationType);

    /**
     * 设置哈希字段值（HSET）
     *
     * @param key 键
     * @param field 字段
     * @param value 值
     * @return 是否成功
     */
    Boolean hSet(String key, String field, Object value);

    /**
     * 当字段不存在时设置值（HSETNX）
     *
     * @param key 键
     * @param field 字段
     * @param value 值
     * @return 是否设置成功
     */
    Boolean hSetIfAbsent(String key, String field, Object value);

    /**
     * 设置多个字段（HMSET）
     *
     * @param key 键
     * @param map 字段-值映射
     * @return 是否成功
     */
    Boolean hMSet(String key, Map<String, ?> map);

    /**
     * 获取字段值（HGET）
     *
     * @param key 键
     * @param field 字段
     * @param valueType 值类型
     * @return 值
     */
    <T> T hGet(String key, String field, Class<T> valueType);

    /**
     * 批量获取字段值（HMGET）
     *
     * @param key 键
     * @param fields 字段集合
     * @param valueType 值类型
     * @return 值列表，顺序与输入字段一致，缺失字段返回 null
     */
    <T> List<T> hMGet(String key, Collection<String> fields, Class<T> valueType);

    /**
     * 删除一个或多个字段（HDEL）
     *
     * @param key 键
     * @param fields 字段
     * @return 删除的字段数量
     */
    Long hDel(String key, String... fields);

    /**
     * 判断字段是否存在（HEXISTS）
     *
     * @param key 键
     * @param field 字段
     * @return 是否存在
     */
    Boolean hExists(String key, String field);

    /**
     * 获取所有字段值（HVALS）
     *
     * @param key 键
     * @param valueType 值类型
     * @return 值列表
     */
    <T> List<T> hVals(String key, Class<T> valueType);

    /**
     * 获取所有字段（HKEYS）
     *
     * @param key 键
     * @return 字段集合
     */
    Set<String> hKeys(String key);

    /**
     * 获取所有字段与值（HGETALL）
     *
     * @param key 键
     * @param valueType 值类型
     * @return 字段-值映射
     */
    <T> Map<String, T> hGetAll(String key, Class<T> valueType);

    /**
     * 字段值自增（HINCRBY）
     *
     * @param key 键
     * @param field 字段
     * @param delta 增量
     * @return 增加后的值
     */
    Long hIncrBy(String key, String field, long delta);

    /**
     * 字段值自增（浮点）（HINCRBYFLOAT）
     *
     * @param key 键
     * @param field 字段
     * @param delta 增量（浮点）
     * @return 增加后的值
     */
    Double hIncrByFloat(String key, String field, double delta);

    /**
     * 获取字段数量（HLEN）
     *
     * @param key 键
     * @return 字段数量
     */
    Long hLen(String key);
}
