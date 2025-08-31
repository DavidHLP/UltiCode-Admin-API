package com.david.commons.redis.operations.impl;

import com.david.commons.redis.exception.RedisCommonsException;
import com.david.commons.redis.operations.RedisZSetOperations;
import com.david.commons.redis.serialization.SerializationStrategySelector;
import com.david.commons.redis.serialization.RedisSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Redis 有序集合操作实现类
 * 提供强类型支持和完整的有序集合操作能力
 *
 * @param <T> 元素类型
 * @author David
 */
@Slf4j
public class RedisZSetOperationsImpl<T> implements RedisZSetOperations<T> {

    private final ZSetOperations<String, Object> zSetOperations;
    private final SerializationStrategySelector strategySelector;
    private final Class<T> valueType;

    public RedisZSetOperationsImpl(RedisTemplate<String, Object> redisTemplate,
            SerializationStrategySelector strategySelector,
            Class<T> valueType) {
        this.zSetOperations = redisTemplate.opsForZSet();
        this.strategySelector = strategySelector;
        this.valueType = valueType;
    }

    @Override
    public Boolean add(String key, T value, double score) {
        try {
            log.debug("Adding value with score {} to zset for key: {}", score, key);
            return zSetOperations.add(key, value, score);
        } catch (Exception e) {
            log.error("Failed to add value to zset for key: {}", key, e);
            throw new RedisCommonsException("REDIS_ZSET_ADD_ERROR",
                    "Failed to add value to zset for key: " + key, e);
        }
    }

    @Override
    public Long add(String key, Set<ZSetTuple<T>> tuples) {
        try {
            log.debug("Adding {} tuples to zset for key: {}", tuples.size(), key);
            Set<ZSetOperations.TypedTuple<Object>> typedTuples = tuples.stream()
                    .map(tuple -> ZSetOperations.TypedTuple.of((Object) tuple.getValue(), tuple.getScore()))
                    .collect(Collectors.toSet());
            return zSetOperations.add(key, typedTuples);
        } catch (Exception e) {
            log.error("Failed to add tuples to zset for key: {}", key, e);
            throw new RedisCommonsException("REDIS_ZSET_ADD_TUPLES_ERROR",
                    "Failed to add tuples to zset for key: " + key, e);
        }
    }

    @Override
    @SafeVarargs
    public final Long remove(String key, T... values) {
        try {
            log.debug("Removing {} values from zset for key: {}", values.length, key);
            Object[] objectValues = Arrays.stream(values).toArray();
            return zSetOperations.remove(key, objectValues);
        } catch (Exception e) {
            log.error("Failed to remove values from zset for key: {}", key, e);
            throw new RedisCommonsException("REDIS_ZSET_REMOVE_ERROR",
                    "Failed to remove values from zset for key: " + key, e);
        }
    }

    @Override
    public Long removeRangeByScore(String key, double min, double max) {
        try {
            log.debug("Removing values by score range [{}, {}] from zset for key: {}", min, max, key);
            return zSetOperations.removeRangeByScore(key, min, max);
        } catch (Exception e) {
            log.error("Failed to remove values by score range from zset for key: {}", key, e);
            throw new RedisCommonsException("REDIS_ZSET_REMOVE_RANGE_BY_SCORE_ERROR",
                    "Failed to remove values by score range from zset for key: " + key, e);
        }
    }

    @Override
    public Long removeRange(String key, long start, long end) {
        try {
            log.debug("Removing values by rank range [{}, {}] from zset for key: {}", start, end, key);
            return zSetOperations.removeRange(key, start, end);
        } catch (Exception e) {
            log.error("Failed to remove values by rank range from zset for key: {}", key, e);
            throw new RedisCommonsException("REDIS_ZSET_REMOVE_RANGE_ERROR",
                    "Failed to remove values by rank range from zset for key: " + key, e);
        }
    }

    @Override
    public Double incrementScore(String key, T value, double delta) {
        try {
            log.debug("Incrementing score by {} for value in zset for key: {}", delta, key);
            return zSetOperations.incrementScore(key, value, delta);
        } catch (Exception e) {
            log.error("Failed to increment score in zset for key: {}", key, e);
            throw new RedisCommonsException("REDIS_ZSET_INCREMENT_SCORE_ERROR",
                    "Failed to increment score in zset for key: " + key, e);
        }
    }

    @Override
    public Double score(String key, T value) {
        try {
            log.debug("Getting score for value in zset for key: {}", key);
            return zSetOperations.score(key, value);
        } catch (Exception e) {
            log.error("Failed to get score from zset for key: {}", key, e);
            throw new RedisCommonsException("REDIS_ZSET_SCORE_ERROR",
                    "Failed to get score from zset for key: " + key, e);
        }
    }

