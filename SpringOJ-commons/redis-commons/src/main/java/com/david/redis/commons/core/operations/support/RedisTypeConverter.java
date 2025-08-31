package com.david.redis.commons.core.operations.support;

import com.david.redis.commons.exception.RedisOperationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/// Redis类型转换工具类
/// 提供统一的类型转换功能，支持基本类型和复杂对象的转换
/// @author David
public final class RedisTypeConverter {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // 基本类型转换器映射
    private static final Map<Class<?>, Function<Object, ?>> PRIMITIVE_CONVERTERS =
            Map.of(
                    String.class, Object::toString,
                    Integer.class, RedisTypeConverter::convertToInteger,
                    int.class, RedisTypeConverter::convertToInteger,
                    Long.class, RedisTypeConverter::convertToLong,
                    long.class, RedisTypeConverter::convertToLong,
                    Double.class, RedisTypeConverter::convertToDouble,
                    double.class, RedisTypeConverter::convertToDouble,
                    Boolean.class, RedisTypeConverter::convertToBoolean,
                    boolean.class, RedisTypeConverter::convertToBoolean);

    private RedisTypeConverter() {
        // 工具类，防止实例化
    }

    /// 创建并配置 ObjectMapper 实例。
    /// 配置内容包括：
    /// - 注册 JavaTimeModule，支持 LocalDateTime 序列化/反序列化
    /// - 忽略未知属性
    /// - 日期不写为时间戳
    /// @return 配置好的 ObjectMapper 实例
    private static ObjectMapper createObjectMapper() {
        var mapper = new ObjectMapper();

        var javaTimeModule = new JavaTimeModule();
        configureLocalDateTimeSerialization(javaTimeModule);

        return mapper.registerModule(javaTimeModule)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /// 配置 LocalDateTime 的序列化和反序列化规则。
    /// @param javaTimeModule 时间模块，用于注册序列化器和反序列化器
    private static void configureLocalDateTimeSerialization(JavaTimeModule javaTimeModule) {
        javaTimeModule.addDeserializer(LocalDateTime.class, createLocalDateTimeDeserializer());
        javaTimeModule.addSerializer(
                LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMAT));
    }

    /// 创建 LocalDateTime 反序列化器。
    /// 支持的格式：
    /// - yyyy-MM-dd HH:mm:ss
    /// - yyyy-MM-dd
    /// - ISO 格式（默认回退）
    /// @return LocalDateTimeDeserializer 实例
    private static LocalDateTimeDeserializer createLocalDateTimeDeserializer() {
        return new LocalDateTimeDeserializer(DATE_TIME_FORMAT) {
            @Override
            public LocalDateTime deserialize(
                    com.fasterxml.jackson.core.JsonParser parser,
                    com.fasterxml.jackson.databind.DeserializationContext context)
                    throws java.io.IOException {

                return Optional.ofNullable(parser.getValueAsString())
                        .filter(dateString -> !dateString.trim().isEmpty())
                        .map(RedisTypeConverter::parseLocalDateTime)
                        .orElse(null);
            }
        };
    }

    /// 将字符串解析为 LocalDateTime。
    /// 支持格式：
    /// - yyyy-MM-dd HH:mm:ss
    /// - yyyy-MM-dd（补充 00:00:00）
    /// - ISO 标准格式（回退解析）
    /// @param dateString 日期字符串
    /// @return 解析得到的 LocalDateTime
    private static LocalDateTime parseLocalDateTime(String dateString) {
        try {
            // 包含时间信息的格式
            if (dateString.contains(" ") || dateString.contains("T")) {
                return LocalDateTime.parse(dateString, DATE_TIME_FORMAT);
            }

            // 只有日期的情况
            return LocalDate.parse(dateString, DATE_FORMAT).atStartOfDay();

        } catch (DateTimeParseException e) {
            // 尝试 ISO 格式
            return LocalDateTime.parse(dateString);
        }
    }

    /// 将值转换为指定类型。
    /// @param value 原始值
    /// @param clazz 目标类型
    /// @param <T> 泛型类型
    /// @return 转换后的值，如果原始值为 null，则返回 null
    /// @throws RedisOperationException 转换失败时抛出异常
    public static <T> T convertValue(Object value, Class<T> clazz) {
        return Optional.ofNullable(value).map(val -> performConversion(val, clazz)).orElse(null);
    }

    /// 执行具体的类型转换逻辑。
    /// 转换优先级：
    /// - 类型直接匹配 → 返回原值
    /// - 基本类型 → 使用预定义转换器
    /// - 复杂对象 → 使用 Jackson 转换
    /// @param value 原始值
    /// @param clazz 目标类型
    /// @param <T> 泛型类型
    /// @return 转换后的值
    @SuppressWarnings("unchecked")
    private static <T> T performConversion(Object value, Class<T> clazz) {
        // 如果类型匹配，直接返回
        if (clazz.isInstance(value)) {
            return (T) value;
        }

        // 基本类型转换或复杂对象转换
        return (T)
                PRIMITIVE_CONVERTERS
                        .getOrDefault(clazz, obj -> convertComplexObject(obj, clazz))
                        .apply(value);
    }

    /// 使用 Jackson 将对象转换为指定类型。
    /// @param value 原始值（一般为 JSON 字符串）
    /// @param clazz 目标类型
    /// @param <T> 泛型类型
    /// @return 转换后的对象
    /// @throws RedisOperationException 转换失败时抛出
    private static <T> T convertComplexObject(Object value, Class<T> clazz) {
        try {
            var jsonString = value.toString();
            return OBJECT_MAPPER.readValue(jsonString, clazz);
        } catch (JsonProcessingException e) {
            throw new RedisOperationException("类型转换失败", e, "CONVERT", value, clazz);
        }
    }

    /// 将对象转换为 Integer 类型。
    /// @param value 原始值
    /// @return 转换后的 Integer
    private static Integer convertToInteger(Object value) {
        return value instanceof Number number
                ? number.intValue()
                : Integer.parseInt(value.toString());
    }

    /// 将对象转换为 Long 类型。
    /// @param value 原始值
    /// @return 转换后的 Long
    private static Long convertToLong(Object value) {
        return value instanceof Number number
                ? number.longValue()
                : Long.parseLong(value.toString());
    }

    /// 将对象转换为 Double 类型。
    /// @param value 原始值
    /// @return 转换后的 Double
    private static Double convertToDouble(Object value) {
        return value instanceof Number number
                ? number.doubleValue()
                : Double.parseDouble(value.toString());
    }

    /// 将对象转换为 Boolean 类型。
    /// @param value 原始值
    /// @return 转换后的 Boolean
    private static Boolean convertToBoolean(Object value) {
        return value instanceof Boolean bool ? bool : Boolean.valueOf(value.toString());
    }
}
