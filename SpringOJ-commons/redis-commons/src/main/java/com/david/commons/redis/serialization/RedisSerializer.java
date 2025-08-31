package com.david.commons.redis.serialization;

/**
 * Redis 序列化接口
 *
 * @param <T> 序列化对象类型
 * @author David
 */
public interface RedisSerializer<T> {

    /**
     * 序列化对象为字节数组
     *
     * @param object 待序列化对象
     * @return 序列化后的字节数组
     * @throws RedisSerializationException 序列化异常
     */
    byte[] serialize(T object) throws RedisSerializationException;

    /**
     * 反序列化字节数组为对象
     *
     * @param bytes 字节数组
     * @param type  目标类型
     * @param <R>   返回类型
     * @return 反序列化后的对象
     * @throws RedisSerializationException 反序列化异常
     */
    <R> R deserialize(byte[] bytes, Class<R> type) throws RedisSerializationException;

    /**
     * 获取序列化类型
     *
     * @return 序列化类型
     */
    SerializationType getType();

    /**
     * 是否支持指定类型的序列化
     *
     * @param type 类型
     * @return 是否支持
     */
    default boolean supports(Class<?> type) {
        return true;
    }
}