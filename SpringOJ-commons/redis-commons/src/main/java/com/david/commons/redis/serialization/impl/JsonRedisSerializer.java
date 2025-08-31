package com.david.commons.redis.serialization.impl;

import com.david.commons.redis.serialization.RedisSerializationException;
import com.david.commons.redis.serialization.RedisSerializer;
import com.david.commons.redis.serialization.SerializationType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 基于 Jackson 的 JSON 序列化器
 * 特点：跨语言兼容，调试友好，可读性强
 *
 * @author David
 */
@Slf4j
@Component
public class JsonRedisSerializer implements RedisSerializer<Object> {

    private final ObjectMapper objectMapper;

    public JsonRedisSerializer() {
        this.objectMapper = createObjectMapper();
    }

    public JsonRedisSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper != null ? objectMapper : createObjectMapper();
    }

    /**
     * 创建配置好的 ObjectMapper
     */
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 注册 Java 8 时间模块
        mapper.registerModule(new JavaTimeModule());

        // 配置序列化选项
        mapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);

        return mapper;
    }

    @Override
    public byte[] serialize(Object object) throws RedisSerializationException {
        if (object == null) {
            return new byte[0];
        }

        try {
            String json = objectMapper.writeValueAsString(object);
            return json.getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            log.error("JSON serialization failed for object: {}", object.getClass().getName(), e);
            throw new RedisSerializationException("JSON_SERIALIZE_ERROR",
                    "Failed to serialize object to JSON", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R deserialize(byte[] bytes, Class<R> type) throws RedisSerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try {
            String json = new String(bytes, StandardCharsets.UTF_8);

            // 处理基本类型
            if (type == String.class) {
                // 如果目标类型是 String，且 JSON 是带引号的字符串，则去掉引号
                if (json.startsWith("\"") && json.endsWith("\"")) {
                    return (R) json.substring(1, json.length() - 1);
                }
                return (R) json;
            }

            return (R) objectMapper.readValue(json, TypeFactory.defaultInstance().constructType(type));
        } catch (IOException e) {
            log.error("JSON deserialization failed for type: {}, data: {}",
                    type.getName(), new String(bytes, StandardCharsets.UTF_8), e);
            throw new RedisSerializationException("JSON_DESERIALIZE_ERROR",
                    "Failed to deserialize JSON to object", e);
        }
    }

    @Override
    public SerializationType getType() {
        return SerializationType.JSON;
    }

    @Override
    public boolean supports(Class<?> type) {
        // JSON 序列化支持大部分 Java 对象
        // 排除一些特殊类型
        return type != null &&
                !type.isArray() ||
                type == byte[].class ||
                type.getComponentType().isPrimitive() ||
                String.class.isAssignableFrom(type.getComponentType());
    }

    /**
     * 获取内部使用的 ObjectMapper
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}