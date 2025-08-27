package com.david.redis.commons.core.operations.interfaces;

import java.util.List;
import java.util.Set;

/**
 * Redis Set类型操作接口
 * 
 * <p>定义所有Set类型的Redis操作方法
 * 
 * @author David
 */
public interface RedisSetOperations {

    /**
     * 向集合添加一个或多个元素
     *
     * @param key 集合键
     * @param values 要添加的值
     * @return 成功添加的元素数量（不包括已存在的元素）
     */
    Long sAdd(String key, Object... values);

    /**
     * 从集合中移除一个或多个元素
     *
     * @param key 集合键
     * @param values 要移除的值
     * @return 成功移除的元素数量
     */
    Long sRem(String key, Object... values);

    /**
     * 获取集合的所有成员
     *
     * @param key 集合键
     * @param clazz 元素类型
     * @param <T> 泛型类型
     * @return 集合的所有成员
     */
    <T> Set<T> sMembers(String key, Class<T> clazz);

    /**
     * 检查元素是否是集合的成员
     *
     * @param key 集合键
     * @param value 要检查的值
     * @return 如果元素是集合成员返回true，否则返回false
     */
    Boolean sIsMember(String key, Object value);

    /**
     * 获取集合的元素数量
     *
     * @param key 集合键
     * @return 集合的元素数量
     */
    Long sSize(String key);

    /**
     * 随机获取集合中的一个元素（不移除）
     *
     * @param key 集合键
     * @param clazz 元素类型
     * @param <T> 泛型类型
     * @return 随机元素，如果集合为空则返回null
     */
    <T> T sRandomMember(String key, Class<T> clazz);

    /**
     * 随机获取集合中的多个元素（不移除）
     *
     * @param key 集合键
     * @param count 要获取的元素数量
     * @param clazz 元素类型
     * @param <T> 泛型类型
     * @return 随机元素列表
     */
    <T> List<T> sRandomMembers(String key, long count, Class<T> clazz);

    /**
     * 随机移除并返回集合中的一个元素
     *
     * @param key 集合键
     * @param clazz 元素类型
     * @param <T> 泛型类型
     * @return 被移除的元素，如果集合为空则返回null
     */
    <T> T sPop(String key, Class<T> clazz);
}
