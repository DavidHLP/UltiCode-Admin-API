package com.david.redis.commons.core;

import com.david.redis.commons.exception.RedisOperationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Redis核心工具类
 *
 * <p>
 * 提供统一的Redis操作接口，支持String、Hash、List、Set、ZSet等数据类型操作，
 * 以及事务支持。所有操作都包含完善的异常处理和日志记录。
 *
 * @author David
 */
@Slf4j
@Component
public class RedisUtils {

    /**
     * -- GETTER --
     * 获取RedisTemplate实例（用于高级操作）
     *
     * @return RedisTemplate实例
     */
    @Getter
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private RedisTransactionManager transactionManager;

    /**
     * 构造函数
     *
     * @param redisTemplate Redis模板
     */
    public RedisUtils(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 设置事务管理器（用于Spring注入）
     *
     * @param transactionManager 事务管理器
     */
    public void setTransactionManager(RedisTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    // ========== String Operations ==========

    /**
     * 设置键值对
     *
     * @param key   键
     * @param value 值
     */
    public void set(String key, Object value) {
        try {
            log.debug("Setting key: {}, value: {}", key, value);
            recordOperation("SET " + key);
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("Failed to set key: {}", key, e);
            throw new RedisOperationException("设置键值失败", e, "SET", key, value);
        }
    }

    /**
     * 设置键值对并指定过期时间
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     */
    public void set(String key, Object value, Duration timeout) {
        try {
            log.debug("Setting key: {}, value: {}, timeout: {}", key, value, timeout);
            recordOperation("SETEX " + key + " " + timeout.getSeconds());
            redisTemplate.opsForValue().set(key, value, timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("Failed to set key with timeout: {}", key, e);
            throw new RedisOperationException("设置键值和过期时间失败", e, "SETEX", key, value, timeout);
        }
    }

    /**
     * 获取指定键的值
     *
     * @param key   键
     * @param clazz 返回值类型
     * @param <T>   泛型类型
     * @return 值，如果键不存在则返回null
     */
    public <T> T get(String key, Class<T> clazz) {
        try {
            log.debug("Getting key: {}, expected type: {}", key, clazz.getSimpleName());
            Object value = redisTemplate.opsForValue().get(key);

            if (value == null) {
                log.debug("Key not found: {}", key);
                return null;
            }

            return convertValue(value, clazz);
        } catch (Exception e) {
            log.error("Failed to get key: {}", key, e);
            throw new RedisOperationException("获取键值失败", e, "GET", key);
        }
    }

    /**
     * 获取字符串值（便捷方法）
     *
     * @param key 键
     * @return 字符串值
     */
    public String getString(String key) {
        return get(key, String.class);
    }

    /**
     * 删除指定键
     *
     * @param key 键
     * @return 是否删除成功
     */
    public Boolean delete(String key) {
        try {
            log.debug("Deleting key: {}", key);
            Boolean result = redisTemplate.delete(key);
            log.debug("Delete result for key {}: {}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to delete key: {}", key, e);
            throw new RedisOperationException("删除键失败", e, "DEL", key);
        }
    }

    /**
     * 批量删除指定键
     *
     * @param keys 键数组
     * @return 成功删除的键数量
     */
    public Long delete(String... keys) {
        try {
            log.debug("Deleting keys: {}", Arrays.toString(keys));
            Long result = redisTemplate.delete(Arrays.asList(keys));
            log.debug("Delete result for keys {}: {}", Arrays.toString(keys), result);
            return result;
        } catch (Exception e) {
            log.error("Failed to delete keys: {}", Arrays.toString(keys), e);
            throw new RedisOperationException("批量删除键失败", e, "DEL", Arrays.toString(keys));
        }
    }

    /**
     * 设置键的过期时间
     *
     * @param key     键
     * @param timeout 过期时间
     * @return 是否设置成功
     */
    public Boolean expire(String key, Duration timeout) {
        try {
            log.debug("Setting expiration for key: {}, timeout: {}", key, timeout);
            Boolean result = redisTemplate.expire(key, timeout.toMillis(), TimeUnit.MILLISECONDS);
            log.debug("Expire result for key {}: {}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to set expiration for key: {}", key, e);
            throw new RedisOperationException("设置过期时间失败", e, "EXPIRE", key, timeout);
        }
    }

    /**
     * 检查键是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    public Boolean hasKey(String key) {
        try {
            log.debug("Checking existence of key: {}", key);
            Boolean result = redisTemplate.hasKey(key);
            log.debug("Key existence result for {}: {}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to check key existence: {}", key, e);
            throw new RedisOperationException("检查键是否存在失败", e, "EXISTS", key);
        }
    }

    /**
     * 获取键的剩余过期时间
     *
     * @param key 键
     * @return 剩余过期时间（秒），-1表示永不过期，-2表示键不存在
     */
    public Long getExpire(String key) {
        try {
            log.debug("Getting expiration time for key: {}", key);
            Long result = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            log.debug("Expiration time for key {}: {} seconds", key, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to get expiration time for key: {}", key, e);
            throw new RedisOperationException("获取过期时间失败", e, "TTL", key);
        }
    }

    /**
     * 根据模式匹配获取键集合
     *
     * @param pattern 匹配模式，支持通配符 * 和 ?
     * @return 匹配的键集合
     */
    public Set<String> keys(String pattern) {
        try {
            log.debug("Getting keys by pattern: {}", pattern);
            Set<String> result = redisTemplate.keys(pattern);
            if (result == null) {
                result = new LinkedHashSet<>();
            }
            log.debug("Found {} keys matching pattern: {}", result.size(), pattern);
            return result;
        } catch (Exception e) {
            log.error("Failed to get keys by pattern: {}", pattern, e);
            throw new RedisOperationException("根据模式获取键失败", e, "KEYS", pattern);
        }
    }

    // ========== Hash Operations ==========

    /**
     * 设置Hash字段值
     *
     * @param key     Hash键
     * @param hashKey Hash字段
     * @param value   值
     */
    public void hSet(String key, String hashKey, Object value) {
        try {
            log.debug("Setting hash field - key: {}, hashKey: {}, value: {}", key, hashKey, value);
            redisTemplate.opsForHash().put(key, hashKey, value);
        } catch (Exception e) {
            log.error("Failed to set hash field - key: {}, hashKey: {}", key, hashKey, e);
            throw new RedisOperationException("设置Hash字段失败", e, "HSET", key, hashKey, value);
        }
    }

    /**
     * 获取Hash字段值
     *
     * @param key     Hash键
     * @param hashKey Hash字段
     * @param clazz   返回值类型
     * @param <T>     泛型类型
     * @return 字段值，如果字段不存在则返回null
     */
    public <T> T hGet(String key, String hashKey, Class<T> clazz) {
        try {
            log.debug(
                    "Getting hash field - key: {}, hashKey: {}, expected type: {}",
                    key,
                    hashKey,
                    clazz.getSimpleName());
            Object value = redisTemplate.opsForHash().get(key, hashKey);

            if (value == null) {
                log.debug("Hash field not found - key: {}, hashKey: {}", key, hashKey);
                return null;
            }

            return convertValue(value, clazz);
        } catch (Exception e) {
            log.error("Failed to get hash field - key: {}, hashKey: {}", key, hashKey, e);
            throw new RedisOperationException("获取Hash字段失败", e, "HGET", key, hashKey);
        }
    }

    /**
     * 获取Hash字段的字符串值（便捷方法）
     *
     * @param key     Hash键
     * @param hashKey Hash字段
     * @return 字符串值
     */
    public String hGetString(String key, String hashKey) {
        return hGet(key, hashKey, String.class);
    }

    /**
     * 获取Hash的所有字段和值
     *
     * @param key Hash键
     * @return 包含所有字段和值的Map，如果Hash不存在则返回空Map
     */
    public java.util.Map<String, Object> hGetAll(String key) {
        try {
            log.debug("Getting all hash fields for key: {}", key);
            java.util.Map<Object, Object> rawMap = redisTemplate.opsForHash().entries(key);

            // 转换为String键的Map
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            for (java.util.Map.Entry<Object, Object> entry : rawMap.entrySet()) {
                String fieldKey = entry.getKey().toString();
                result.put(fieldKey, entry.getValue());
            }

            log.debug("Retrieved {} hash fields for key: {}", result.size(), key);
            return result;
        } catch (Exception e) {
            log.error("Failed to get all hash fields for key: {}", key, e);
            throw new RedisOperationException("获取Hash所有字段失败", e, "HGETALL", key);
        }
    }

    /**
     * 删除Hash中的一个或多个字段
     *
     * @param key      Hash键
     * @param hashKeys 要删除的Hash字段
     * @return 成功删除的字段数量
     */
    public Long hDelete(String key, String... hashKeys) {
        try {
            log.debug(
                    "Deleting hash fields - key: {}, hashKeys: {}",
                    key,
                    java.util.Arrays.toString(hashKeys));
            Long result = redisTemplate.opsForHash().delete(key, (Object[]) hashKeys);
            log.debug("Deleted {} hash fields for key: {}", result, key);
            return result;
        } catch (Exception e) {
            log.error(
                    "Failed to delete hash fields - key: {}, hashKeys: {}",
                    key,
                    java.util.Arrays.toString(hashKeys),
                    e);
            throw new RedisOperationException(
                    "删除Hash字段失败", e, "HDEL", key, java.util.Arrays.toString(hashKeys));
        }
    }

    /**
     * 检查Hash字段是否存在
     *
     * @param key     Hash键
     * @param hashKey Hash字段
     * @return 是否存在
     */
    public Boolean hExists(String key, String hashKey) {
        try {
            log.debug("Checking hash field existence - key: {}, hashKey: {}", key, hashKey);
            Boolean result = redisTemplate.opsForHash().hasKey(key, hashKey);
            log.debug(
                    "Hash field existence result - key: {}, hashKey: {}, exists: {}",
                    key,
                    hashKey,
                    result);
            return result;
        } catch (Exception e) {
            log.error(
                    "Failed to check hash field existence - key: {}, hashKey: {}", key, hashKey, e);
            throw new RedisOperationException("检查Hash字段是否存在失败", e, "HEXISTS", key, hashKey);
        }
    }

    /**
     * 获取Hash中字段的数量
     *
     * @param key Hash键
     * @return 字段数量
     */
    public Long hSize(String key) {
        try {
            log.debug("Getting hash size for key: {}", key);
            Long result = redisTemplate.opsForHash().size(key);
            log.debug("Hash size for key {}: {}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to get hash size for key: {}", key, e);
            throw new RedisOperationException("获取Hash大小失败", e, "HLEN", key);
        }
    }

    /**
     * 获取Hash中所有的字段名
     *
     * @param key Hash键
     * @return 所有字段名的集合
     */
    public java.util.Set<String> hKeys(String key) {
        try {
            log.debug("Getting hash keys for key: {}", key);
            java.util.Set<Object> rawKeys = redisTemplate.opsForHash().keys(key);

            // 转换为String集合
            java.util.Set<String> result = new java.util.HashSet<>();
            for (Object rawKey : rawKeys) {
                result.add(rawKey.toString());
            }

            log.debug("Retrieved {} hash keys for key: {}", result.size(), key);
            return result;
        } catch (Exception e) {
            log.error("Failed to get hash keys for key: {}", key, e);
            throw new RedisOperationException("获取Hash字段名失败", e, "HKEYS", key);
        }
    }

    /**
     * 获取Hash中所有的值
     *
     * @param key Hash键
     * @return 所有值的列表
     */
    public java.util.List<Object> hValues(String key) {
        try {
            log.debug("Getting hash values for key: {}", key);
            java.util.List<Object> result = redisTemplate.opsForHash().values(key);
            log.debug("Retrieved {} hash values for key: {}", result.size(), key);
            return result;
        } catch (Exception e) {
            log.error("Failed to get hash values for key: {}", key, e);
            throw new RedisOperationException("获取Hash值列表失败", e, "HVALS", key);
        }
    }

    /**
     * 为Hash字段的数值增加指定的增量
     *
     * @param key       Hash键
     * @param hashKey   Hash字段
     * @param increment 增量值
     * @return 增加后的值
     */
    public Long hIncrBy(String key, String hashKey, long increment) {
        try {
            log.debug(
                    "Incrementing hash field - key: {}, hashKey: {}, increment: {}",
                    key,
                    hashKey,
                    increment);
            Long result = redisTemplate.opsForHash().increment(key, hashKey, increment);
            log.debug(
                    "Hash field incremented - key: {}, hashKey: {}, new value: {}",
                    key,
                    hashKey,
                    result);
            return result;
        } catch (Exception e) {
            log.error(
                    "Failed to increment hash field - key: {}, hashKey: {}, increment: {}",
                    key,
                    hashKey,
                    increment,
                    e);
            throw new RedisOperationException(
                    "Hash字段数值增加失败", e, "HINCRBY", key, hashKey, increment);
        }
    }

    /**
     * 为Hash字段的浮点数值增加指定的增量
     *
     * @param key       Hash键
     * @param hashKey   Hash字段
     * @param increment 增量值
     * @return 增加后的值
     */
    public Double hIncrByFloat(String key, String hashKey, double increment) {
        try {
            log.debug(
                    "Incrementing hash field by float - key: {}, hashKey: {}, increment: {}",
                    key,
                    hashKey,
                    increment);
            Double result = redisTemplate.opsForHash().increment(key, hashKey, increment);
            log.debug(
                    "Hash field incremented by float - key: {}, hashKey: {}, new value: {}",
                    key,
                    hashKey,
                    result);
            return result;
        } catch (Exception e) {
            log.error(
                    "Failed to increment hash field by float - key: {}, hashKey: {}, increment: {}",
                    key,
                    hashKey,
                    increment,
                    e);
            throw new RedisOperationException(
                    "Hash字段浮点数值增加失败", e, "HINCRBYFLOAT", key, hashKey, increment);
        }
    }

    /**
     * 批量设置Hash字段
     *
     * @param key Hash键
     * @param map 包含字段和值的Map
     */
    public void hMSet(String key, java.util.Map<String, Object> map) {
        try {
            log.debug("Setting multiple hash fields - key: {}, fields count: {}", key, map.size());
            redisTemplate.opsForHash().putAll(key, map);
            log.debug("Successfully set {} hash fields for key: {}", map.size(), key);
        } catch (Exception e) {
            log.error("Failed to set multiple hash fields for key: {}", key, e);
            throw new RedisOperationException("批量设置Hash字段失败", e, "HMSET", key, map);
        }
    }

    /**
     * 批量获取Hash字段值
     *
     * @param key      Hash键
     * @param hashKeys 要获取的Hash字段
     * @return 字段值列表，顺序与输入的字段顺序一致
     */
    public java.util.List<Object> hMGet(String key, String... hashKeys) {
        try {
            log.debug(
                    "Getting multiple hash fields - key: {}, hashKeys: {}",
                    key,
                    java.util.Arrays.toString(hashKeys));
            java.util.List<Object> result = redisTemplate
                    .opsForHash()
                    .multiGet(key, java.util.Arrays.asList((Object[]) hashKeys));
            log.debug("Retrieved {} hash field values for key: {}", result.size(), key);
            return result;
        } catch (Exception e) {
            log.error(
                    "Failed to get multiple hash fields - key: {}, hashKeys: {}",
                    key,
                    java.util.Arrays.toString(hashKeys),
                    e);
            throw new RedisOperationException(
                    "批量获取Hash字段失败", e, "HMGET", key, java.util.Arrays.toString(hashKeys));
        }
    }

    // ========== List Operations ==========

    /**
     * 从列表左侧推入一个或多个元素
     *
     * @param key    列表键
     * @param values 要推入的值
     * @return 推入后列表的长度
     */
    public Long lPush(String key, Object... values) {
        try {
            log.debug("Left pushing to list - key: {}, values count: {}", key, values.length);
            Long result = redisTemplate.opsForList().leftPushAll(key, values);
            log.debug("Left pushed {} elements to list {}, new size: {}", values.length, key, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to left push to list - key: {}", key, e);
            throw new RedisOperationException("列表左侧推入失败", e, "LPUSH", key, values);
        }
    }

    /**
     * 从列表右侧推入一个或多个元素
     *
     * @param key    列表键
     * @param values 要推入的值
     * @return 推入后列表的长度
     */
    public Long rPush(String key, Object... values) {
        try {
            log.debug("Right pushing to list - key: {}, values count: {}", key, values.length);
            Long result = redisTemplate.opsForList().rightPushAll(key, values);
            log.debug("Right pushed {} elements to list {}, new size: {}", values.length, key, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to right push to list - key: {}", key, e);
            throw new RedisOperationException("列表右侧推入失败", e, "RPUSH", key, values);
        }
    }

    /**
     * 从列表左侧弹出一个元素
     *
     * @param key   列表键
     * @param clazz 返回值类型
     * @param <T>   泛型类型
     * @return 弹出的元素，如果列表为空则返回null
     */
    public <T> T lPop(String key, Class<T> clazz) {
        try {
            log.debug("Left popping from list - key: {}, expected type: {}", key, clazz.getSimpleName());
            Object value = redisTemplate.opsForList().leftPop(key);

            if (value == null) {
                log.debug("List is empty or key not found: {}", key);
                return null;
            }

            T result = convertValue(value, clazz);
            log.debug("Left popped element from list {}: {}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to left pop from list - key: {}", key, e);
            throw new RedisOperationException("列表左侧弹出失败", e, "LPOP", key);
        }
    }

    /**
     * 从列表右侧弹出一个元素
     *
     * @param key   列表键
     * @param clazz 返回值类型
     * @param <T>   泛型类型
     * @return 弹出的元素，如果列表为空则返回null
     */
    public <T> T rPop(String key, Class<T> clazz) {
        try {
            log.debug("Right popping from list - key: {}, expected type: {}", key, clazz.getSimpleName());
            Object value = redisTemplate.opsForList().rightPop(key);

            if (value == null) {
                log.debug("List is empty or key not found: {}", key);
                return null;
            }

            T result = convertValue(value, clazz);
            log.debug("Right popped element from list {}: {}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to right pop from list - key: {}", key, e);
            throw new RedisOperationException("列表右侧弹出失败", e, "RPOP", key);
        }
    }

    /**
     * 获取列表指定范围内的元素
     *
     * @param key   列表键
     * @param start 开始索引（包含）
     * @param end   结束索引（包含），-1表示到列表末尾
     * @param clazz 元素类型
     * @param <T>   泛型类型
     * @return 指定范围内的元素列表
     */
    public <T> java.util.List<T> lRange(String key, long start, long end, Class<T> clazz) {
        try {
            log.debug("Getting list range - key: {}, start: {}, end: {}, type: {}",
                    key, start, end, clazz.getSimpleName());
            java.util.List<Object> rawList = redisTemplate.opsForList().range(key, start, end);

            if (rawList == null || rawList.isEmpty()) {
                log.debug("List range is empty - key: {}, start: {}, end: {}", key, start, end);
                return new java.util.ArrayList<>();
            }

            java.util.List<T> result = new java.util.ArrayList<>();
            for (Object item : rawList) {
                result.add(convertValue(item, clazz));
            }

            log.debug("Retrieved {} elements from list range - key: {}", result.size(), key);
            return result;
        } catch (Exception e) {
            log.error("Failed to get list range - key: {}, start: {}, end: {}", key, start, end, e);
            throw new RedisOperationException("获取列表范围失败", e, "LRANGE", key, start, end);
        }
    }

    /**
     * 获取列表长度
     *
     * @param key 列表键
     * @return 列表长度
     */
    public Long lSize(String key) {
        try {
            log.debug("Getting list size for key: {}", key);
            Long result = redisTemplate.opsForList().size(key);
            log.debug("List size for key {}: {}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to get list size for key: {}", key, e);
            throw new RedisOperationException("获取列表长度失败", e, "LLEN", key);
        }
    }

    /**
     * 获取列表指定索引的元素
     *
     * @param key   列表键
     * @param index 索引
     * @param clazz 元素类型
     * @param <T>   泛型类型
     * @return 指定索引的元素，如果索引超出范围则返回null
     */
    public <T> T lIndex(String key, long index, Class<T> clazz) {
        try {
            log.debug("Getting list element by index - key: {}, index: {}, type: {}",
                    key, index, clazz.getSimpleName());
            Object value = redisTemplate.opsForList().index(key, index);

            if (value == null) {
                log.debug("List element not found - key: {}, index: {}", key, index);
                return null;
            }

            T result = convertValue(value, clazz);
            log.debug("Retrieved list element - key: {}, index: {}, value: {}", key, index, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to get list element by index - key: {}, index: {}", key, index, e);
            throw new RedisOperationException("获取列表元素失败", e, "LINDEX", key, index);
        }
    }

    /**
     * 设置列表指定索引的元素值
     *
     * @param key   列表键
     * @param index 索引
     * @param value 新值
     */
    public void lSet(String key, long index, Object value) {
        try {
            log.debug("Setting list element by index - key: {}, index: {}, value: {}", key, index, value);
            redisTemplate.opsForList().set(key, index, value);
            log.debug("Set list element - key: {}, index: {}", key, index);
        } catch (Exception e) {
            log.error("Failed to set list element by index - key: {}, index: {}", key, index, e);
            throw new RedisOperationException("设置列表元素失败", e, "LSET", key, index, value);
        }
    }

    // ========== Set Operations ==========

    /**
     * 向集合添加一个或多个元素
     *
     * @param key    集合键
     * @param values 要添加的值
     * @return 成功添加的元素数量（不包括已存在的元素）
     */
    public Long sAdd(String key, Object... values) {
        try {
            log.debug("Adding elements to set - key: {}, values count: {}", key, values.length);
            Long result = redisTemplate.opsForSet().add(key, values);
            log.debug("Added {} new elements to set {}", result, key);
            return result;
        } catch (Exception e) {
            log.error("Failed to add elements to set - key: {}", key, e);
            throw new RedisOperationException("集合添加元素失败", e, "SADD", key, values);
        }
    }

    /**
     * 从集合中移除一个或多个元素
     *
     * @param key    集合键
     * @param values 要移除的值
     * @return 成功移除的元素数量
     */
    public Long sRem(String key, Object... values) {
        try {
            log.debug("Removing elements from set - key: {}, values count: {}", key, values.length);
            Long result = redisTemplate.opsForSet().remove(key, values);
            log.debug("Removed {} elements from set {}", result, key);
            return result;
        } catch (Exception e) {
            log.error("Failed to remove elements from set - key: {}", key, e);
            throw new RedisOperationException("集合移除元素失败", e, "SREM", key, values);
        }
    }

    /**
     * 获取集合的所有成员
     *
     * @param key   集合键
     * @param clazz 元素类型
     * @param <T>   泛型类型
     * @return 集合的所有成员
     */
    public <T> java.util.Set<T> sMembers(String key, Class<T> clazz) {
        try {
            log.debug("Getting all set members - key: {}, type: {}", key, clazz.getSimpleName());
            java.util.Set<Object> rawSet = redisTemplate.opsForSet().members(key);

            if (rawSet == null || rawSet.isEmpty()) {
                log.debug("Set is empty or key not found: {}", key);
                return new java.util.HashSet<>();
            }

            java.util.Set<T> result = new java.util.HashSet<>();
            for (Object item : rawSet) {
                result.add(convertValue(item, clazz));
            }

            log.debug("Retrieved {} members from set {}", result.size(), key);
            return result;
        } catch (Exception e) {
            log.error("Failed to get set members - key: {}", key, e);
            throw new RedisOperationException("获取集合成员失败", e, "SMEMBERS", key);
        }
    }

    /**
     * 检查元素是否是集合的成员
     *
     * @param key   集合键
     * @param value 要检查的值
     * @return 如果元素是集合成员返回true，否则返回false
     */
    public Boolean sIsMember(String key, Object value) {
        try {
            log.debug("Checking set membership - key: {}, value: {}", key, value);
            Boolean result = redisTemplate.opsForSet().isMember(key, value);
            log.debug("Set membership result - key: {}, value: {}, isMember: {}", key, value, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to check set membership - key: {}, value: {}", key, value, e);
            throw new RedisOperationException("检查集合成员失败", e, "SISMEMBER", key, value);
        }
    }

    /**
     * 获取集合的元素数量
     *
     * @param key 集合键
     * @return 集合的元素数量
     */
    public Long sSize(String key) {
        try {
            log.debug("Getting set size for key: {}", key);
            Long result = redisTemplate.opsForSet().size(key);
            log.debug("Set size for key {}: {}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to get set size for key: {}", key, e);
            throw new RedisOperationException("获取集合大小失败", e, "SCARD", key);
        }
    }

    /**
     * 随机获取集合中的一个元素（不移除）
     *
     * @param key   集合键
     * @param clazz 元素类型
     * @param <T>   泛型类型
     * @return 随机元素，如果集合为空则返回null
     */
    public <T> T sRandomMember(String key, Class<T> clazz) {
        try {
            log.debug("Getting random set member - key: {}, type: {}", key, clazz.getSimpleName());
            Object value = redisTemplate.opsForSet().randomMember(key);

            T result = convertValue(value, clazz);
            log.debug("Retrieved random member from set {}: {}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to get random set member - key: {}", key, e);
            throw new RedisOperationException("获取随机集合成员失败", e, "SRANDMEMBER", key);
        }
    }

    /**
     * 随机获取集合中的多个元素（不移除）
     *
     * @param key   集合键
     * @param count 要获取的元素数量
     * @param clazz 元素类型
     * @param <T>   泛型类型
     * @return 随机元素列表
     */
    public <T> java.util.List<T> sRandomMembers(String key, long count, Class<T> clazz) {
        try {
            log.debug("Getting random set members - key: {}, count: {}, type: {}",
                    key, count, clazz.getSimpleName());
            java.util.List<Object> rawList = redisTemplate.opsForSet().randomMembers(key, count);

            if (rawList == null || rawList.isEmpty()) {
                log.debug("No random members found - key: {}, count: {}", key, count);
                return new java.util.ArrayList<>();
            }

            java.util.List<T> result = new java.util.ArrayList<>();
            for (Object item : rawList) {
                result.add(convertValue(item, clazz));
            }

            log.debug("Retrieved {} random members from set {}", result.size(), key);
            return result;
        } catch (Exception e) {
            log.error("Failed to get random set members - key: {}, count: {}", key, count, e);
            throw new RedisOperationException("获取随机集合成员失败", e, "SRANDMEMBER", key, count);
        }
    }

    /**
     * 随机移除并返回集合中的一个元素
     *
     * @param key   集合键
     * @param clazz 元素类型
     * @param <T>   泛型类型
     * @return 被移除的元素，如果集合为空则返回null
     */
    public <T> T sPop(String key, Class<T> clazz) {
        try {
            log.debug("Popping random element from set - key: {}, type: {}", key, clazz.getSimpleName());
            Object value = redisTemplate.opsForSet().pop(key);

            if (value == null) {
                log.debug("Set is empty or key not found: {}", key);
                return null;
            }

            T result = convertValue(value, clazz);
            log.debug("Popped random element from set {}: {}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to pop random element from set - key: {}", key, e);
            throw new RedisOperationException("随机弹出集合元素失败", e, "SPOP", key);
        }
    }

    // ========== ZSet Operations ==========

    /**
     * 向有序集合添加一个成员，或者更新已存在成员的分数
     *
     * @param key   有序集合键
     * @param value 成员值
     * @param score 分数
     * @return 如果成员是新成员返回true，如果是更新已存在成员的分数返回false
     */
    public Boolean zAdd(String key, Object value, double score) {
        try {
            log.debug("Adding member to sorted set - key: {}, value: {}, score: {}", key, value, score);
            Boolean result = redisTemplate.opsForZSet().add(key, value, score);
            log.debug("ZSet add result - key: {}, value: {}, isNew: {}", key, value, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to add member to sorted set - key: {}, value: {}, score: {}", key, value, score, e);
            throw new RedisOperationException("有序集合添加成员失败", e, "ZADD", key, value, score);
        }
    }

    /**
     * 批量向有序集合添加成员
     *
     * @param key           有序集合键
     * @param scoreValueMap 分数和值的映射
     * @return 成功添加的新成员数量
     */
    public Long zAdd(String key, java.util.Map<Object, Double> scoreValueMap) {
        try {
            log.debug("Adding multiple members to sorted set - key: {}, members count: {}", key, scoreValueMap.size());

            // 转换为Spring Data Redis需要的格式
            Set<ZSetOperations.TypedTuple<Object>> tuples = new java.util.HashSet<>();

            for (java.util.Map.Entry<Object, Double> entry : scoreValueMap.entrySet()) {
                tuples.add(ZSetOperations.TypedTuple.of(entry.getKey(), entry.getValue()));
            }

            Long result = redisTemplate.opsForZSet().add(key, tuples);
            log.debug("ZSet batch add result - key: {}, new members: {}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to add multiple members to sorted set - key: {}", key, e);
            throw new RedisOperationException("有序集合批量添加成员失败", e, "ZADD", key, scoreValueMap);
        }
    }

    /**
     * 从有序集合中移除一个或多个成员
     *
     * @param key    有序集合键
     * @param values 要移除的成员值
     * @return 成功移除的成员数量
     */
    public Long zRem(String key, Object... values) {
        try {
            log.debug("Removing members from sorted set - key: {}, values count: {}", key, values.length);
            Long result = redisTemplate.opsForZSet().remove(key, values);
            log.debug("ZSet remove result - key: {}, removed count: {}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to remove members from sorted set - key: {}", key, e);
            throw new RedisOperationException("有序集合移除成员失败", e, "ZREM", key, values);
        }
    }

    /**
     * 获取有序集合指定排名范围内的成员（按分数从低到高排序）
     *
     * @param key   有序集合键
     * @param start 开始排名（包含）
     * @param end   结束排名（包含），-1表示到集合末尾
     * @param clazz 成员类型
     * @param <T>   泛型类型
     * @return 指定排名范围内的成员集合
     */
    public <T> java.util.Set<T> zRange(String key, long start, long end, Class<T> clazz) {
        try {
            log.debug("Getting sorted set range - key: {}, start: {}, end: {}, type: {}",
                    key, start, end, clazz.getSimpleName());
            java.util.Set<Object> rawSet = redisTemplate.opsForZSet().range(key, start, end);

            if (rawSet == null || rawSet.isEmpty()) {
                log.debug("ZSet range is empty - key: {}, start: {}, end: {}", key, start, end);
                return new java.util.LinkedHashSet<>();
            }

            java.util.Set<T> result = new java.util.LinkedHashSet<>();
            for (Object item : rawSet) {
                result.add(convertValue(item, clazz));
            }

            log.debug("Retrieved {} members from sorted set range - key: {}", result.size(), key);
            return result;
        } catch (Exception e) {
            log.error("Failed to get sorted set range - key: {}, start: {}, end: {}", key, start, end, e);
            throw new RedisOperationException("获取有序集合范围失败", e, "ZRANGE", key, start, end);
        }
    }

    /**
     * 获取有序集合指定排名范围内的成员及其分数（按分数从低到高排序）
     *
     * @param key   有序集合键
     * @param start 开始排名（包含）
     * @param end   结束排名（包含），-1表示到集合末尾
     * @param clazz 成员类型
     * @param <T>   泛型类型
     * @return 包含成员和分数的TypedTuple集合
     */
    public <T> Set<ZSetOperations.TypedTuple<T>> zRangeWithScores(
            String key, long start, long end, Class<T> clazz) {
        try {
            log.debug("Getting sorted set range with scores - key: {}, start: {}, end: {}, type: {}",
                    key, start, end, clazz.getSimpleName());
            java.util.Set<ZSetOperations.TypedTuple<Object>> rawSet = redisTemplate
                    .opsForZSet().rangeWithScores(key, start, end);

            if (rawSet == null || rawSet.isEmpty()) {
                log.debug("ZSet range with scores is empty - key: {}, start: {}, end: {}", key, start, end);
                return new java.util.LinkedHashSet<>();
            }

            Set<ZSetOperations.TypedTuple<T>> result = new java.util.LinkedHashSet<>();

            for (ZSetOperations.TypedTuple<Object> tuple : rawSet) {
                T convertedValue = convertValue(tuple.getValue(), clazz);
                if (convertedValue != null) {
                    result.add(ZSetOperations.TypedTuple.of(convertedValue, tuple.getScore()));
                }
            }

            log.debug("Retrieved {} members with scores from sorted set range - key: {}", result.size(), key);
            return result;
        } catch (Exception e) {
            log.error("Failed to get sorted set range with scores - key: {}, start: {}, end: {}", key, start, end, e);
            throw new RedisOperationException("获取有序集合范围和分数失败", e, "ZRANGE", key, start, end);
        }
    }

    /**
     * 获取有序集合指定排名范围内的成员（按分数从高到低排序）
     *
     * @param key   有序集合键
     * @param start 开始排名（包含）
     * @param end   结束排名（包含），-1表示到集合末尾
     * @param clazz 成员类型
     * @param <T>   泛型类型
     * @return 指定排名范围内的成员集合（按分数从高到低）
     */
    public <T> Set<T> zRevRange(String key, long start, long end, Class<T> clazz) {
        try {
            log.debug("Getting sorted set reverse range - key: {}, start: {}, end: {}, type: {}",
                    key, start, end, clazz.getSimpleName());
            Set<Object> rawSet = redisTemplate.opsForZSet().reverseRange(key, start, end);

            if (rawSet == null || rawSet.isEmpty()) {
                log.debug("ZSet reverse range is empty - key: {}, start: {}, end: {}", key, start, end);
                return new java.util.LinkedHashSet<>();
            }

            Set<T> result = new LinkedHashSet<>();
            for (Object item : rawSet) {
                result.add(convertValue(item, clazz));
            }

            log.debug("Retrieved {} members from sorted set reverse range - key: {}", result.size(), key);
            return result;
        } catch (Exception e) {
            log.error("Failed to get sorted set reverse range - key: {}, start: {}, end: {}", key, start, end, e);
            throw new RedisOperationException("获取有序集合逆序范围失败", e, "ZREVRANGE", key, start, end);
        }
    }

    /**
     * 获取有序集合中指定成员的分数
     *
     * @param key   有序集合键
     * @param value 成员值
     * @return 成员的分数，如果成员不存在则返回null
     */
    public Double zScore(String key, Object value) {
        try {
            log.debug("Getting member score from sorted set - key: {}, value: {}", key, value);
            Double result = redisTemplate.opsForZSet().score(key, value);
            log.debug("ZSet score result - key: {}, value: {}, score: {}", key, value, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to get member score from sorted set - key: {}, value: {}", key, value, e);
            throw new RedisOperationException("获取有序集合成员分数失败", e, "ZSCORE", key, value);
        }
    }

    /**
     * 获取有序集合中指定成员的排名（按分数从低到高）
     *
     * @param key   有序集合键
     * @param value 成员值
     * @return 成员的排名（从0开始），如果成员不存在则返回null
     */
    public Long zRank(String key, Object value) {
        try {
            log.debug("Getting member rank from sorted set - key: {}, value: {}", key, value);
            Long result = redisTemplate.opsForZSet().rank(key, value);
            log.debug("ZSet rank result - key: {}, value: {}, rank: {}", key, value, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to get member rank from sorted set - key: {}, value: {}", key, value, e);
            throw new RedisOperationException("获取有序集合成员排名失败", e, "ZRANK", key, value);
        }
    }

    /**
     * 获取有序集合中指定成员的逆序排名（按分数从高到低）
     *
     * @param key   有序集合键
     * @param value 成员值
     * @return 成员的逆序排名（从0开始），如果成员不存在则返回null
     */
    public Long zRevRank(String key, Object value) {
        try {
            log.debug("Getting member reverse rank from sorted set - key: {}, value: {}", key, value);
            Long result = redisTemplate.opsForZSet().reverseRank(key, value);
            log.debug("ZSet reverse rank result - key: {}, value: {}, rank: {}", key, value, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to get member reverse rank from sorted set - key: {}, value: {}", key, value, e);
            throw new RedisOperationException("获取有序集合成员逆序排名失败", e, "ZREVRANK", key, value);
        }
    }

    /**
     * 获取有序集合的成员数量
     *
     * @param key 有序集合键
     * @return 成员数量
     */
    public Long zSize(String key) {
        try {
            log.debug("Getting sorted set size for key: {}", key);
            Long result = redisTemplate.opsForZSet().size(key);
            log.debug("ZSet size for key {}: {}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to get sorted set size for key: {}", key, e);
            throw new RedisOperationException("获取有序集合大小失败", e, "ZCARD", key);
        }
    }

    /**
     * 获取有序集合中指定分数范围内的成员数量
     *
     * @param key 有序集合键
     * @param min 最小分数（包含）
     * @param max 最大分数（包含）
     * @return 指定分数范围内的成员数量
     */
    public Long zCount(String key, double min, double max) {
        try {
            log.debug("Counting sorted set members by score range - key: {}, min: {}, max: {}", key, min, max);
            Long result = redisTemplate.opsForZSet().count(key, min, max);
            log.debug("ZSet count result - key: {}, min: {}, max: {}, count: {}", key, min, max, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to count sorted set members by score range - key: {}, min: {}, max: {}", key, min, max,
                    e);
            throw new RedisOperationException("统计有序集合分数范围成员数量失败", e, "ZCOUNT", key, min, max);
        }
    }

    /**
     * 为有序集合中指定成员的分数增加增量
     *
     * @param key       有序集合键
     * @param value     成员值
     * @param increment 分数增量
     * @return 增加后的分数
     */
    public Double zIncrBy(String key, Object value, double increment) {
        try {
            log.debug("Incrementing sorted set member score - key: {}, value: {}, increment: {}", key, value,
                    increment);
            Double result = redisTemplate.opsForZSet().incrementScore(key, value, increment);
            log.debug("ZSet increment result - key: {}, value: {}, new score: {}", key, value, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to increment sorted set member score - key: {}, value: {}, increment: {}", key, value,
                    increment, e);
            throw new RedisOperationException("有序集合成员分数增加失败", e, "ZINCRBY", key, value, increment);
        }
    }

    /**
     * 获取有序集合中指定分数范围内的成员
     *
     * @param key   有序集合键
     * @param min   最小分数（包含）
     * @param max   最大分数（包含）
     * @param clazz 成员类型
     * @param <T>   泛型类型
     * @return 指定分数范围内的成员集合
     */
    public <T> Set<T> zRangeByScore(String key, double min, double max, Class<T> clazz) {
        try {
            log.debug("Getting sorted set members by score range - key: {}, min: {}, max: {}, type: {}",
                    key, min, max, clazz.getSimpleName());
            Set<Object> rawSet = redisTemplate.opsForZSet().rangeByScore(key, min, max);

            if (rawSet == null || rawSet.isEmpty()) {
                log.debug("ZSet range by score is empty - key: {}, min: {}, max: {}", key, min, max);
                return new LinkedHashSet<>();
            }

            Set<T> result = new LinkedHashSet<>();
            for (Object item : rawSet) {
                result.add(convertValue(item, clazz));
            }

            log.debug("Retrieved {} members from sorted set by score range - key: {}", result.size(), key);
            return result;
        } catch (Exception e) {
            log.error("Failed to get sorted set members by score range - key: {}, min: {}, max: {}", key, min, max, e);
            throw new RedisOperationException("按分数范围获取有序集合成员失败", e, "ZRANGEBYSCORE", key, min, max);
        }
    }

    /**
     * 移除有序集合中指定分数范围内的成员
     *
     * @param key 有序集合键
     * @param min 最小分数（包含）
     * @param max 最大分数（包含）
     * @return 移除的成员数量
     */
    public Long zRemRangeByScore(String key, double min, double max) {
        try {
            log.debug("Removing sorted set members by score range - key: {}, min: {}, max: {}", key, min, max);
            Long result = redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
            log.debug("ZSet remove by score result - key: {}, min: {}, max: {}, removed: {}", key, min, max, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to remove sorted set members by score range - key: {}, min: {}, max: {}", key, min, max,
                    e);
            throw new RedisOperationException("按分数范围移除有序集合成员失败", e, "ZREMRANGEBYSCORE", key, min, max);
        }
    }

    /**
     * 移除有序集合中指定排名范围内的成员
     *
     * @param key   有序集合键
     * @param start 开始排名（包含）
     * @param end   结束排名（包含）
     * @return 移除的成员数量
     */
    public Long zRemRangeByRank(String key, long start, long end) {
        try {
            log.debug("Removing sorted set members by rank range - key: {}, start: {}, end: {}", key, start, end);
            Long result = redisTemplate.opsForZSet().removeRange(key, start, end);
            log.debug("ZSet remove by rank result - key: {}, start: {}, end: {}, removed: {}", key, start, end, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to remove sorted set members by rank range - key: {}, start: {}, end: {}", key, start,
                    end, e);
            throw new RedisOperationException("按排名范围移除有序集合成员失败", e, "ZREMRANGEBYRANK", key, start, end);
        }
    }

    // ========== Transaction Support ==========

    /**
     * 在Redis事务中执行操作
     *
     * <p>
     * 使用Redis的MULTI/EXEC命令来确保操作的原子性。
     * 如果回调中的任何操作失败，整个事务将被回滚。
     * </p>
     *
     * @param callback 事务回调
     * @param <T>      返回值类型
     * @return 事务执行结果
     */
    public <T> T executeInTransaction(RedisCallback<T> callback) {
        try {
            log.debug("Starting Redis transaction");

            return redisTemplate.execute(new SessionCallback<T>() {
                @Override
                @SuppressWarnings("unchecked")
                public T execute(RedisOperations operations)
                        throws org.springframework.dao.DataAccessException {

                    // 开始事务
                    operations.multi();

                    try {
                        // 执行回调中的操作
                        T result = callback.doInRedis(RedisUtils.this);

                        // 提交事务
                        List<Object> execResults = operations.exec();

                        if (execResults == null) {
                            log.warn("Redis transaction was discarded (WATCH key was modified)");
                            throw new RedisOperationException("Redis事务被丢弃，可能是监视的键被修改", (String) null, "EXEC");
                        }

                        log.debug("Redis transaction committed successfully with {} operations", execResults.size());
                        return result;

                    } catch (Exception e) {
                        log.error("Error in Redis transaction, discarding", e);
                        // 发生异常时，事务会自动被丢弃
                        operations.discard();
                        throw new RedisOperationException("Redis事务执行失败", e, "TRANSACTION");
                    }
                }
            });

        } catch (Exception e) {
            log.error("Failed to execute Redis transaction", e);
            if (e instanceof RedisOperationException) {
                throw e;
            }
            throw new RedisOperationException("Redis事务执行失败", e, "TRANSACTION");
        }
    }

    /**
     * 在Redis事务中执行操作（无返回值版本）
     *
     * 便捷方法：接受一个消费型回调，无需返回值。
     *
     * @param action 事务中要执行的操作
     */
    public void executeInTransaction(Consumer<RedisUtils> action) {
        executeInTransaction(ops -> {
            action.accept(ops);
            return null;
        });
    }

    /**
     * 监视一个或多个键，用于乐观锁
     *
     * <p>
     * 在事务开始前调用此方法来监视键的变化。
     * 如果在事务执行期间被监视的键发生了变化，事务将被丢弃。
     * </p>
     *
     * @param keys 要监视的键
     */
    public void watch(String... keys) {
        try {
            log.debug("Watching keys for transaction: {}", Arrays.toString(keys));
            redisTemplate.watch(Arrays.asList((String[]) keys));
        } catch (Exception e) {
            log.error("Failed to watch keys: {}", Arrays.toString(keys), e);
            throw new RedisOperationException("监视键失败", e, "WATCH", keys);
        }
    }

    /**
     * 取消对所有键的监视
     */
    public void unwatch() {
        try {
            log.debug("Unwatching all keys");
            redisTemplate.unwatch();
        } catch (Exception e) {
            log.error("Failed to unwatch keys", e);
            throw new RedisOperationException("取消监视键失败", e, "UNWATCH");
        }
    }

    /**
     * 类型转换工具方法
     *
     * @param value 原始值
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 转换后的值
     */
    @SuppressWarnings("unchecked")
    private <T> T convertValue(Object value, Class<T> clazz) {
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
            return objectMapper.readValue(jsonString, clazz);
        } catch (JsonProcessingException e) {
            throw new RedisOperationException("类型转换失败", e, "CONVERT", value, clazz);
        }
    }

    // ========== Transaction Support Methods ==========

    /**
     * 记录事务操作（如果在事务中）
     *
     * @param operation 操作描述
     */
    private void recordOperation(String operation) {
        if (transactionManager != null && transactionManager.isInTransaction()) {
            transactionManager.addOperation(operation);
        }
    }

    /**
     * 检查是否在事务中
     *
     * @return 如果当前在事务中返回true
     */
    public boolean isInTransaction() {
        return transactionManager != null && transactionManager.isInTransaction();
    }

    /**
     * 获取当前事务上下文
     *
     * @return 当前事务上下文，如果不在事务中则返回null
     */
    public TransactionContext getCurrentTransactionContext() {
        return transactionManager != null ? transactionManager.getCurrentTransaction() : null;
    }

    // ========== Utility Methods ==========

    /**
     * Redis事务回调接口
     *
     * @param <T> 返回值类型
     */
    @FunctionalInterface
    public interface RedisCallback<T> {
        /**
         * 在事务中执行的操作
         *
         * @param operations Redis操作对象
         * @return 操作结果
         */
        T doInRedis(RedisUtils operations);
    }

}
