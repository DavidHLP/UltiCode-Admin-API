package com.david.commons.redis.operations.impl;

import com.david.commons.redis.exception.RedisCommonsException;
import com.david.commons.redis.operations.RedisStringOperations;
import com.david.commons.redis.serialization.RedisSerializer;
import com.david.commons.redis.serialization.SerializationStrategySelector;
import com.david.commons.redis.serialization.SerializationType;
import com.david.commons.redis.serialization.impl.JsonRedisSerializer;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Redis 字符串操作实现类
 *
 * <p>提供链式调用和方法级泛型的强类型读取能力
 *
 * @author David
 */
@Slf4j
public class RedisStringOperationsImpl implements RedisStringOperations {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ValueOperations<String, Object> valueOperations;
    private final SerializationStrategySelector strategySelector;
    private final SerializationType serializationType;

    public RedisStringOperationsImpl(
            RedisTemplate<String, Object> redisTemplate,
            SerializationStrategySelector strategySelector) {
        this.redisTemplate = redisTemplate;
        this.valueOperations = redisTemplate.opsForValue();
        this.strategySelector = strategySelector;
        this.serializationType = strategySelector.getDefaultStrategy();
    }

    private RedisStringOperationsImpl(
            RedisTemplate<String, Object> redisTemplate,
            SerializationStrategySelector strategySelector,
            SerializationType serializationType) {
        this.redisTemplate = redisTemplate;
        this.valueOperations = redisTemplate.opsForValue();
        this.strategySelector = strategySelector;
        this.serializationType = serializationType;
    }

    @Override
    public RedisStringOperations using(SerializationType serializationType) {
        if (serializationType == null || serializationType == this.serializationType) {
            return this;
        }
        return new RedisStringOperationsImpl(redisTemplate, strategySelector, serializationType);
    }

    @Override
    public Boolean set(String key, Object value) {
        try {
            log.debug("为键设置值: {}", key);
            valueOperations.set(key, value);
            return true;
        } catch (Exception e) {
            log.error("为键设置值失败: {}", key, e);
            throw new RedisCommonsException("REDIS_SET_ERROR", "为键设置值失败: " + key, e);
        }
    }