    @Override
    public Long rank(String key, T value) {
        try {
            log.debug("Getting rank for value in zset for key: {}", key);
            return zSetOperations.rank(key, value);
        } catch (Exception e) {
            log.error("Failed to get rank from zset for key: {}", key, e);
            throw new RedisCommonsException("REDIS_ZSET_RANK_ERROR",
                    "Failed to get rank from zset for key: " + key, e);
        }
    }

    @Override
    public Long reverseRank(String key, T value) {
        try {
            log.debug("Getting reverse rank for value in zset for key: {}", key);
            return zSetOperations.reverseRank(key, value);
        } catch (Exception e) {
            log.error("Failed to get reverse rank from zset for key: {}", key, e);
            throw new RedisCommonsException("REDIS_ZSET_REVERSE_RANK_ERROR",
                    "Failed to get reverse rank from zset for key: " + key, e);
        }
    }

    @Override
    public Set<T> range(String key, long start, long end) {
        try {
            log.debug("Getting range [{}, {}] from zset for key: {}", start, end, key);
            Set<Object> values = zSetOperations.range(key, start, end);
            if (values == null) {
                return null;
            }
            return values.stream()
                    .map(value -> convertValue(value, valueType))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Failed to get range from zset for key: {}", key, e);
            throw new RedisCommonsException("REDIS_ZSET_RANGE_ERROR",
                    "Failed to get range from zset for key: " + key, e);
        }
    }

    @Override
    public Set<ZSetTuple<T>> rangeWithScores(String key, long start, long end) {
        try {
            log.debug("Getting range with scores [{}, {}] from zset for key: {}", start, end, key);
            Set<ZSetOperations.TypedTuple<Object>> tuples = zSetOperations.rangeWithScores(key, start, end);
            if (tuples == null) {
                return null;
            }
            return tuples.stream()
                    .map(tuple -> new ZSetTupleImpl<>(convertValue(tuple.getValue(), valueType), tuple.getScore()))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Failed to get range with scores from zset for key: {}", key, e);
            throw new RedisCommonsException("REDIS_ZSET_RANGE_WITH_SCORES_ERROR",
                    "Failed to get range with scores from zset for key: " + key, e);
        }
    }

    @Override
    public Set<T> reverseRange(String key, long start, long end) {
        try {
            log.debug("Getting reverse range [{}, {}] from zset for key: {}", start, end, key);
            Set<Object> values = zSetOperations.reverseRange(key, start, end);
            if (values == null) {
                return null;
            }
            return values.stream()
                    .map(value -> convertValue(value, valueType))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Failed to get reverse range from zset for key: {}", key, e);
            throw new RedisCommonsException("REDIS_ZSET_REVERSE_RANGE_ERROR",
                    "Failed to get reverse range from zset for key: " + key, e);
        }
    }

    @Override
    public Set<ZSetTuple<T>> reverseRangeWithScores(String key, long start, long end) {
        try {
            log.debug("Getting reverse range with scores [{}, {}] from zset for key: {}", start, end, key);
            Set<ZSetOperations.TypedTuple<Object>> tuples = zSetOperations.reverseRangeWithScores(key, start, end);
            if (tuples == null) {
                return null;
            }
            return tuples.stream()
                    .map(tuple -> new ZSetTupleImpl<>(convertValue(tuple.getValue(), valueType), tuple.getScore()))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Failed to get reverse range with scores from zset for key: {}", key, e);
            throw new RedisCommonsException("REDIS_ZSET_REVERSE_RANGE_WITH_SCORES_ERROR",
                    "Failed to get reverse range with scores from zset for key: " + key, e);
        }
    }

    @Override
    public Set<T> rangeByScore(String key, double min, double max) {
        try {
            log.debug("Getting range by score [{}, {}] from zset for key: {}", min, max, key);
            Set<Object> values = zSetOperations.rangeByScore(key, min, max);
            if (values == null) {
                return null;
            }
            return values.stream()
                    .map(value -> convertValue(value, valueType))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Failed to get range by score from zset for key: {}", key, e);
            throw new RedisCommonsException("REDIS_ZSET_RANGE_BY_SCORE_ERROR",
                    "Failed to get range by score from zset for key: " + key, e);
        }
    }

    @Override
    public Set<ZSetTuple<T>> rangeByScoreWithScores(String key, double min, double max) {
        try {
            log.debug("Getting range by score with scores [{}, {}] from zset for key: {}", min, max, key);
            Set<ZSetOperations.TypedTuple<Object>> tuples = zSetOperations.rangeByScoreWithScores(key, min, max);
            if (tuples == null) {
                return null;
            }
            return tuples.stream()
                    .map(tuple -> new ZSetTupleImpl<>(convertValue(tuple.getValue(), valueType), tuple.getScore()))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Failed to get range by score with scores from zset for key: {}", key, e);
            throw new RedisCommonsException("REDIS_ZSET_RANGE_BY_SCORE_WITH_SCORES_ERROR",
                    "Failed to get range by score with scores from zset for key: " + key, e);
        }
    }

