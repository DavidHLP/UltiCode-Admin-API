package com.david.commons.redis.operations.impl;

import com.david.commons.redis.exception.RedisCommonsException;
import com.david.commons.redis.operations.RedisSetOperations;
import com.david.commons.redis.serialization.SerializationStrategySelector;
import com.david.commons.redis.serialization.RedisSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Redis 集合操作实现类
 * 提供强类型支持和完整的集合操作能力
 *
 * @param <T> 元素类型
 * @author David
 */
@Slf4j
public class RedisSetOperationsImpl<T> implements RedisSetOperations<T> {

    private final SetOperations<String, Object> setOperations;
    private final SerializationStrategySelector strategySelector;
    private final Class<T> valueType;

    public RedisSetOperationsImpl(RedisTemplate<String, Object> redisTemplate,
            SerializationStrategySelector strategySelector,
            Class<T> valueType) {
        this.setOperations = redisTemplate.opsForSet();
        this.strategySelector = strategySelector;
        this.valueType = valueType;
    }

    @Override
    @SafeVarargs
    public final Long add(String key, T... values) {
        try {
            log.debug("Adding {} values to set for key: {}", values.length, key);
            Object[] objectValues = Arrays.stream(values).toArray();
            return setOperations.add(key, objectValues);
        } catch (Exception e) {
            log.error("Failed to add values to set for key: {}", key, e);
            throw new RedisCommonsException("REDIS_SET_ADD_ERROR",
                    "Failed to add values to set for key: " + key, e);
        }
    }

    @Override
    @SafeVarargs
    public final Long remove(String key, T... values) {
        try {
            log.debug("Removing {} values from set for key: {}", values.length, key);
            Object[] objectValues = Arrays.stream(values).toArray();
            return setOperations.remove(key, objectValues);
        } catch (Exception e) {
            log.error("Failed to remove values from set for key: {}", key, e);
            throw new RedisCommonsException("REDIS_SET_REMOVE_ERROR",
                    "Failed to remove values from set for key: " + key, e);
        }
    }

    @Override
    public T pop(String key) {
        try {
            log.debug("Popping value from set for key: {}", key);
            Object value = setOperations.pop(key);
            return convertValue(value, valueType);
        } catch (Exception e) {
            log.error("Failed to pop value from set for key: {}", key, e);
            throw new RedisCommonsException("REDIS_SET_POP_ERROR",
                    "Failed to pop value from set for key: " + key, e);
        }
    }

    @Override
    public List<T> pop(String key, long count) {
        try {
            log.debug("Popping {} values from set for key: {}", count, key);
            List<Object> values = setOperations.pop(key, count);
            if (values == null) {
                return null;
            }
            return values.stream()
                    .map(value -> convertValue(value, valueType))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to pop values from set for key: {}", key, e);
            throw new RedisCommonsException("REDIS_SET_POP_MULTIPLE_ERROR",
                    "Failed to pop values from set for key: " + key, e);
        }
    }

    @Override
    public Boolean move(String key, T value, String destKey) {
        try {
            log.debug("Moving value from set {} to set {}", key, destKey);
            return setOperations.move(key, value, destKey);
        } catch (Exception e) {
            log.error("Failed to move value from set {} to set {}", key, destKey, e);
            throw new RedisCommonsException("REDIS_SET_MOVE_ERROR",
                    "Failed to move value from set " + key + " to set " + destKey, e);
        }
    }

    @Override
    public Long size(String key) {
        try {
            log.debug("Getting set size for key: {}", key);
            return setOperations.size(key);
        } catch (Exception e) {
            log.error("Failed to get set size for key: {}", key, e);
            throw new RedisCommonsException("REDIS_SET_SIZE_ERROR",
                    "Failed to get set size for key: " + key, e);
        }
    }

    @Override
    public Boolean isMember(String key, T value) {
        try {
            log.debug("Checking if value is member of set for key: {}", key);
            return setOperations.isMember(key, value);
        } catch (Exception e) {
            log.error("Failed to check set membership for key: {}", key, e);
            throw new RedisCommonsException("REDIS_SET_IS_MEMBER_ERROR",
                    "Failed to check set membership for key: " + key, e);
        }
    }

    @Override
    public Map<Object, Boolean> isMember(String key, Object... values) {
        try {
            log.debug("Checking if {} values are members of set for key: {}", values.length, key);
            return setOperations.isMember(key, values);
        } catch (Exception e) {
            log.error("Failed to check set membership for multiple values for key: {}", key, e);
            throw new RedisCommonsException("REDIS_SET_IS_MEMBER_MULTIPLE_ERROR",
                    "Failed to check set membership for multiple values for key: " + key, e);
        }
    }

    @Override
    public Set<T> members(String key) {
        try {
            log.debug("Getting all members of set for key: {}", key);
            Set<Object> values = setOperations.members(key);
            if (values == null) {
                return null;
            }
            return values.stream()
                    .map(value -> convertValue(value, valueType))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Failed to get set members for key: {}", key, e);
            throw new RedisCommonsException("REDIS_SET_MEMBERS_ERROR",
                    "Failed to get set members for key: " + key, e);
        }
    }

