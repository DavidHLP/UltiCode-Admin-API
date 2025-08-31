package com.david.commons.redis.serialization.impl;

import com.david.commons.redis.serialization.RedisSerializationException;
import com.david.commons.redis.serialization.RedisSerializer;
import com.david.commons.redis.serialization.SerializationType;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;

/**
 * 基于 Kryo 的高性能序列化器
 * 特点：高性能，体积小，仅支持 Java
 *
 * @author David
 */
@Slf4j
@Component
public class KryoRedisSerializer implements RedisSerializer<Object> {

    private final ThreadLocal<Kryo> kryoThreadLocal;

    public KryoRedisSerializer() {
        this.kryoThreadLocal = ThreadLocal.withInitial(this::createKryo);
    }

    /**
     * 创建 Kryo 实例
     */
    private Kryo createKryo() {
        Kryo kryo = new Kryo();

        // 配置 Kryo
        kryo.setReferences(true); // 支持循环引用
        kryo.setRegistrationRequired(false); // 不要求预注册类

        // 注册常用类以提高性能
        registerCommonClasses(kryo);

        return kryo;
    }

    /**
     * 注册常用类以提高序列化性能
     */
    private void registerCommonClasses(Kryo kryo) {
        // 基本类型包装类
        kryo.register(Boolean.class);
        kryo.register(Byte.class);
        kryo.register(Character.class);
        kryo.register(Short.class);
        kryo.register(Integer.class);
        kryo.register(Long.class);
        kryo.register(Float.class);
        kryo.register(Double.class);
        kryo.register(String.class);

        // 常用集合类
        kryo.register(java.util.ArrayList.class);
        kryo.register(java.util.LinkedList.class);
        kryo.register(java.util.HashMap.class);
        kryo.register(java.util.LinkedHashMap.class);
        kryo.register(java.util.TreeMap.class);
        kryo.register(java.util.HashSet.class);
        kryo.register(java.util.LinkedHashSet.class);
        kryo.register(java.util.TreeSet.class);

        // 时间类
        kryo.register(java.util.Date.class);
        kryo.register(java.time.LocalDateTime.class);
        kryo.register(java.time.LocalDate.class);
        kryo.register(java.time.LocalTime.class);
    }

    @Override
    public byte[] serialize(Object object) throws RedisSerializationException {
        if (object == null) {
            return new byte[0];
        }

        Kryo kryo = kryoThreadLocal.get();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Output output = new Output(baos)) {

            kryo.writeClassAndObject(output, object);
            output.flush();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Kryo serialization failed for object: {}", object.getClass().getName(), e);
            throw new RedisSerializationException("KRYO_SERIALIZE_ERROR",
                    "Failed to serialize object with Kryo", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R deserialize(byte[] bytes, Class<R> type) throws RedisSerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        Kryo kryo = kryoThreadLocal.get();
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                Input input = new Input(bais)) {

            Object result = kryo.readClassAndObject(input);

            // 类型检查
            if (result != null && !type.isAssignableFrom(result.getClass())) {
                throw new RedisSerializationException("KRYO_TYPE_MISMATCH",
                        "Deserialized object type {} does not match expected type {}",
                        result.getClass().getName(), type.getName());
            }

            return (R) result;

        } catch (Exception e) {
            log.error("Kryo deserialization failed for type: {}", type.getName(), e);
            throw new RedisSerializationException("KRYO_DESERIALIZE_ERROR",
                    "Failed to deserialize object with Kryo", e);
        }
    }

    @Override
    public SerializationType getType() {
        return SerializationType.KRYO;
    }

    @Override
    public boolean supports(Class<?> type) {
        // Kryo 支持所有实现了 Serializable 接口的类
        // 以及基本类型和常用集合类
        return type != null && (Serializable.class.isAssignableFrom(type) ||
                type.isPrimitive() ||
                isPrimitiveWrapper(type) ||
                type == String.class ||
                java.util.Collection.class.isAssignableFrom(type) ||
                java.util.Map.class.isAssignableFrom(type));
    }

    /**
     * 检查是否为基本类型包装类
     */
    private boolean isPrimitiveWrapper(Class<?> type) {
        return type == Boolean.class ||
                type == Byte.class ||
                type == Character.class ||
                type == Short.class ||
                type == Integer.class ||
                type == Long.class ||
                type == Float.class ||
                type == Double.class;
    }

    /**
     * 清理当前线程的 Kryo 实例
     */
    public void clearThreadLocal() {
        kryoThreadLocal.remove();
    }

    /**
     * 获取当前线程的 Kryo 实例信息
     */
    public String getThreadLocalInfo() {
        Kryo kryo = kryoThreadLocal.get();
        return String.format("Kryo instance for thread %s: %s",
                Thread.currentThread().getName(), kryo.toString());
    }
}