    @Override
    public Set<T> rangeByScore(String key, double min, double max, long offset, long count) {
        try {
            log.debug("Getting range by score [{}, {}] with limit [{}, {}] from zset for key: {}",
                    min, max, offset, count, key);
            Set<Object> values = zSetOperations.rangeByScore(key, min, max, offset, count);
            if (values == null) {
                return null;
            }
            return values.stream()
                    .map(value -> convertValue(value, valueType))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Failed to get range by score with limit from zset for key: {}", key, e);
            throw new RedisCommonsException("REDIS_ZSET_RANGE_BY_SCORE_LIMIT_ERROR",
                    "Failed to get range by score with limit from zset for key: " + key, e);
        }
    }

    @Override
    public Set<ZSetTuple<T>> rangeByScoreWithScores(String key, double min, double max, long offset, long count) {
        try {
            log.debug("Getting range by score with scores [{}, {}] with limit [{}, {}] from zset for key: {}",
                    min, max, offset, count, key);
            Set<ZSetOperations.TypedTuple<Object>> tuples = zSetOperations.rangeByScoreWithScores(key, min, max, offset,
                    count);
            if (tuples == null) {
                return null;
            }
            return tuples.stream()
                    .map(tuple -> new ZSetTupleImpl<>(convertValue(tuple.getValue(), valueType), tuple.getScore()))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Failed to get range by score with scores and limit from zset for key: {}", key, e);
            throw new RedisCommonsException("REDIS_ZSET_RANGE_BY_SCORE_WITH_SCORES_LIMIT_ERROR",
                    "Failed to get range by score with scores and limit from zset for key: " + key, e);
        }
    }

    @Override
    public Long count(String key, double min, double max) {
        try {
            log.debug("Counting elements by score range [{}, {}] in zset for key: {}", min, max, key);
            return zSetOperations.count(key, min, max);
        } catch (Exception e) {
            log.error("Failed to count elements by score range in zset for key: {}", key, e);
            throw new RedisCommonsException("REDIS_ZSET_COUNT_ERROR",
                    "Failed to count elements by score range in zset for key: " + key, e);
        }
    }

    @Override
    public Long size(String key) {
        try {
            log.debug("Getting zset size for key: {}", key);
            return zSetOperations.size(key);
        } catch (Exception e) {
            log.error("Failed to get zset size for key: {}", key, e);
            throw new RedisCommonsException("REDIS_ZSET_SIZE_ERROR",
                    "Failed to get zset size for key: " + key, e);
        }
    }

    @Override
    public Long intersectAndStore(String key, String destKey, String... otherKeys) {
        try {
            log.debug("Intersecting zsets for key: {} with {} other keys and storing in {}",
                    key, otherKeys.length, destKey);
            return zSetOperations.intersectAndStore(key, Arrays.asList(otherKeys), destKey);
        } catch (Exception e) {
            log.error("Failed to intersect and store zsets for key: {}", key, e);
            throw new RedisCommonsException("REDIS_ZSET_INTERSECT_AND_STORE_ERROR",
                    "Failed to intersect and store zsets for key: " + key, e);
        }
    }

    @Override
    public Long intersectAndStore(String key, String destKey, Collection<String> otherKeys) {
        try {
            log.debug("Intersecting zsets for key: {} with {} other keys and storing in {}",
                    key, otherKeys.size(), destKey);
            return zSetOperations.intersectAndStore(key, otherKeys, destKey);
        } catch (Exception e) {
            log.error("Failed to intersect and store zsets for key: {}", key, e);
            throw new RedisCommonsException("REDIS_ZSET_INTERSECT_AND_STORE_ERROR",
                    "Failed to intersect and store zsets for key: " + key, e);
        }
    }

    @Override
    public Long unionAndStore(String key, String destKey, String... otherKeys) {
        try {
            log.debug("Unioning zsets for key: {} with {} other keys and storing in {}",
                    key, otherKeys.length, destKey);
            return zSetOperations.unionAndStore(key, Arrays.asList(otherKeys), destKey);
        } catch (Exception e) {
            log.error("Failed to union and store zsets for key: {}", key, e);
            throw new RedisCommonsException("REDIS_ZSET_UNION_AND_STORE_ERROR",
                    "Failed to union and store zsets for key: " + key, e);
        }
    }

    @Override
    public Long unionAndStore(String key, String destKey, Collection<String> otherKeys) {
        try {
            log.debug("Unioning zsets for key: {} with {} other keys and storing in {}",
                    key, otherKeys.size(), destKey);
            return zSetOperations.unionAndStore(key, otherKeys, destKey);
        } catch (Exception e) {
            log.error("Failed to union and store zsets for key: {}", key, e);
            throw new RedisCommonsException("REDIS_ZSET_UNION_AND_STORE_ERROR",
                    "Failed to union and store zsets for key: " + key, e);
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