    @Override
    public Boolean set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            log.debug("为键设置值(带超时): {} 超时: {} {}", key, timeout, unit);
            valueOperations.set(key, value, timeout, unit);
            return true;
        } catch (Exception e) {
            log.error("为键设置值(带超时)失败: {}", key, e);
            throw new RedisCommonsException("REDIS_SET_ERROR", "为键设置值(带超时)失败: " + key, e);
        }
    }

    @Override
    public Boolean set(String key, Object value, Duration duration) {
        try {
            log.debug("为键设置值(带持续时间): {} 持续时间: {}", key, duration);
            valueOperations.set(key, value, duration);
            return true;
        } catch (Exception e) {
            log.error("为键设置值(带持续时间)失败: {}", key, e);
            throw new RedisCommonsException("REDIS_SET_ERROR", "为键设置值(带持续时间)失败: " + key, e);
        }
    }

    @Override
    public Boolean setIfAbsent(String key, Object value) {
        try {
            log.debug("为键设置值(如果不存在): {}", key);
            Boolean result = valueOperations.setIfAbsent(key, value);
            return result != null ? result : false;
        } catch (Exception e) {
            log.error("为键设置值(如果不存在)失败: {}", key, e);
            throw new RedisCommonsException(
                    "REDIS_SET_IF_ABSENT_ERROR", "为键设置值(如果不存在)失败: " + key, e);
        }
    }

    @Override
    public Boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
        try {
            log.debug("为键设置值(如果不存在,带超时): {} 超时: {} {}", key, timeout, unit);
            Boolean result = valueOperations.setIfAbsent(key, value, timeout, unit);
            return result != null ? result : false;
        } catch (Exception e) {
            log.error("为键设置值(如果不存在,带超时)失败: {}", key, e);
            throw new RedisCommonsException(
                    "REDIS_SET_IF_ABSENT_ERROR", "为键设置值(如果不存在,带超时)失败: " + key, e);
        }
    }

    @Override
    public <T> T get(String key, Class<T> valueType) {
        try {
            log.debug("获取键的值: {}", key);
            Object value = valueOperations.get(key);
            if (value == null) {
                return null;
            }

            // 如果值已经是目标类型，直接返回
            if (valueType.isInstance(value)) {
                return valueType.cast(value);
            }

            // 否则尝试类型转换
            return convertValue(value, valueType);
        } catch (Exception e) {
            log.error("获取键的值失败: {}", key, e);
            throw new RedisCommonsException("REDIS_GET_ERROR", "获取键的值失败: " + key, e);
        }
    }

    @Override
    public <T> T getAndSet(String key, T value, Class<T> valueType) {
        try {
            log.debug("获取并设置键的值: {}", key);
            Object oldValue = valueOperations.getAndSet(key, value);
            if (oldValue == null) {
                return null;
            }

            if (valueType.isInstance(oldValue)) {
                return valueType.cast(oldValue);
            }

            return convertValue(oldValue, valueType);
        } catch (Exception e) {
            log.error("获取并设置键的值失败: {}", key, e);
            throw new RedisCommonsException("REDIS_GET_AND_SET_ERROR", "获取并设置键的值失败: " + key, e);
        }
    }

    @Override
    public Long increment(String key) {
        try {
            log.debug("增加键的值: {}", key);
            return valueOperations.increment(key);
        } catch (Exception e) {
            log.error("增加键的值失败: {}", key, e);
            throw new RedisCommonsException("REDIS_INCREMENT_ERROR", "增加键的值失败: " + key, e);
        }
    }

    @Override
    public Long increment(String key, long delta) {
        try {
            log.debug("按增量增加键的值: {} 增量: {}", key, delta);
            return valueOperations.increment(key, delta);
        } catch (Exception e) {
            log.error("按增量增加键的值失败: {}", key, e);
            throw new RedisCommonsException("REDIS_INCREMENT_ERROR", "按增量增加键的值失败: " + key, e);
        }
    }

    @Override
    public Long decrement(String key) {
        try {
            log.debug("减少键的值: {}", key);
            return valueOperations.decrement(key);
        } catch (Exception e) {
            log.error("减少键的值失败: {}", key, e);
            throw new RedisCommonsException("REDIS_DECREMENT_ERROR", "减少键的值失败: " + key, e);
        }
    }

    @Override
    public Long decrement(String key, long delta) {
        try {
            log.debug("按减量减少键的值: {} 减量: {}", key, delta);
            return valueOperations.decrement(key, delta);
        } catch (Exception e) {
            log.error("按减量减少键的值失败: {}", key, e);
            throw new RedisCommonsException("REDIS_DECREMENT_ERROR", "按减量减少键的值失败: " + key, e);
        }
    }

    @Override
    public Long size(String key) {
        try {
            log.debug("获取字符串键的长度: {}", key);
            return valueOperations.size(key);
        } catch (Exception e) {
            log.error("获取字符串键的长度失败: {}", key, e);
            throw new RedisCommonsException("REDIS_SIZE_ERROR", "获取字符串键的长度失败: " + key, e);
        }
    }

    @Override
    public Long delete(String... keys) {
        try {
            log.debug("删除键: {}", Arrays.toString(keys));
            return redisTemplate.delete(Arrays.stream(keys).toList());
        } catch (Exception e) {
            log.error("删除键失败: {}", Arrays.toString(keys), e);
            throw new RedisCommonsException(
                    "REDIS_DELETE_ERROR", "删除键失败: " + Arrays.toString(keys), e);
        }
    }

    /**
     * 类型转换辅助方法
     *
     * @param value 原始值
     * @param targetType 目标类型
     * @return 转换后的值
     */
    private <T> T convertValue(Object value, Class<T> targetType) {
        if (value == null) {
            return null;
        }

        // 如果已经是目标类型
        if (targetType.isInstance(value)) {
            return targetType.cast(value);
        }

        // 字符串类型转换
        if (targetType == String.class) {
            return targetType.cast(value.toString());
        }

        // 数值类型转换
        if (value instanceof Number number) {
            Object result = convertNumberValue(number, targetType);
            if (result != null) {
                return targetType.cast(result);
            }
        }

        // 字符串到数值类型的转换
        if (value instanceof String stringValue) {
            Object result = convertStringValue(stringValue, targetType);
            if (result != null) {
                return targetType.cast(result);
            }
        }

        // Map/Collection -> POJO 回退转换（兼容旧缓存无类型信息的 JSON 反序列化为 LinkedHashMap 的场景）
        if (value instanceof java.util.Map || value instanceof java.util.Collection) {
            T result = convertCollectionValue(value, targetType);
            if (result != null) {
                return result;
            }
        }

        // 如果无法转换，尝试使用序列化器
        T result = convertUsingSerializer(value, targetType);
        if (result != null) {
            return result;
        }

        // 最后抛出类型转换异常
        throw new RedisCommonsException(
                "REDIS_TYPE_CONVERSION_ERROR",
                "无法将类型 " + value.getClass().getSimpleName() + " 转换为 " + targetType.getSimpleName());
    }

    /** 数值类型转换 */
    private <T> Object convertNumberValue(Number number, Class<T> targetType) {
        if (targetType == Integer.class || targetType == int.class) {
            return number.intValue();
        } else if (targetType == Long.class || targetType == long.class) {
            return number.longValue();
        } else if (targetType == Double.class || targetType == double.class) {
            return number.doubleValue();
        } else if (targetType == Float.class || targetType == float.class) {
            return number.floatValue();
        }
        return null;
    }

    /** 字符串到数值类型的转换 */
    private <T> Object convertStringValue(String stringValue, Class<T> targetType) {
        try {
            if (targetType == Integer.class || targetType == int.class) {
                return Integer.valueOf(stringValue);
            } else if (targetType == Long.class || targetType == long.class) {
                return Long.valueOf(stringValue);
            } else if (targetType == Double.class || targetType == double.class) {
                return Double.valueOf(stringValue);
            } else if (targetType == Float.class || targetType == float.class) {
                return Float.valueOf(stringValue);
            } else if (targetType == Boolean.class || targetType == boolean.class) {
                return Boolean.valueOf(stringValue);
            }
        } catch (NumberFormatException e) {
            log.warn("字符串 '{}' 转换为 {} 类型失败", stringValue, targetType.getSimpleName());
        }
        return null;
    }

    /** 集合类型转换 */
    private <T> T convertCollectionValue(Object value, Class<T> targetType) {
        try {
            RedisSerializer<Object> jsonSer =
                    strategySelector.getSerializerWithFallback(SerializationType.JSON, targetType);
            if (jsonSer instanceof JsonRedisSerializer jr) {
                return jr.getObjectMapper().convertValue(value, targetType);
            }
        } catch (Exception e) {
            log.debug("通过ObjectMapper将Map/Collection转换为 {} 失败", targetType.getSimpleName(), e);
        }
        return null;
    }

    /** 使用序列化器转换 */
    private <T> T convertUsingSerializer(Object value, Class<T> targetType) {
        try {
            RedisSerializer<Object> serializer =
                    strategySelector.getSerializerWithFallback(this.serializationType, targetType);

            if (value instanceof byte[]) {
                return serializer.deserialize((byte[]) value, targetType);
            } else if (value instanceof String) {
                return serializer.deserialize(((String) value).getBytes(), targetType);
            }
        } catch (Exception e) {
            log.debug("使用序列化器反序列化值失败", e);
        }
        return null;
    }
}
