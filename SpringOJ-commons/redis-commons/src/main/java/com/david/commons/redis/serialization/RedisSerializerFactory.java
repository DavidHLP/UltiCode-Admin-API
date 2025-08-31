package com.david.commons.redis.serialization;

import com.david.commons.redis.serialization.impl.JdkRedisSerializer;
import com.david.commons.redis.serialization.impl.JsonRedisSerializer;
import com.david.commons.redis.serialization.impl.KryoRedisSerializer;
import com.david.commons.redis.serialization.impl.ProtobufRedisSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Redis 序列化器工厂
 * 负责创建和管理不同类型的序列化器实例
 *
 * @author David
 */
@Component
public class RedisSerializerFactory {

    /**
     * 序列化器缓存，避免重复创建
     */
    private final Map<SerializationType, RedisSerializer<?>> serializerCache = new ConcurrentHashMap<>();

    /**
     * 各种序列化器实现
     */
    private final JsonRedisSerializer jsonSerializer;
    private final KryoRedisSerializer kryoSerializer;
    private final JdkRedisSerializer jdkSerializer;
    private final ProtobufRedisSerializer protobufSerializer;

    @Autowired
    public RedisSerializerFactory(JsonRedisSerializer jsonSerializer,
            KryoRedisSerializer kryoSerializer,
            JdkRedisSerializer jdkSerializer,
            ProtobufRedisSerializer protobufSerializer) {
        this.jsonSerializer = jsonSerializer;
        this.kryoSerializer = kryoSerializer;
        this.jdkSerializer = jdkSerializer;
        this.protobufSerializer = protobufSerializer;

        // 初始化缓存
        initializeCache();
    }

    /**
     * 初始化序列化器缓存
     */
    private void initializeCache() {
        serializerCache.put(SerializationType.JSON, jsonSerializer);
        serializerCache.put(SerializationType.KRYO, kryoSerializer);
        serializerCache.put(SerializationType.JDK, jdkSerializer);
        serializerCache.put(SerializationType.PROTOBUF, protobufSerializer);
    }

    /**
     * 获取指定类型的序列化器
     *
     * @param type 序列化类型
     * @param <T>  序列化对象类型
     * @return 序列化器实例
     * @throws RedisSerializationException 如果不支持指定的序列化类型
     */
    @SuppressWarnings("unchecked")
    public <T> RedisSerializer<T> getSerializer(SerializationType type) {
        RedisSerializer<?> serializer = serializerCache.get(type);
        if (serializer == null) {
            throw new RedisSerializationException("SERIALIZER_NOT_FOUND",
                    "Unsupported serialization type: " + type);
        }
        return (RedisSerializer<T>) serializer;
    }

    /**
     * 获取默认序列化器（JSON）
     *
     * @param <T> 序列化对象类型
     * @return 默认序列化器
     */
    public <T> RedisSerializer<T> getDefaultSerializer() {
        return getSerializer(SerializationType.JSON);
    }

    /**
     * 根据对象类型自动选择最优序列化器
     *
     * @param objectType 对象类型
     * @param <T>        序列化对象类型
     * @return 最优序列化器
     */
    public <T> RedisSerializer<T> getOptimalSerializer(Class<T> objectType) {
        // 基本类型和字符串使用 JSON
        if (isPrimitiveOrWrapper(objectType) || String.class.equals(objectType)) {
            return getSerializer(SerializationType.JSON);
        }

        // 实现了 Serializable 接口的复杂对象使用 Kryo（高性能）
        if (java.io.Serializable.class.isAssignableFrom(objectType)) {
            return getSerializer(SerializationType.KRYO);
        }

        // 其他情况使用 JSON（兼容性最好）
        return getSerializer(SerializationType.JSON);
    }

    /**
     * 检查是否为基本类型或其包装类
     */
    private boolean isPrimitiveOrWrapper(Class<?> type) {
        return type.isPrimitive() ||
                type.equals(Boolean.class) ||
                type.equals(Byte.class) ||
                type.equals(Character.class) ||
                type.equals(Short.class) ||
                type.equals(Integer.class) ||
                type.equals(Long.class) ||
                type.equals(Float.class) ||
                type.equals(Double.class);
    }

    /**
     * 检查指定类型是否支持序列化
     *
     * @param type       序列化类型
     * @param objectType 对象类型
     * @return 是否支持
     */
    public boolean supports(SerializationType type, Class<?> objectType) {
        RedisSerializer<?> serializer = serializerCache.get(type);
        return serializer != null && serializer.supports(objectType);
    }

    /**
     * 获取所有支持的序列化类型
     *
     * @return 支持的序列化类型数组
     */
    public SerializationType[] getSupportedTypes() {
        return serializerCache.keySet().toArray(new SerializationType[0]);
    }
}