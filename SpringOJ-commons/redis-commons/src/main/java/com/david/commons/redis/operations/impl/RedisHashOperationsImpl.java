package com.david.commons.redis.operations.impl;

import com.david.commons.redis.exception.RedisCommonsException;
import com.david.commons.redis.operations.RedisHashOperations;
import com.david.commons.redis.serialization.RedisSerializer;
import com.david.commons.redis.serialization.SerializationStrategySelector;
import com.david.commons.redis.serialization.enums.SerializationType;
import com.david.commons.redis.serialization.impl.JsonRedisSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Redis Hash 操作实现类
 *
 * <p>提供链式调用和方法级泛型的强类型读取能力
 *
 * @author David
 */
@Slf4j
public class RedisHashOperationsImpl implements RedisHashOperations {

    private final RedisTemplate<String, Object> redisTemplate;
    private final HashOperations<String, Object, Object> hashOperations;
    private final SerializationStrategySelector strategySelector;
    private final SerializationType serializationType;

    public RedisHashOperationsImpl(
            RedisTemplate<String, Object> redisTemplate,
            SerializationStrategySelector strategySelector) {
        this.redisTemplate = redisTemplate;
        this.hashOperations = redisTemplate.opsForHash();
        this.strategySelector = strategySelector;
        this.serializationType = strategySelector.getDefaultStrategy();
    }

    private RedisHashOperationsImpl(
            RedisTemplate<String, Object> redisTemplate,
            SerializationStrategySelector strategySelector,
            SerializationType serializationType) {
        this.redisTemplate = redisTemplate;
        this.hashOperations = redisTemplate.opsForHash();
        this.strategySelector = strategySelector;
        this.serializationType = serializationType;
    }

    @Override
    public RedisHashOperations using(SerializationType serializationType) {
        if (serializationType == null || serializationType == this.serializationType) {
            return this;
        }
        return new RedisHashOperationsImpl(redisTemplate, strategySelector, serializationType);
    }

    @Override
    public Boolean hSet(String key, String field, Object value) {
        try {
            log.debug("HSET - 键: {}, 字段: {}", key, field);
            hashOperations.put(key, field, value);
            return true;
        } catch (Exception e) {
            log.error("HSET 失败 - 键: {}, 字段: {}", key, field, e);
            throw new RedisCommonsException("REDIS_HASH_HSET_ERROR", "HSET 失败: " + key + ":" + field, e);
        }
    }

    @Override
    public Boolean hSetIfAbsent(String key, String field, Object value) {
        try {
            log.debug("HSETNX - 键: {}, 字段: {}", key, field);
            Boolean result = hashOperations.putIfAbsent(key, field, value);
            return result != null ? result : false;
        } catch (Exception e) {
            log.error("HSETNX 失败 - 键: {}, 字段: {}", key, field, e);
            throw new RedisCommonsException("REDIS_HASH_HSETNX_ERROR", "HSETNX 失败: " + key + ":" + field, e);
        }
    }

    @Override
    public Boolean hMSet(String key, Map<String, ?> map) {
        try {
            log.debug("HMSET - 键: {}, 字段数: {}", key, map != null ? map.size() : 0);
            if (map == null || map.isEmpty()) {
                return true;
            }
            // HashOperations 需要 Map<Object,Object>
            Map<Object, Object> raw = new HashMap<>(map);
            hashOperations.putAll(key, raw);
            return true;
        } catch (Exception e) {
            log.error("HMSET 失败 - 键: {}", key, e);
            throw new RedisCommonsException("REDIS_HASH_HMSET_ERROR", "HMSET 失败: " + key, e);
        }
    }

    @Override
    public <T> T hGet(String key, String field, Class<T> valueType) {
        try {
            log.debug("HGET - 键: {}, 字段: {}", key, field);
            Object value = hashOperations.get(key, field);
            if (value == null) {
                return null;
            }
            if (valueType.isInstance(value)) {
                return valueType.cast(value);
            }
            return convertValue(value, valueType);
        } catch (Exception e) {
            log.error("HGET 失败 - 键: {}, 字段: {}", key, field, e);
            throw new RedisCommonsException("REDIS_HASH_HGET_ERROR", "HGET 失败: " + key + ":" + field, e);
        }
    }

    @Override
    public <T> List<T> hMGet(String key, Collection<String> fields, Class<T> valueType) {
        try {
            log.debug("HMGET - 键: {}, 字段数: {}", key, fields != null ? fields.size() : 0);
            if (fields == null || fields.isEmpty()) {
                return List.of();
            }
            List<Object> raw = hashOperations.multiGet(key, new ArrayList<>(fields));
            return raw.stream()
                    .map(v -> v == null ? null : (valueType.isInstance(v) ? valueType.cast(v) : convertValue(v, valueType)))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("HMGET 失败 - 键: {}", key, e);
            throw new RedisCommonsException("REDIS_HASH_HMGET_ERROR", "HMGET 失败: " + key, e);
        }
    }

    @Override
    public Long hDel(String key, String... fields) {
        try {
            log.debug("HDEL - 键: {}, 字段: {}", key, Arrays.toString(fields));
            if (fields == null || fields.length == 0) {
                return 0L;
            }
            Long deleted = hashOperations.delete(key, (Object[]) fields);
            return deleted != null ? deleted : 0L;
        } catch (Exception e) {
            log.error("HDEL 失败 - 键: {}, 字段: {}", key, Arrays.toString(fields), e);
            throw new RedisCommonsException("REDIS_HASH_HDEL_ERROR", "HDEL 失败: " + key, e);
        }
    }

