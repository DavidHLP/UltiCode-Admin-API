package com.david.redis.commons.core.lock.interfaces;

import com.david.redis.commons.core.RedisUtils;

/**
 * Redis事务回调接口
 *
 * @param <T> 返回值类型
 */
@FunctionalInterface
public interface RedisCallback<T> {
    /**
     * 在事务中执行的操作
     *
     * @param operations Redis操作对象
     * @return 操作结果
     */
    T doInRedis(RedisUtils operations);
}