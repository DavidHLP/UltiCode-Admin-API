package com.david.redis.commons.core.operations.interfaces;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis String类型操作接口
 *
 * <p>定义所有String类型的Redis操作方法，采用更安全、优雅的泛型设计。
 *
 * @author David
 */
public interface RedisStringOperations {

    /**
     * 设置键值对，并确保值可序列化。
     *
     * @param key 键
     * @param value 可序列化的值
     * @param <T> 值的泛型类型
     */
    <T> void set(String key, T value);

    /**
     * 设置键值对并指定过期时间，确保值可序列化。
     *
     * @param key 键
     * @param value 可序列化的值
     * @param timeout 过期时间
     * @param <T> 值的泛型类型
     */
    <T> void set(String key, T value, Duration timeout);

    /**
     * 获取指定键的值，并进行类型安全转换。
     *
     * @param key 键
     * @param clazz 返回值类型
     * @param <T> 泛型类型
     * @return 值，如果键不存在或类型不匹配则返回null
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * 获取原始字符串值，作为基础方法。
     *
     * @param key 键
     * @return 字符串值，如果键不存在则返回null
     */
    String get(String key);

    /**
     * 批量获取多个键的值，并进行类型安全转换。
     *
     * @param keys 键列表
     * @param clazz 返回值列表的类型
     * @param <T> 泛型类型
     * @return 值列表，与键列表顺序对应，如果键不存在则对应位置为null
     */
    <T> List<T> multiGet(List<String> keys, Class<T> clazz);

    /**
     * 批量设置多个键值对。
     *
     * @param keyValues 键值对映射，值必须可序列化
     * @param <T> 值的泛型类型
     */
    <T> void multiSet(Map<String, T> keyValues);

    /**
     * 删除一个或多个指定的键。
     *
     * @param keys 键数组
     * @return 成功删除的键数量
     */
    Long delete(String... keys);

    /**
     * 设置键的过期时间。
     *
     * @param key 键
     * @param timeout 过期时间
     * @return 是否设置成功
     */
    Boolean expire(String key, Duration timeout);

    /**
     * 检查键是否存在。
     *
     * @param key 键
     * @return 是否存在
     */
    Boolean hasKey(String key);

    /**
     * 获取键的剩余过期时间。
     *
     * @param key 键
     * @return 剩余过期时间（秒），-1表示永不过期，-2表示键不存在
     */
    Long getExpire(String key);

    /**
     * 使用 SCAN 按模式匹配获取键集合（推荐，避免 KEYS 的阻塞与在集群环境下的不稳定）。
     *
     * @param pattern 匹配模式，支持通配符 * 和 ?
     * @return 匹配的键集合
     */
    Set<String> scanKeys(String pattern);
}
