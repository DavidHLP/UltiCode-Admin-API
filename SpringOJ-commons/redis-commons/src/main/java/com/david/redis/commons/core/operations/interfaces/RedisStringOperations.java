package com.david.redis.commons.core.operations.interfaces;

import java.time.Duration;
import java.util.Set;

/**
 * Redis String类型操作接口
 * 
 * <p>定义所有String类型的Redis操作方法
 * 
 * @author David
 */
public interface RedisStringOperations {

    /**
     * 设置键值对
     *
     * @param key 键
     * @param value 值
     */
    void set(String key, Object value);

    /**
     * 设置键值对并指定过期时间
     *
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     */
    void set(String key, Object value, Duration timeout);

    /**
     * 获取指定键的值
     *
     * @param key 键
     * @param clazz 返回值类型
     * @param <T> 泛型类型
     * @return 值，如果键不存在则返回null
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * 获取字符串值（便捷方法）
     *
     * @param key 键
     * @return 字符串值
     */
    String getString(String key);

    /**
     * 删除指定键
     *
     * @param key 键
     * @return 是否删除成功
     */
    Boolean delete(String key);

    /**
     * 批量删除指定键
     *
     * @param keys 键数组
     * @return 成功删除的键数量
     */
    Long delete(String... keys);

    /**
     * 设置键的过期时间
     *
     * @param key 键
     * @param timeout 过期时间
     * @return 是否设置成功
     */
    Boolean expire(String key, Duration timeout);

    /**
     * 检查键是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    Boolean hasKey(String key);

    /**
     * 获取键的剩余过期时间
     *
     * @param key 键
     * @return 剩余过期时间（秒），-1表示永不过期，-2表示键不存在
     */
    Long getExpire(String key);

    /**
     * 根据模式匹配获取键集合
     *
     * @param pattern 匹配模式，支持通配符 * 和 ?
     * @return 匹配的键集合
     */
    Set<String> keys(String pattern);

    /**
     * 使用 SCAN 按模式匹配获取键集合（推荐，避免 KEYS 的阻塞与在集群环境下的不稳定）
     *
     * @param pattern 匹配模式，支持通配符 * 和 ?
     * @return 匹配的键集合
     */
    Set<String> scanKeys(String pattern);

    /**
     * 批量获取多个键的值
     *
     * @param keys 键列表
     * @return 值列表，与键列表顺序对应
     */
    java.util.List<Object> multiGet(java.util.List<String> keys);

    /**
     * 批量设置多个键值对
     *
     * @param keyValues 键值对映射
     */
    void multiSet(java.util.Map<String, Object> keyValues);

    /**
     * 设置键的过期时间（支持TimeUnit）
     *
     * @param key 键
     * @param timeout 过期时间数值
     * @param timeUnit 时间单位
     * @return 是否设置成功
     */
    Boolean expire(String key, long timeout, java.util.concurrent.TimeUnit timeUnit);
}
