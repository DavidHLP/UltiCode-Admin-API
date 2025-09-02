package com.david.commons.redis.serialization;

import com.david.commons.redis.serialization.impl.JsonRedisSerializer;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

/**
 * Redis 序列化器工厂 - 简化为仅支持 Jackson JSON 序列化
 * 负责创建和管理 JSON 序列化器实例
 *
 * @author David
 */
@Component
@RequiredArgsConstructor
public class RedisSerializerFactory {

    /**
     * JSON 序列化器实现
     */
    private final JsonRedisSerializer jsonSerializer;
    /**
     * 获取指定类型的序列化器 - 仅支持 JSON 类型
     *
     * @param type 序列化类型
     * @param <T>  序列化对象类型
     * @return 序列化器实例
     * @throws RedisSerializationException 如果不支持指定的序列化类型
     */
    public RedisSerializer<Object> getSerializer(SerializationType type) {
        if (type != SerializationType.JSON) {
            throw new RedisSerializationException("SERIALIZER_NOT_FOUND",
                    "Only JSON serialization is supported, but requested: " + type);
        }
        return jsonSerializer;
    }

    /**
     * 获取默认序列化器（JSON）
     *
     * @param <T> 序列化对象类型
     * @return 默认序列化器
     */
    public RedisSerializer<Object> getDefaultSerializer() {
        return jsonSerializer;
    }

    /**
     * 根据对象类型获取序列化器 - 统一返回 JSON 序列化器
     *
     * @param objectType 对象类型
     * @return JSON 序列化器
     */
    public RedisSerializer<Object> getOptimalSerializer(Class<?> objectType) {
        // 统一使用 JSON 序列化器，通过 Class 进行反序列化
        return jsonSerializer;
    }

    /**
     * 检查指定类型是否支持序列化 - 仅支持 JSON 类型
     *
     * @param type       序列化类型
     * @param objectType 对象类型
     * @return 是否支持
     */
    public boolean supports(SerializationType type, Class<?> objectType) {
        if (type != SerializationType.JSON) {
            return false;
        }
        return jsonSerializer.supports(objectType);
    }

    /**
     * 获取所有支持的序列化类型 - 仅返回 JSON
     *
     * @return 支持的序列化类型数组
     */
    public SerializationType[] getSupportedTypes() {
        return new SerializationType[]{SerializationType.JSON};
    }
}