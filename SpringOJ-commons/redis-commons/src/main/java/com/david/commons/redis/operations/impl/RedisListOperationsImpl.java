package com.david.commons.redis.operations.impl;

import com.david.commons.redis.exception.RedisCommonsException;
import com.david.commons.redis.operations.RedisListOperations;
import com.david.commons.redis.serialization.SerializationStrategySelector;
import com.david.commons.redis.serialization.RedisSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis 列表操作实现类
 * 提供强类型支持和完整的列表操作能力
 *
 * @param <T> 元素类型
 * @author David
 */
@Slf4j
public class RedisListOperationsImpl<T> implements RedisListOperations<T> {

    private final ListOperations<String, Object> listOperations;
    private final SerializationStrategySelector strategySelector;
    private final Class<T> valueType;

    public RedisListOperationsImpl(RedisTemplate<String, Object> redisTemplate,
            SerializationStrategySelector strategySelector,
            Class<T> valueType) {
        this.listOperations = redisTemplate.opsForList();
        this.strategySelector = strategySelector;
        this.valueType = valueType;
    }

    @Override
    public Long leftPush(String key, T value) {
        try {
            log.debug("Left pushing value to list for key: {}", key);
            return listOperations.leftPush(key, value);
        } catch (Exception e) {
            log.error("Failed to left push value to list for key: {}", key, e);
            throw new RedisCommonsException("REDIS_LIST_LEFT_PUSH_ERROR",
                    "Failed to left push value to list for key: " + key, e);
        }
    }

    @Override
    @SafeVarargs
    public final Long leftPushAll(String key, T... values) {
        try {
            log.debug("Left pushing {} values to list for key: {}", values.length, key);
            Object[] objectValues = Arrays.stream(values).toArray();
            return listOperations.leftPushAll(key, objectValues);
        } catch (Exception e) {
            log.error("Failed to left push all values to list for key: {}", key, e);
            throw new RedisCommonsException("REDIS_LIST_LEFT_PUSH_ALL_ERROR",
                    "Failed to left push all values to list for key: " + key, e);
        }
    }

    @Override
    public Long leftPushAll(String key, Collection<T> values) {
        try {
            log.debug("Left pushing {} values to list for key: {}", values.size(), key);
            Object[] objectValues = values.toArray();
            return listOperations.leftPushAll(key, objectValues);
        } catch (Exception e) {
            log.error("Failed to left push all values to list for key: {}", key, e);
            throw new RedisCommonsException("REDIS_LIST_LEFT_PUSH_ALL_ERROR",
                    "Failed to left push all values to list for key: " + key, e);
        }
    }

    @Override
    public Long leftPushIfPresent(String key, T value) {
        try {
            log.debug("Left pushing value if present to list for key: {}", key);
            return listOperations.leftPushIfPresent(key, value);
        } catch (Exception e) {
            log.error("Failed to left push if present to list for key: {}", key, e);
            throw new RedisCommonsException("REDIS_LIST_LEFT_PUSH_IF_PRESENT_ERROR",
                    "Failed to left push if present to list for key: " + key, e);
        }
    }

    @Override
    public Long rightPush(String key, T value) {
        try {
            log.debug("Right pushing value to list for key: {}", key);
            return listOperations.rightPush(key, value);
        } catch (Exception e) {
            log.error("Failed to right push value to list for key: {}", key, e);
            throw new RedisCommonsException("REDIS_LIST_RIGHT_PUSH_ERROR",
                    "Failed to right push value to list for key: " + key, e);
        }
    }

    @Override
    @SafeVarargs
    public final Long rightPushAll(String key, T... values) {
        try {
            log.debug("Right pushing {} values to list for key: {}", values.length, key);
            Object[] objectValues = Arrays.stream(values).toArray();
            return listOperations.rightPushAll(key, objectValues);
        } catch (Exception e) {
            log.error("Failed to right push all values to list for key: {}", key, e);
            throw new RedisCommonsException("REDIS_LIST_RIGHT_PUSH_ALL_ERROR",
                    "Failed to right push all values to list for key: " + key, e);
        }
    }

    @Override
    public Long rightPushAll(String key, Collection<T> values) {
        try {
            log.debug("Right pushing {} values to list for key: {}", values.size(), key);
            Object[] objectValues = values.toArray();
            return listOperations.rightPushAll(key, objectValues);
        } catch (Exception e) {
            log.error("Failed to right push all values to list for key: {}", key, e);
            throw new RedisCommonsException("REDIS_LIST_RIGHT_PUSH_ALL_ERROR",
                    "Failed to right push all values to list for key: " + key, e);
        }
    }

    @Override
    public Long rightPushIfPresent(String key, T value) {
        try {
            log.debug("Right pushing value if present to list for key: {}", key);
            return listOperations.rightPushIfPresent(key, value);
        } catch (Exception e) {
            log.error("Failed to right push if present to list for key: {}", key, e);
            throw new RedisCommonsException("REDIS_LIST_RIGHT_PUSH_IF_PRESENT_ERROR",
                    "Failed to right push if present to list for key: " + key, e);
        }
    }

