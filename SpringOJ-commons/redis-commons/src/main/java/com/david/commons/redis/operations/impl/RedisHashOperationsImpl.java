package com.david.commons.redis.operations.impl;

import com.david.commons.redis.exception.RedisCommonsException;
import com.david.commons.redis.operations.RedisHashOperations;
import com.david.commons.redis.serialization.RedisSerializer;
import com.david.commons.redis.serialization.SerializationStrategySelector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Redis 哈希操作实现类
 * 提供强类型支持和批量操作能力
 *
 * @param <T> 值类型
 * @author David
 */
@Slf4j
public class RedisHashOperationsImpl<T> implements RedisHashOperations<T> {

    private final HashOperations<String, Object, Object> hashOperations;
    private final SerializationStrategySelector strategySelector;
    private final Class<T> valueType;

    public RedisHashOperationsImpl(RedisTemplate<String, Object> redisTemplate,
            SerializationStrategySelector strategySelector,
            Class<T> valueType) {
        this.hashOperations = redisTemplate.opsForHash();
        this.strategySelector = strategySelector;
        this.valueType = valueType;
    }

    @Override
    public void put(String key, String hashKey, T value) {
        try {
            log.debug("Setting hash field for key: {}, hashKey: {}", key, hashKey);
            hashOperations.put(key, (Object) hashKey, value);
        } catch (Exception e) {
            log.error("Failed to set hash field for key: {}, hashKey: {}", key, hashKey, e);
            throw new RedisCommonsException("REDIS_HASH_PUT_ERROR",
                    "Failed to set hash field for key: " + key + ", hashKey: " + hashKey, e);
        }
    }