    @Override
    public Boolean hExists(String key, String field) {
        try {
            log.debug("HEXISTS - 键: {}, 字段: {}", key, field);
            Boolean exists = hashOperations.hasKey(key, field);
            return exists != null ? exists : false;
        } catch (Exception e) {
            log.error("HEXISTS 失败 - 键: {}, 字段: {}", key, field, e);
            throw new RedisCommonsException("REDIS_HASH_HEXISTS_ERROR", "HEXISTS 失败: " + key + ":" + field, e);
        }
    }

    @Override
    public <T> List<T> hVals(String key, Class<T> valueType) {
        try {
            log.debug("HVALS - 键: {}", key);
            List<Object> values = hashOperations.values(key);
            if (values == null || values.isEmpty()) {
                return List.of();
            }
            return values.stream()
                    .map(v -> v == null ? null : (valueType.isInstance(v) ? valueType.cast(v) : convertValue(v, valueType)))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("HVALS 失败 - 键: {}", key, e);
            throw new RedisCommonsException("REDIS_HASH_HVALS_ERROR", "HVALS 失败: " + key, e);
        }
    }

    @Override
    public Set<String> hKeys(String key) {
        try {
            log.debug("HKEYS - 键: {}", key);
            Set<Object> keys = hashOperations.keys(key);
            if (keys == null || keys.isEmpty()) {
                return Set.of();
            }
            return keys.stream().map(String::valueOf).collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("HKEYS 失败 - 键: {}", key, e);
            throw new RedisCommonsException("REDIS_HASH_HKEYS_ERROR", "HKEYS 失败: " + key, e);
        }
    }

    @Override
    public <T> Map<String, T> hGetAll(String key, Class<T> valueType) {
        try {
            log.debug("HGETALL - 键: {}", key);
            Map<Object, Object> entries = hashOperations.entries(key);
            if (entries == null || entries.isEmpty()) {
                return Map.of();
            }
            Map<String, T> result = new LinkedHashMap<>(entries.size());
            for (Map.Entry<Object, Object> e : entries.entrySet()) {
                String f = String.valueOf(e.getKey());
                Object v = e.getValue();
                if (v == null) {
                    result.put(f, null);
                } else if (valueType.isInstance(v)) {
                    result.put(f, valueType.cast(v));
                } else {
                    result.put(f, convertValue(v, valueType));
                }
            }
            return result;
        } catch (Exception e) {
            log.error("HGETALL 失败 - 键: {}", key, e);
            throw new RedisCommonsException("REDIS_HASH_HGETALL_ERROR", "HGETALL 失败: " + key, e);
        }
    }

    @Override
    public Long hIncrBy(String key, String field, long delta) {
        try {
            log.debug("HINCRBY - 键: {}, 字段: {}, 增量: {}", key, field, delta);
            Long val = hashOperations.increment(key, field, delta);
            return val;
        } catch (Exception e) {
            log.error("HINCRBY 失败 - 键: {}, 字段: {}", key, field, e);
            throw new RedisCommonsException("REDIS_HASH_HINCRBY_ERROR", "HINCRBY 失败: " + key + ":" + field, e);
        }
    }

    @Override
    public Double hIncrByFloat(String key, String field, double delta) {
        try {
            log.debug("HINCRBYFLOAT - 键: {}, 字段: {}, 增量: {}", key, field, delta);
            Double val = hashOperations.increment(key, field, delta);
            return val;
        } catch (Exception e) {
            log.error("HINCRBYFLOAT 失败 - 键: {}, 字段: {}", key, field, e);
            throw new RedisCommonsException("REDIS_HASH_HINCRBYFLOAT_ERROR", "HINCRBYFLOAT 失败: " + key + ":" + field, e);
        }
    }

    @Override
    public Long hLen(String key) {
        try {
            log.debug("HLEN - 键: {}", key);
            Long size = hashOperations.size(key);
            return size != null ? size : 0L;
        } catch (Exception e) {
            log.error("HLEN 失败 - 键: {}", key, e);
            throw new RedisCommonsException("REDIS_HASH_HLEN_ERROR", "HLEN 失败: " + key, e);
        }
    }

    /** 类型转换辅助方法（与字符串操作实现保持一致） */
    private <T> T convertValue(Object value, Class<T> targetType) {
        if (value == null) {
            return null;
        }
        if (targetType.isInstance(value)) {
            return targetType.cast(value);
        }
        if (targetType == String.class) {
            return targetType.cast(value.toString());
        }
        if (value instanceof Number number) {
            Object result = convertNumberValue(number, targetType);
            if (result != null) {
                return targetType.cast(result);
            }
        }
        if (value instanceof String stringValue) {
            Object result = convertStringValue(stringValue, targetType);
            if (result != null) {
                return targetType.cast(result);
            }
        }
        if (value instanceof java.util.Map || value instanceof java.util.Collection) {
            T result = convertCollectionValue(value, targetType);
            if (result != null) {
                return result;
            }
        }
        T result = convertUsingSerializer(value, targetType);
        if (result != null) {
            return result;
        }
        throw new RedisCommonsException(
                "REDIS_TYPE_CONVERSION_ERROR",
                "无法将类型 " + value.getClass().getSimpleName() + " 转换为 " + targetType.getSimpleName());
    }

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