    @Override
    public T randomMember(String key) {
        try {
            log.debug("Getting random member from set for key: {}", key);
            Object value = setOperations.randomMember(key);
            return convertValue(value, valueType);
        } catch (Exception e) {
            log.error("Failed to get random member from set for key: {}", key, e);
            throw new RedisCommonsException("REDIS_SET_RANDOM_MEMBER_ERROR",
                    "Failed to get random member from set for key: " + key, e);
        }
    }

    @Override
    public List<T> randomMembers(String key, long count) {
        try {
            log.debug("Getting {} random members from set for key: {}", count, key);
            List<Object> values = setOperations.randomMembers(key, count);
            if (values == null) {
                return null;
            }
            return values.stream()
                    .map(value -> convertValue(value, valueType))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get random members from set for key: {}", key, e);
            throw new RedisCommonsException("REDIS_SET_RANDOM_MEMBERS_ERROR",
                    "Failed to get random members from set for key: " + key, e);
        }
    }

    @Override
    public Set<T> distinctRandomMembers(String key, long count) {
        try {
            log.debug("Getting {} distinct random members from set for key: {}", count, key);
            Set<Object> values = setOperations.distinctRandomMembers(key, count);
            if (values == null) {
                return null;
            }
            return values.stream()
                    .map(value -> convertValue(value, valueType))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Failed to get distinct random members from set for key: {}", key, e);
            throw new RedisCommonsException("REDIS_SET_DISTINCT_RANDOM_MEMBERS_ERROR",
                    "Failed to get distinct random members from set for key: " + key, e);
        }
    }

    @Override
    public Set<T> intersect(String key, String... otherKeys) {
        try {
            log.debug("Getting intersection of sets for key: {} with {} other keys", key, otherKeys.length);
            Set<Object> values = setOperations.intersect(key, Arrays.asList(otherKeys));
            if (values == null) {
                return null;
            }
            return values.stream()
                    .map(value -> convertValue(value, valueType))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Failed to get set intersection for key: {}", key, e);
            throw new RedisCommonsException("REDIS_SET_INTERSECT_ERROR",
                    "Failed to get set intersection for key: " + key, e);
        }
    }

    @Override
    public Set<T> intersect(String key, Collection<String> otherKeys) {
        try {
            log.debug("Getting intersection of sets for key: {} with {} other keys", key, otherKeys.size());
            Set<Object> values = setOperations.intersect(key, otherKeys);
            if (values == null) {
                return null;
            }
            return values.stream()
                    .map(value -> convertValue(value, valueType))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Failed to get set intersection for key: {}", key, e);
            throw new RedisCommonsException("REDIS_SET_INTERSECT_ERROR",
                    "Failed to get set intersection for key: " + key, e);
        }
    }

    @Override
    public Set<T> union(String key, String... otherKeys) {
        try {
            log.debug("Getting union of sets for key: {} with {} other keys", key, otherKeys.length);
            Set<Object> values = setOperations.union(key, Arrays.asList(otherKeys));
            if (values == null) {
                return null;
            }
            return values.stream()
                    .map(value -> convertValue(value, valueType))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Failed to get set union for key: {}", key, e);
            throw new RedisCommonsException("REDIS_SET_UNION_ERROR",
                    "Failed to get set union for key: " + key, e);
        }
    }

    @Override
    public Set<T> union(String key, Collection<String> otherKeys) {
        try {
            log.debug("Getting union of sets for key: {} with {} other keys", key, otherKeys.size());
            Set<Object> values = setOperations.union(key, otherKeys);
            if (values == null) {
                return null;
            }
            return values.stream()
                    .map(value -> convertValue(value, valueType))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Failed to get set union for key: {}", key, e);
            throw new RedisCommonsException("REDIS_SET_UNION_ERROR",
                    "Failed to get set union for key: " + key, e);
        }
    }

    @Override
    public Set<T> difference(String key, String... otherKeys) {
        try {
            log.debug("Getting difference of sets for key: {} with {} other keys", key, otherKeys.length);
            Set<Object> values = setOperations.difference(key, Arrays.asList(otherKeys));
            if (values == null) {
                return null;
            }
            return values.stream()
                    .map(value -> convertValue(value, valueType))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Failed to get set difference for key: {}", key, e);
            throw new RedisCommonsException("REDIS_SET_DIFFERENCE_ERROR",
                    "Failed to get set difference for key: " + key, e);
        }
    }

    @Override
    public Set<T> difference(String key, Collection<String> otherKeys) {
        try {
            log.debug("Getting difference of sets for key: {} with {} other keys", key, otherKeys.size());
            Set<Object> values = setOperations.difference(key, otherKeys);
            if (values == null) {
                return null;
            }
            return values.stream()
                    .map(value -> convertValue(value, valueType))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Failed to get set difference for key: {}", key, e);
            throw new RedisCommonsException("REDIS_SET_DIFFERENCE_ERROR",
                    "Failed to get set difference for key: " + key, e);
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