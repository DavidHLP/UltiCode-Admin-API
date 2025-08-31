package com.david.commons.redis.serialization.impl;

import com.david.commons.redis.serialization.RedisSerializationException;
import com.david.commons.redis.serialization.RedisSerializer;
import com.david.commons.redis.serialization.SerializationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * 基于 JDK 的原生序列化器
 * 特点：Java 原生支持，兼容性好，但性能较低，体积较大
 *
 * @author David
 */
@Slf4j
@Component
public class JdkRedisSerializer implements RedisSerializer<Object> {

    @Override
    public byte[] serialize(Object object) throws RedisSerializationException {
        if (object == null) {
            return new byte[0];
        }

        // 检查对象是否实现了 Serializable 接口
        if (!(object instanceof Serializable)) {
            throw new RedisSerializationException("JDK_NOT_SERIALIZABLE",
                    "Object must implement Serializable interface: {}",
                    object.getClass().getName());
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            oos.writeObject(object);
            oos.flush();
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("JDK serialization failed for object: {}", object.getClass().getName(), e);
            throw new RedisSerializationException("JDK_SERIALIZE_ERROR",
                    "Failed to serialize object with JDK serialization", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R deserialize(byte[] bytes, Class<R> type) throws RedisSerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bais)) {

            Object result = ois.readObject();

            // 类型检查
            if (result != null && !type.isAssignableFrom(result.getClass())) {
                throw new RedisSerializationException("JDK_TYPE_MISMATCH",
                        "Deserialized object type {} does not match expected type {}",
                        result.getClass().getName(), type.getName());
            }

            return (R) result;

        } catch (IOException e) {
            log.error("JDK deserialization IO failed for type: {}", type.getName(), e);
            throw new RedisSerializationException("JDK_DESERIALIZE_IO_ERROR",
                    "IO error during JDK deserialization", e);
        } catch (ClassNotFoundException e) {
            log.error("JDK deserialization class not found for type: {}", type.getName(), e);
            throw new RedisSerializationException("JDK_DESERIALIZE_CLASS_ERROR",
                    "Class not found during JDK deserialization", e);
        } catch (ClassCastException e) {
            log.error("JDK deserialization type cast failed for type: {}", type.getName(), e);
            throw new RedisSerializationException("JDK_DESERIALIZE_CAST_ERROR",
                    "Type cast error during JDK deserialization", e);
        }
    }

    @Override
    public SerializationType getType() {
        return SerializationType.JDK;
    }

    @Override
    public boolean supports(Class<?> type) {
        // JDK 序列化只支持实现了 Serializable 接口的类
        return type != null && Serializable.class.isAssignableFrom(type);
    }

    /**
     * 检查对象是否可以被 JDK 序列化
     *
     * @param object 待检查的对象
     * @return 是否可序列化
     */
    public boolean isSerializable(Object object) {
        return object instanceof Serializable;
    }

    /**
     * 获取序列化后的大小估算
     *
     * @param object 待序列化对象
     * @return 序列化后的字节数，如果无法序列化返回 -1
     */
    public long getSerializedSize(Object object) {
        if (!isSerializable(object)) {
            return -1;
        }

        try {
            byte[] serialized = serialize(object);
            return serialized.length;
        } catch (RedisSerializationException e) {
            log.warn("Failed to calculate serialized size for object: {}",
                    object.getClass().getName(), e);
            return -1;
        }
    }
}