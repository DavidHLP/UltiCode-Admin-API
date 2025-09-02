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
import java.util.stream.Collectors;

/**
 * Redis 字符串操作实现类 提供强类型支持和链式调用能力
 *
 * @param <T> 值类型
 * @author David
 */
@Slf4j
public class RedisStringOperationsImpl<T> implements RedisStringOperations<T> {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ValueOperations<String, Object> valueOperations;
    private final SerializationStrategySelector strategySelector;
    private final Class<T> valueType;

    public RedisStringOperationsImpl(
            RedisTemplate<String, Object> redisTemplate,
            SerializationStrategySelector strategySelector,
            Class<T> valueType) {
        this.redisTemplate = redisTemplate;
        this.valueOperations = redisTemplate.opsForValue();
        this.strategySelector = strategySelector;
        this.valueType = valueType;
    }

    @Override
    public Boolean set(String key, T value) {
        try {
            log.debug("Setting value for key: {}", key);
            valueOperations.set(key, value);
            return true;
        } catch (Exception e) {
            log.error("Failed to set value for key: {}", key, e);
            throw new RedisCommonsException(
                    "REDIS_SET_ERROR", "Failed to set value for key: " + key, e);
        }
    }

    @Override
    public Boolean set(String key, T value, long timeout, TimeUnit unit) {
        try {
            log.debug("Setting value for key: {} with timeout: {} {}", key, timeout, unit);
            valueOperations.set(key, value, timeout, unit);
            return true;
        } catch (Exception e) {
            log.error("Failed to set value with timeout for key: {}", key, e);
            throw new RedisCommonsException(
                    "REDIS_SET_ERROR", "Failed to set value with timeout for key: " + key, e);
        }
    }

    @Override
    public Boolean set(String key, T value, Duration duration) {
        try {
            log.debug("Setting value for key: {} with duration: {}", key, duration);
            valueOperations.set(key, value, duration);
            return true;
        } catch (Exception e) {
            log.error("Failed to set value with duration for key: {}", key, e);
            throw new RedisCommonsException(
                    "REDIS_SET_ERROR", "Failed to set value with duration for key: " + key, e);
        }
    }

    @Override
    public Boolean setIfAbsent(String key, T value) {
        try {
            log.debug("Setting value if absent for key: {}", key);
            Boolean result = valueOperations.setIfAbsent(key, value);
            return result != null ? result : false;
        } catch (Exception e) {
            log.error("Failed to set value if absent for key: {}", key, e);
            throw new RedisCommonsException(
                    "REDIS_SET_IF_ABSENT_ERROR",
                    "Failed to set value if absent for key: " + key,
                    e);
        }
    }

