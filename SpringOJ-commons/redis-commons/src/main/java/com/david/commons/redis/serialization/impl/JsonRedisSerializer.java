package com.david.commons.redis.serialization.impl;

import com.david.commons.redis.serialization.RedisSerializationException;
import com.david.commons.redis.serialization.RedisSerializer;
import com.david.commons.redis.serialization.SerializationType;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
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
        // 避免污染全局 ObjectMapper，这里进行复制后再按 Redis 需求定制
        this.objectMapper = objectMapper != null ? configureForRedis(objectMapper.copy()) : createObjectMapper();
    }

    /**
     * 创建配置好的 ObjectMapper
     */
    private ObjectMapper createObjectMapper() {
        return configureForRedis(new ObjectMapper());
    }

    /**
     * 复制 RedisCommonsAutoConfiguration 的 ObjectMapper 配置：
     * - 注册 JavaTimeModule
     * - 关闭时间戳，忽略未知字段
     * - 启用受限的多态类型信息（NON_FINAL, As.PROPERTY）
     */
    private ObjectMapper configureForRedis(ObjectMapper base) {
        ObjectMapper om = base;
        // Java 8 时间支持
        om.registerModule(new JavaTimeModule());
        // 序列化/反序列化特性
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        om.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);

        // 受信任包的多态类型校验器
        var ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.david")
                .allowIfSubType("java.time")
                .allowIfSubType("java.util")
                .build();
        om.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        return om;
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
        // JSON 基本支持几乎所有非数组类型
        if (type == null) {
            return false;
        }
        if (!type.isArray()) {
            return true;
        }
        // 数组特殊处理：仅支持 byte[]、原始类型数组和 String[]
        Class<?> component = type.getComponentType();
        if (component == null) {
            return false;
        }
        return type == byte[].class || component.isPrimitive() || String.class.isAssignableFrom(component);
    }

    /**
     * 获取内部使用的 ObjectMapper
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}