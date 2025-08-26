package com.david.redis.commons.core.operations;

import com.david.redis.commons.core.transaction.RedisTransactionManager;
import com.david.redis.commons.core.utils.RedisOperationUtils;
import com.david.redis.commons.core.utils.RedisTypeConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;

/**
 * Redis Hash类型操作实现类
 * 
 * <p>实现所有Hash类型的Redis操作方法
 * 
 * @author David
 */
@Slf4j
public class RedisHashOperationsImpl implements RedisHashOperations {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTransactionManager transactionManager;

    public RedisHashOperationsImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.transactionManager = null;
    }

    public RedisHashOperationsImpl(RedisTemplate<String, Object> redisTemplate, 
                                 RedisTransactionManager transactionManager) {
        this.redisTemplate = redisTemplate;
        this.transactionManager = transactionManager;
    }

    @Override
    public void hSet(String key, String hashKey, Object value) {
        RedisOperationUtils.executeWithExceptionHandling("HSET", key, 
            new Object[]{hashKey, value}, () -> {
            RedisOperationUtils.logDebug("Setting hash field - key: {}, hashKey: {}, value: {}", key, hashKey, value);
            redisTemplate.opsForHash().put(key, hashKey, value);
            return null;
        });
    }

    @Override
    public <T> T hGet(String key, String hashKey, Class<T> clazz) {
        return RedisOperationUtils.executeWithExceptionHandling("HGET", key, 
            new Object[]{hashKey}, () -> {
            RedisOperationUtils.logDebug("Getting hash field - key: {}, hashKey: {}, expected type: {}", 
                key, hashKey, clazz.getSimpleName());
            Object value = redisTemplate.opsForHash().get(key, hashKey);

            if (value == null) {
                RedisOperationUtils.logDebug("Hash field not found - key: {}, hashKey: {}", key, hashKey);
                return null;
            }

            return RedisTypeConverter.convertValue(value, clazz);
        });
    }

    @Override
    public String hGetString(String key, String hashKey) {
        return hGet(key, hashKey, String.class);
    }

    @Override
    public Map<String, Object> hGetAll(String key) {
        return RedisOperationUtils.executeWithExceptionHandling("HGETALL", key, () -> {
            RedisOperationUtils.logDebug("Getting all hash fields for key: {}", key);
            Map<Object, Object> rawMap = redisTemplate.opsForHash().entries(key);

            // 转换为String键的Map
            Map<String, Object> result = new HashMap<>();
            for (Map.Entry<Object, Object> entry : rawMap.entrySet()) {
                String fieldKey = entry.getKey().toString();
                result.put(fieldKey, entry.getValue());
            }

            RedisOperationUtils.logDebug("Retrieved {} hash fields for key: {}", result.size(), key);
            return result;
        });
    }

    @Override
    public Long hDelete(String key, String... hashKeys) {
        return RedisOperationUtils.executeWithExceptionHandling("HDEL", key, 
            new Object[]{Arrays.toString(hashKeys)}, () -> {
            RedisOperationUtils.logDebug("Deleting hash fields - key: {}, hashKeys: {}", 
                key, Arrays.toString(hashKeys));
            Long result = redisTemplate.opsForHash().delete(key, (Object[]) hashKeys);
            RedisOperationUtils.logDebug("Deleted {} hash fields for key: {}", result, key);
            return result;
        });
    }

    @Override
    public Boolean hExists(String key, String hashKey) {
        return RedisOperationUtils.executeWithExceptionHandling("HEXISTS", key, 
            new Object[]{hashKey}, () -> {
            RedisOperationUtils.logDebug("Checking hash field existence - key: {}, hashKey: {}", key, hashKey);
            Boolean result = redisTemplate.opsForHash().hasKey(key, hashKey);
            RedisOperationUtils.logDebug("Hash field existence result - key: {}, hashKey: {}, exists: {}", 
                key, hashKey, result);
            return result;
        });
    }

    @Override
    public Long hSize(String key) {
        return RedisOperationUtils.executeWithExceptionHandling("HLEN", key, () -> {
            RedisOperationUtils.logDebug("Getting hash size for key: {}", key);
            Long result = redisTemplate.opsForHash().size(key);
            RedisOperationUtils.logDebug("Hash size for key {}: {}", key, result);
            return result;
        });
    }

    @Override
    public Set<String> hKeys(String key) {
        return RedisOperationUtils.executeWithExceptionHandling("HKEYS", key, () -> {
            RedisOperationUtils.logDebug("Getting hash keys for key: {}", key);
            Set<Object> rawKeys = redisTemplate.opsForHash().keys(key);

            // 转换为String集合
            Set<String> result = new HashSet<>();
            for (Object rawKey : rawKeys) {
                result.add(rawKey.toString());
            }

            RedisOperationUtils.logDebug("Retrieved {} hash keys for key: {}", result.size(), key);
            return result;
        });
    }

    @Override
    public List<Object> hValues(String key) {
        return RedisOperationUtils.executeWithExceptionHandling("HVALS", key, () -> {
            RedisOperationUtils.logDebug("Getting hash values for key: {}", key);
            List<Object> result = redisTemplate.opsForHash().values(key);
            RedisOperationUtils.logDebug("Retrieved {} hash values for key: {}", result.size(), key);
            return result;
        });
    }

    @Override
    public Long hIncrBy(String key, String hashKey, long increment) {
        return RedisOperationUtils.executeWithExceptionHandling("HINCRBY", key, 
            new Object[]{hashKey, increment}, () -> {
            RedisOperationUtils.logDebug("Incrementing hash field - key: {}, hashKey: {}, increment: {}", 
                key, hashKey, increment);
            Long result = redisTemplate.opsForHash().increment(key, hashKey, increment);
            RedisOperationUtils.logDebug("Hash field incremented - key: {}, hashKey: {}, new value: {}", 
                key, hashKey, result);
            return result;
        });
    }

    @Override
    public Double hIncrByFloat(String key, String hashKey, double increment) {
        return RedisOperationUtils.executeWithExceptionHandling("HINCRBYFLOAT", key, 
            new Object[]{hashKey, increment}, () -> {
            RedisOperationUtils.logDebug("Incrementing hash field by float - key: {}, hashKey: {}, increment: {}", 
                key, hashKey, increment);
            Double result = redisTemplate.opsForHash().increment(key, hashKey, increment);
            RedisOperationUtils.logDebug("Hash field incremented by float - key: {}, hashKey: {}, new value: {}", 
                key, hashKey, result);
            return result;
        });
    }

    @Override
    public void hMSet(String key, Map<String, Object> map) {
        RedisOperationUtils.executeWithExceptionHandling("HMSET", key, 
            new Object[]{map}, () -> {
            RedisOperationUtils.logDebug("Setting multiple hash fields - key: {}, fields count: {}", key, map.size());
            redisTemplate.opsForHash().putAll(key, map);
            RedisOperationUtils.logDebug("Successfully set {} hash fields for key: {}", map.size(), key);
            return null;
        });
    }

    @Override
    public List<Object> hMGet(String key, String... hashKeys) {
        return RedisOperationUtils.executeWithExceptionHandling("HMGET", key, 
            new Object[]{Arrays.toString(hashKeys)}, () -> {
            RedisOperationUtils.logDebug("Getting multiple hash fields - key: {}, hashKeys: {}", 
                key, Arrays.toString(hashKeys));
            List<Object> result = redisTemplate.opsForHash().multiGet(key, Arrays.asList(hashKeys));
            RedisOperationUtils.logDebug("Retrieved {} hash field values for key: {}", result.size(), key);
            return result;
        });
    }
}