    @Override
    public Boolean setIfAbsent(String key, T value, long timeout, TimeUnit unit) {
        try {
            log.debug(
                    "Setting value if absent for key: {} with timeout: {} {}", key, timeout, unit);
            Boolean result = valueOperations.setIfAbsent(key, value, timeout, unit);
            return result != null ? result : false;
        } catch (Exception e) {
            log.error("Failed to set value if absent with timeout for key: {}", key, e);
            throw new RedisCommonsException(
                    "REDIS_SET_IF_ABSENT_ERROR",
                    "Failed to set value if absent with timeout for key: " + key,
                    e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(String key) {
        try {
            log.debug("Getting value for key: {}", key);
            Object value = valueOperations.get(key);
            if (value == null) {
                return null;
            }

            // 如果值已经是目标类型，直接返回
            if (valueType.isInstance(value)) {
                return (T) value;
            }

            // 否则尝试类型转换
            return convertValue(value, valueType);
        } catch (Exception e) {
            log.error("Failed to get value for key: {}", key, e);
            throw new RedisCommonsException(
                    "REDIS_GET_ERROR", "Failed to get value for key: " + key, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getAndSet(String key, T value) {
        try {
            log.debug("Getting and setting value for key: {}", key);
            Object oldValue = valueOperations.getAndSet(key, value);
            if (oldValue == null) {
                return null;
            }

            if (valueType.isInstance(oldValue)) {
                return (T) oldValue;
            }

            return convertValue(oldValue, valueType);
        } catch (Exception e) {
            log.error("Failed to get and set value for key: {}", key, e);
            throw new RedisCommonsException(
                    "REDIS_GET_AND_SET_ERROR", "Failed to get and set value for key: " + key, e);
        }
    }

    @Override
    public Long increment(String key) {
        try {
            log.debug("Incrementing value for key: {}", key);
            return valueOperations.increment(key);
        } catch (Exception e) {
            log.error("Failed to increment value for key: {}", key, e);
            throw new RedisCommonsException(
                    "REDIS_INCREMENT_ERROR", "Failed to increment value for key: " + key, e);
        }
    }

    @Override
    public Long increment(String key, long delta) {
        try {
            log.debug("Incrementing value for key: {} by delta: {}", key, delta);
            return valueOperations.increment(key, delta);
        } catch (Exception e) {
            log.error("Failed to increment value by delta for key: {}", key, e);
            throw new RedisCommonsException(
                    "REDIS_INCREMENT_ERROR",
                    "Failed to increment value by delta for key: " + key,
                    e);
        }
    }

    @Override
    public Long decrement(String key) {
        try {
            log.debug("Decrementing value for key: {}", key);
            return valueOperations.decrement(key);
        } catch (Exception e) {
            log.error("Failed to decrement value for key: {}", key, e);
            throw new RedisCommonsException(
                    "REDIS_DECREMENT_ERROR", "Failed to decrement value for key: " + key, e);
        }
    }

    @Override
    public Long decrement(String key, long delta) {
        try {
            log.debug("Decrementing value for key: {} by delta: {}", key, delta);
            return valueOperations.decrement(key, delta);
        } catch (Exception e) {
            log.error("Failed to decrement value by delta for key: {}", key, e);
            throw new RedisCommonsException(
                    "REDIS_DECREMENT_ERROR",
                    "Failed to decrement value by delta for key: " + key,
                    e);
        }
    }

    @Override
    public Long size(String key) {
        try {
            log.debug("Getting string length for key: {}", key);
            return valueOperations.size(key);
        } catch (Exception e) {
            log.error("Failed to get string length for key: {}", key, e);
            throw new RedisCommonsException(
                    "REDIS_SIZE_ERROR", "Failed to get string length for key: " + key, e);
        }
    }

    @Override
    public Long delete(String... keys) {
        return redisTemplate.delete(Arrays.stream(keys).toList());
    }

    /**
     * 类型转换辅助方法
     *
     * @param value 原始值
     * @param targetType 目标类型
     * @return 转换后的值
     */
    @SuppressWarnings("unchecked")
    private T convertValue(Object value, Class<T> targetType) {
        if (value == null) {
            return null;
        }

        // 如果已经是目标类型
        if (targetType.isInstance(value)) {
            return (T) value;
        }

        // 字符串类型转换
        if (targetType == String.class) {
            return (T) value.toString();
        }

        // 数值类型转换
        if (value instanceof Number number) {
            if (targetType == Integer.class || targetType == int.class) {
                return (T) Integer.valueOf(number.intValue());
            } else if (targetType == Long.class || targetType == long.class) {
                return (T) Long.valueOf(number.longValue());
            } else if (targetType == Double.class || targetType == double.class) {
                return (T) Double.valueOf(number.doubleValue());
            } else if (targetType == Float.class || targetType == float.class) {
                return (T) Float.valueOf(number.floatValue());
            }
        }

        // 字符串到数值类型的转换
        if (value instanceof String stringValue) {
            try {
                if (targetType == Integer.class || targetType == int.class) {
                    return (T) Integer.valueOf(stringValue);
                } else if (targetType == Long.class || targetType == long.class) {
                    return (T) Long.valueOf(stringValue);
                } else if (targetType == Double.class || targetType == double.class) {
                    return (T) Double.valueOf(stringValue);
                } else if (targetType == Float.class || targetType == float.class) {
                    return (T) Float.valueOf(stringValue);
                } else if (targetType == Boolean.class || targetType == boolean.class) {
                    return (T) Boolean.valueOf(stringValue);
                }
            } catch (NumberFormatException e) {
                log.warn(
                        "Failed to convert string '{}' to {}",
                        stringValue,
                        targetType.getSimpleName());
            }
        }

        // Map/Collection -> POJO 回退转换（兼容旧缓存无类型信息的 JSON 反序列化为 LinkedHashMap 的场景）
        if (value instanceof java.util.Map || value instanceof java.util.Collection) {
            try {
                RedisSerializer<T> jsonSer =
                        strategySelector.getSerializerWithFallback(SerializationType.JSON, targetType);
                if (jsonSer instanceof JsonRedisSerializer jr) {
                    return jr.getObjectMapper().convertValue(value, targetType);
                }
            } catch (Exception e) {
                log.debug("Failed to convert Map/Collection to {} via ObjectMapper", targetType.getSimpleName(), e);
            }
        }

        // 如果无法转换，尝试使用序列化器
        try {
            RedisSerializer<T> serializer =
                    strategySelector.getSerializerWithFallback(
                            strategySelector.selectStrategy(targetType), targetType);

            if (value instanceof byte[]) {
                return serializer.deserialize((byte[]) value, targetType);
            } else if (value instanceof String) {
                return serializer.deserialize(((String) value).getBytes(), targetType);
            }
        } catch (Exception e) {
            log.debug("Failed to deserialize value using serializer", e);
        }

        // 最后尝试强制类型转换
        try {
            return (T) value;
        } catch (ClassCastException e) {
            throw new RedisCommonsException(
                    "REDIS_TYPE_CONVERSION_ERROR",
                    "Cannot convert value of type "
                            + value.getClass().getSimpleName()
                            + " to "
                            + targetType.getSimpleName(),
                    e);
        }
    }
}
