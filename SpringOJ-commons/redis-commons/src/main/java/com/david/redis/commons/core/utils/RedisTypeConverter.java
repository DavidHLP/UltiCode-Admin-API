package com.david.redis.commons.core.utils;

import com.david.redis.commons.exception.RedisOperationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Redis类型转换工具类
 * 
 * <p>提供统一的类型转换功能，支持基本类型和复杂对象的转换
 * 
 * @author David
 */
public class RedisTypeConverter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 类型转换工具方法
     *
     * @param value 原始值
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 转换后的值
     * @throws RedisOperationException 转换失败时抛出
     */
    @SuppressWarnings("unchecked")
    public static <T> T convertValue(Object value, Class<T> clazz) {
        if (value == null) {
            return null;
        }

        // 如果类型匹配，直接返回
        if (clazz.isInstance(value)) {
            return (T) value;
        }

        // 字符串类型转换
        if (clazz == String.class) {
            return (T) value.toString();
        }

        // 基本类型转换
        if (clazz == Integer.class || clazz == int.class) {
            if (value instanceof Number) {
                return (T) Integer.valueOf(((Number) value).intValue());
            }
            return (T) Integer.valueOf(value.toString());
        }

        if (clazz == Long.class || clazz == long.class) {
            if (value instanceof Number) {
                return (T) Long.valueOf(((Number) value).longValue());
            }
            return (T) Long.valueOf(value.toString());
        }

        if (clazz == Double.class || clazz == double.class) {
            if (value instanceof Number) {
                return (T) Double.valueOf(((Number) value).doubleValue());
            }
            return (T) Double.valueOf(value.toString());
        }

        if (clazz == Boolean.class || clazz == boolean.class) {
            if (value instanceof Boolean) {
                return (T) value;
            }
            return (T) Boolean.valueOf(value.toString());
        }

        // 复杂对象使用JSON转换
        try {
            String jsonString = value.toString();
            return OBJECT_MAPPER.readValue(jsonString, clazz);
        } catch (JsonProcessingException e) {
            throw new RedisOperationException("类型转换失败", e, "CONVERT", value, clazz);
        }
    }

    /**
     * 获取ObjectMapper实例
     * 
     * @return ObjectMapper实例
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }
}
