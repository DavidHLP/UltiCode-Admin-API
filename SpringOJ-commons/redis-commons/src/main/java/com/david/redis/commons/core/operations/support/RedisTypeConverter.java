package com.david.redis.commons.core.operations.support;

import com.david.redis.commons.exception.RedisOperationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Redis类型转换工具类
 *
 * <p>
 * 提供统一的类型转换功能，支持基本类型和复杂对象的转换
 *
 * @author David
 */
public class RedisTypeConverter {

    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    /**
     * 创建配置好的ObjectMapper实例
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 注册JavaTimeModule处理Java 8时间类型
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // 配置LocalDateTime的序列化和反序列化格式
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 自定义LocalDateTime反序列化器，支持多种格式
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter) {
            @Override
            public LocalDateTime deserialize(com.fasterxml.jackson.core.JsonParser parser,
                    com.fasterxml.jackson.databind.DeserializationContext context) throws java.io.IOException {
                String dateString = parser.getValueAsString();
                if (dateString == null || dateString.trim().isEmpty()) {
                    return null;
                }

                try {
                    // 尝试完整的日期时间格式
                    if (dateString.contains(" ") || dateString.contains("T")) {
                        return LocalDateTime.parse(dateString, dateTimeFormatter);
                    } else {
                        // 只有日期的情况，添加默认时间00:00:00
                        return java.time.LocalDate.parse(dateString, dateFormatter).atStartOfDay();
                    }
                } catch (Exception e) {
                    // 最后尝试ISO格式
                    return LocalDateTime.parse(dateString);
                }
            }
        });

        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));

        mapper.registerModule(javaTimeModule);

        // 配置其他选项
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        return mapper;
    }

    /**
     * 类型转换工具方法
     *
     * @param value 原始值
     * @param clazz 目标类型
     * @param <T>   泛型类型
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
