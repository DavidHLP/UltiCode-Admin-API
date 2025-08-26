package com.david.redis.commons.core.operations;

import com.david.redis.commons.core.transaction.RedisTransactionManager;
import com.david.redis.commons.core.utils.RedisOperationUtils;
import com.david.redis.commons.core.utils.RedisTypeConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis List类型操作实现类
 * 
 * <p>实现所有List类型的Redis操作方法
 * 
 * @author David
 */
@Slf4j
public class RedisListOperationsImpl implements RedisListOperations {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTransactionManager transactionManager;

    public RedisListOperationsImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.transactionManager = null;
    }

    public RedisListOperationsImpl(RedisTemplate<String, Object> redisTemplate, 
                                 RedisTransactionManager transactionManager) {
        this.redisTemplate = redisTemplate;
        this.transactionManager = transactionManager;
    }

    @Override
    public Long lPush(String key, Object... values) {
        return RedisOperationUtils.executeWithExceptionHandling("LPUSH", key, 
            new Object[]{values}, () -> {
            RedisOperationUtils.logDebug("Left pushing to list - key: {}, values count: {}", key, values.length);
            Long result = redisTemplate.opsForList().leftPushAll(key, values);
            RedisOperationUtils.logDebug("Left pushed {} elements to list {}, new size: {}", 
                values.length, key, result);
            return result;
        });
    }

    @Override
    public Long rPush(String key, Object... values) {
        return RedisOperationUtils.executeWithExceptionHandling("RPUSH", key, 
            new Object[]{values}, () -> {
            RedisOperationUtils.logDebug("Right pushing to list - key: {}, values count: {}", key, values.length);
            Long result = redisTemplate.opsForList().rightPushAll(key, values);
            RedisOperationUtils.logDebug("Right pushed {} elements to list {}, new size: {}", 
                values.length, key, result);
            return result;
        });
    }

    @Override
    public <T> T lPop(String key, Class<T> clazz) {
        return RedisOperationUtils.executeWithExceptionHandling("LPOP", key, () -> {
            RedisOperationUtils.logDebug("Left popping from list - key: {}, expected type: {}", 
                key, clazz.getSimpleName());
            Object value = redisTemplate.opsForList().leftPop(key);

            if (value == null) {
                RedisOperationUtils.logDebug("List is empty or key not found: {}", key);
                return null;
            }

            T result = RedisTypeConverter.convertValue(value, clazz);
            RedisOperationUtils.logDebug("Left popped element from list {}: {}", key, result);
            return result;
        });
    }

    @Override
    public <T> T rPop(String key, Class<T> clazz) {
        return RedisOperationUtils.executeWithExceptionHandling("RPOP", key, () -> {
            RedisOperationUtils.logDebug("Right popping from list - key: {}, expected type: {}", 
                key, clazz.getSimpleName());
            Object value = redisTemplate.opsForList().rightPop(key);

            if (value == null) {
                RedisOperationUtils.logDebug("List is empty or key not found: {}", key);
                return null;
            }

            T result = RedisTypeConverter.convertValue(value, clazz);
            RedisOperationUtils.logDebug("Right popped element from list {}: {}", key, result);
            return result;
        });
    }

    @Override
    public <T> List<T> lRange(String key, long start, long end, Class<T> clazz) {
        return RedisOperationUtils.executeWithExceptionHandling("LRANGE", key, 
            new Object[]{start, end}, () -> {
            RedisOperationUtils.logDebug("Getting list range - key: {}, start: {}, end: {}, type: {}", 
                key, start, end, clazz.getSimpleName());
            List<Object> rawList = redisTemplate.opsForList().range(key, start, end);

            if (rawList == null || rawList.isEmpty()) {
                RedisOperationUtils.logDebug("List range is empty - key: {}, start: {}, end: {}", key, start, end);
                return new ArrayList<>();
            }

            List<T> result = new ArrayList<>();
            for (Object item : rawList) {
                result.add(RedisTypeConverter.convertValue(item, clazz));
            }

            RedisOperationUtils.logDebug("Retrieved {} elements from list range - key: {}", result.size(), key);
            return result;
        });
    }

    @Override
    public Long lSize(String key) {
        return RedisOperationUtils.executeWithExceptionHandling("LLEN", key, () -> {
            RedisOperationUtils.logDebug("Getting list size for key: {}", key);
            Long result = redisTemplate.opsForList().size(key);
            RedisOperationUtils.logDebug("List size for key {}: {}", key, result);
            return result;
        });
    }

    @Override
    public <T> T lIndex(String key, long index, Class<T> clazz) {
        return RedisOperationUtils.executeWithExceptionHandling("LINDEX", key, 
            new Object[]{index}, () -> {
            RedisOperationUtils.logDebug("Getting list element by index - key: {}, index: {}, type: {}", 
                key, index, clazz.getSimpleName());
            Object value = redisTemplate.opsForList().index(key, index);

            if (value == null) {
                RedisOperationUtils.logDebug("List element not found - key: {}, index: {}", key, index);
                return null;
            }

            T result = RedisTypeConverter.convertValue(value, clazz);
            RedisOperationUtils.logDebug("Retrieved list element - key: {}, index: {}, value: {}", key, index, result);
            return result;
        });
    }

    @Override
    public void lSet(String key, long index, Object value) {
        RedisOperationUtils.executeWithExceptionHandling("LSET", key, 
            new Object[]{index, value}, () -> {
            RedisOperationUtils.logDebug("Setting list element by index - key: {}, index: {}, value: {}", 
                key, index, value);
            redisTemplate.opsForList().set(key, index, value);
            RedisOperationUtils.logDebug("Set list element - key: {}, index: {}", key, index);
            return null;
        });
    }
}