    @Override
    public void putAll(String key, Map<String, T> map) {
        try {
            log.debug("Setting multiple hash fields for key: {}, count: {}", key, map.size());
            // Convert Map<String, T> to Map<String, Object> for Redis operations
            Map<String, Object> objectMap = map.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> (Object) entry.getValue()));
            hashOperations.putAll(key, objectMap);
        } catch (Exception e) {
            log.error("Failed to set multiple hash fields for key: {}", key, e);
            throw new RedisCommonsException("REDIS_HASH_PUT_ALL_ERROR",
                    "Failed to set multiple hash fields for key: " + key, e);
        }
    }

    @Override
    public Boolean putIfAbsent(String key, String hashKey, T value) {
        try {
            log.debug("Setting hash field if absent for key: {}, hashKey: {}", key, hashKey);
            Boolean result = hashOperations.putIfAbsent(key, (Object) hashKey, value);
            return result != null ? result : false;
        } catch (Exception e) {
            log.error("Failed to set hash field if absent for key: {}, hashKey: {}", key, hashKey, e);
            throw new RedisCommonsException("REDIS_HASH_PUT_IF_ABSENT_ERROR",
                    "Failed to set hash field if absent for key: " + key + ", hashKey: " + hashKey, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(String key, String hashKey) {
        try {
            log.debug("Getting hash field for key: {}, hashKey: {}", key, hashKey);
            Object value = hashOperations.get(key, (Object) hashKey);
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
            log.error("Failed to get hash field for key: {}, hashKey: {}", key, hashKey, e);
            throw new RedisCommonsException("REDIS_HASH_GET_ERROR",
                    "Failed to get hash field for key: " + key + ", hashKey: " + hashKey, e);
        }
    }

    @Override
    public List<T> multiGet(String key, Collection<String> hashKeys) {
        try {
            log.debug("Getting multiple hash fields for key: {}, count: {}", key, hashKeys.size());
            // Convert Collection<String> to Collection<Object> for Redis operations
            Collection<Object> objectHashKeys = hashKeys.stream()
                    .map(hashKey -> (Object) hashKey)
                    .collect(Collectors.toList());
            List<Object> values = hashOperations.multiGet(key, objectHashKeys);
            return values.stream()
                    .map(value -> value == null ? null : convertValue(value, valueType))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get multiple hash fields for key: {}", key, e);
            throw new RedisCommonsException("REDIS_HASH_MULTI_GET_ERROR",
                    "Failed to get multiple hash fields for key: " + key, e);
        }
    }

    @Override
    public Map<String, T> entries(String key) {
        try {
            log.debug("Getting all hash entries for key: {}", key);
            Map<Object, Object> entries = hashOperations.entries(key);
            return entries.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> entry.getKey().toString(),
                            entry -> convertValue(entry.getValue(), valueType)));
        } catch (Exception e) {
            log.error("Failed to get all hash entries for key: {}", key, e);
            throw new RedisCommonsException("REDIS_HASH_ENTRIES_ERROR",
                    "Failed to get all hash entries for key: " + key, e);
        }
    }

    @Override
    public Set<String> keys(String key) {
        try {
            log.debug("Getting all hash keys for key: {}", key);
            Set<Object> objectKeys = hashOperations.keys(key);
            return objectKeys.stream()
                    .map(Object::toString)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Failed to get all hash keys for key: {}", key, e);
            throw new RedisCommonsException("REDIS_HASH_KEYS_ERROR",
                    "Failed to get all hash keys for key: " + key, e);
        }
    }

    @Override
    public List<T> values(String key) {
        try {
            log.debug("Getting all hash values for key: {}", key);
            List<Object> values = hashOperations.values(key);
            return values.stream()
                    .map(value -> convertValue(value, valueType))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get all hash values for key: {}", key, e);
            throw new RedisCommonsException("REDIS_HASH_VALUES_ERROR",
                    "Failed to get all hash values for key: " + key, e);
        }
    }

    @Override
    public Long delete(String key, String... hashKeys) {
        try {
            log.debug("Deleting hash fields for key: {}, count: {}", key, hashKeys.length);
            return hashOperations.delete(key, (Object[]) hashKeys);
        } catch (Exception e) {
            log.error("Failed to delete hash fields for key: {}", key, e);
            throw new RedisCommonsException("REDIS_HASH_DELETE_ERROR",
                    "Failed to delete hash fields for key: " + key, e);
        }
    }

    @Override
    public Boolean hasKey(String key, String hashKey) {
        try {
            log.debug("Checking if hash field exists for key: {}, hashKey: {}", key, hashKey);
            return hashOperations.hasKey(key, (Object) hashKey);
        } catch (Exception e) {
            log.error("Failed to check hash field existence for key: {}, hashKey: {}", key, hashKey, e);
            throw new RedisCommonsException("REDIS_HASH_HAS_KEY_ERROR",
                    "Failed to check hash field existence for key: " + key + ", hashKey: " + hashKey, e);
        }
    }

    @Override
    public Long size(String key) {
        try {
            log.debug("Getting hash size for key: {}", key);
            return hashOperations.size(key);
        } catch (Exception e) {
            log.error("Failed to get hash size for key: {}", key, e);
            throw new RedisCommonsException("REDIS_HASH_SIZE_ERROR",
                    "Failed to get hash size for key: " + key, e);
        }
    }

    @Override
    public Long increment(String key, String hashKey, long delta) {
        try {
            log.debug("Incrementing hash field for key: {}, hashKey: {}, delta: {}", key, hashKey, delta);
            return hashOperations.increment(key, (Object) hashKey, delta);
        } catch (Exception e) {
            log.error("Failed to increment hash field for key: {}, hashKey: {}", key, hashKey, e);
            throw new RedisCommonsException("REDIS_HASH_INCREMENT_ERROR",
                    "Failed to increment hash field for key: " + key + ", hashKey: " + hashKey, e);
        }
    }

    @Override
    public Double increment(String key, String hashKey, double delta) {
        try {
            log.debug("Incrementing hash field (double) for key: {}, hashKey: {}, delta: {}", key, hashKey, delta);
            return hashOperations.increment(key, (Object) hashKey, delta);
        } catch (Exception e) {
            log.error("Failed to increment hash field (double) for key: {}, hashKey: {}", key, hashKey, e);
            throw new RedisCommonsException("REDIS_HASH_INCREMENT_ERROR",
                    "Failed to increment hash field (double) for key: " + key + ", hashKey: " + hashKey, e);
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