    @Override
    public T leftPop(String key) {
        try {
            log.debug("Left popping value from list for key: {}", key);
            Object value = listOperations.leftPop(key);
            return convertValue(value, valueType);
        } catch (Exception e) {
            log.error("Failed to left pop value from list for key: {}", key, e);
            throw new RedisCommonsException("REDIS_LIST_LEFT_POP_ERROR",
                    "Failed to left pop value from list for key: " + key, e);
        }
    }

    @Override
    public T leftPop(String key, long timeout, TimeUnit unit) {
        try {
            log.debug("Left popping value with timeout from list for key: {}", key);
            Object value = listOperations.leftPop(key, timeout, unit);
            return convertValue(value, valueType);
        } catch (Exception e) {
            log.error("Failed to left pop value with timeout from list for key: {}", key, e);
            throw new RedisCommonsException("REDIS_LIST_LEFT_POP_TIMEOUT_ERROR",
                    "Failed to left pop value with timeout from list for key: " + key, e);
        }
    }

    @Override
    public T rightPop(String key) {
        try {
            log.debug("Right popping value from list for key: {}", key);
            Object value = listOperations.rightPop(key);
            return convertValue(value, valueType);
        } catch (Exception e) {
            log.error("Failed to right pop value from list for key: {}", key, e);
            throw new RedisCommonsException("REDIS_LIST_RIGHT_POP_ERROR",
                    "Failed to right pop value from list for key: " + key, e);
        }
    }

    @Override
    public T rightPop(String key, long timeout, TimeUnit unit) {
        try {
            log.debug("Right popping value with timeout from list for key: {}", key);
            Object value = listOperations.rightPop(key, timeout, unit);
            return convertValue(value, valueType);
        } catch (Exception e) {
            log.error("Failed to right pop value with timeout from list for key: {}", key, e);
            throw new RedisCommonsException("REDIS_LIST_RIGHT_POP_TIMEOUT_ERROR",
                    "Failed to right pop value with timeout from list for key: " + key, e);
        }
    }

    @Override
    public List<T> range(String key, long start, long end) {
        try {
            log.debug("Getting range [{}, {}] from list for key: {}", start, end, key);
            List<Object> values = listOperations.range(key, start, end);
            if (values == null) {
                return null;
            }
            return values.stream()
                    .map(value -> convertValue(value, valueType))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get range from list for key: {}", key, e);
            throw new RedisCommonsException("REDIS_LIST_RANGE_ERROR",
                    "Failed to get range from list for key: " + key, e);
        }
    }

    @Override
    public T index(String key, long index) {
        try {
            log.debug("Getting index {} from list for key: {}", index, key);
            Object value = listOperations.index(key, index);
            return convertValue(value, valueType);
        } catch (Exception e) {
            log.error("Failed to get index from list for key: {}", key, e);
            throw new RedisCommonsException("REDIS_LIST_INDEX_ERROR",
                    "Failed to get index from list for key: " + key, e);
        }
    }

    @Override
    public void set(String key, long index, T value) {
        try {
            log.debug("Setting index {} in list for key: {}", index, key);
            listOperations.set(key, index, value);
        } catch (Exception e) {
            log.error("Failed to set index in list for key: {}", key, e);
            throw new RedisCommonsException("REDIS_LIST_SET_ERROR",
                    "Failed to set index in list for key: " + key, e);
        }
    }

    @Override
    public Long remove(String key, long count, T value) {
        try {
            log.debug("Removing {} occurrences of value from list for key: {}", count, key);
            return listOperations.remove(key, count, value);
        } catch (Exception e) {
            log.error("Failed to remove value from list for key: {}", key, e);
            throw new RedisCommonsException("REDIS_LIST_REMOVE_ERROR",
                    "Failed to remove value from list for key: " + key, e);
        }
    }

    @Override
    public void trim(String key, long start, long end) {
        try {
            log.debug("Trimming list to range [{}, {}] for key: {}", start, end, key);
            listOperations.trim(key, start, end);
        } catch (Exception e) {
            log.error("Failed to trim list for key: {}", key, e);
            throw new RedisCommonsException("REDIS_LIST_TRIM_ERROR",
                    "Failed to trim list for key: " + key, e);
        }
    }

    @Override
    public Long size(String key) {
        try {
            log.debug("Getting list size for key: {}", key);
            return listOperations.size(key);
        } catch (Exception e) {
            log.error("Failed to get list size for key: {}", key, e);
            throw new RedisCommonsException("REDIS_LIST_SIZE_ERROR",
                    "Failed to get list size for key: " + key, e);
        }
    }

    /**
     * 类型转换辅助方法
     *
     * @param value      原始值
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
        if (value instanceof Number) {
            Number number = (Number) value;
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
        if (value instanceof String) {
            String stringValue = (String) value;
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
                log.warn("Failed to convert string '{}' to {}", stringValue, targetType.getSimpleName());
            }
        }

        // 如果无法转换，尝试使用序列化器
        try {
            RedisSerializer<T> serializer = strategySelector.getSerializerWithFallback(
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
            throw new RedisCommonsException("REDIS_TYPE_CONVERSION_ERROR",
                    "Cannot convert value of type " + value.getClass().getSimpleName() +
                            " to " + targetType.getSimpleName(),
                    e);
        }
    }
}