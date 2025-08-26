package com.david.redis.commons.core.operations;

import com.david.redis.commons.core.transaction.RedisTransactionManager;
import com.david.redis.commons.core.utils.RedisOperationUtils;
import com.david.redis.commons.core.utils.RedisTypeConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Redis Set类型操作实现类
 * 
 * <p>实现所有Set类型的Redis操作方法
 * 
 * @author David
 */
@Slf4j
public class RedisSetOperationsImpl implements RedisSetOperations {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTransactionManager transactionManager;

    public RedisSetOperationsImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.transactionManager = null;
    }

    public RedisSetOperationsImpl(RedisTemplate<String, Object> redisTemplate, 
                                RedisTransactionManager transactionManager) {
        this.redisTemplate = redisTemplate;
        this.transactionManager = transactionManager;
    }

    @Override
    public Long sAdd(String key, Object... values) {
        return RedisOperationUtils.executeWithExceptionHandling("SADD", key, 
            new Object[]{values}, () -> {
            RedisOperationUtils.logDebug("Adding elements to set - key: {}, values count: {}", key, values.length);
            Long result = redisTemplate.opsForSet().add(key, values);
            RedisOperationUtils.logDebug("Added {} new elements to set {}", result, key);
            return result;
        });
    }

    @Override
    public Long sRem(String key, Object... values) {
        return RedisOperationUtils.executeWithExceptionHandling("SREM", key, 
            new Object[]{values}, () -> {
            RedisOperationUtils.logDebug("Removing elements from set - key: {}, values count: {}", key, values.length);
            Long result = redisTemplate.opsForSet().remove(key, values);
            RedisOperationUtils.logDebug("Removed {} elements from set {}", result, key);
            return result;
        });
    }

    @Override
    public <T> Set<T> sMembers(String key, Class<T> clazz) {
        return RedisOperationUtils.executeWithExceptionHandling("SMEMBERS", key, () -> {
            RedisOperationUtils.logDebug("Getting all set members - key: {}, type: {}", key, clazz.getSimpleName());
            Set<Object> rawSet = redisTemplate.opsForSet().members(key);

            if (rawSet == null || rawSet.isEmpty()) {
                RedisOperationUtils.logDebug("Set is empty or key not found: {}", key);
                return new HashSet<>();
            }

            Set<T> result = new HashSet<>();
            for (Object item : rawSet) {
                result.add(RedisTypeConverter.convertValue(item, clazz));
            }

            RedisOperationUtils.logDebug("Retrieved {} members from set {}", result.size(), key);
            return result;
        });
    }

    @Override
    public Boolean sIsMember(String key, Object value) {
        return RedisOperationUtils.executeWithExceptionHandling("SISMEMBER", key, 
            new Object[]{value}, () -> {
            RedisOperationUtils.logDebug("Checking set membership - key: {}, value: {}", key, value);
            Boolean result = redisTemplate.opsForSet().isMember(key, value);
            RedisOperationUtils.logDebug("Set membership result - key: {}, value: {}, isMember: {}", key, value, result);
            return result;
        });
    }

    @Override
    public Long sSize(String key) {
        return RedisOperationUtils.executeWithExceptionHandling("SCARD", key, () -> {
            RedisOperationUtils.logDebug("Getting set size for key: {}", key);
            Long result = redisTemplate.opsForSet().size(key);
            RedisOperationUtils.logDebug("Set size for key {}: {}", key, result);
            return result;
        });
    }

    @Override
    public <T> T sRandomMember(String key, Class<T> clazz) {
        return RedisOperationUtils.executeWithExceptionHandling("SRANDMEMBER", key, () -> {
            RedisOperationUtils.logDebug("Getting random set member - key: {}, type: {}", key, clazz.getSimpleName());
            Object value = redisTemplate.opsForSet().randomMember(key);

            T result = RedisTypeConverter.convertValue(value, clazz);
            RedisOperationUtils.logDebug("Retrieved random member from set {}: {}", key, result);
            return result;
        });
    }

    @Override
    public <T> List<T> sRandomMembers(String key, long count, Class<T> clazz) {
        return RedisOperationUtils.executeWithExceptionHandling("SRANDMEMBER", key, 
            new Object[]{count}, () -> {
            RedisOperationUtils.logDebug("Getting random set members - key: {}, count: {}, type: {}", 
                key, count, clazz.getSimpleName());
            List<Object> rawList = redisTemplate.opsForSet().randomMembers(key, count);

            if (rawList == null || rawList.isEmpty()) {
                RedisOperationUtils.logDebug("No random members found - key: {}, count: {}", key, count);
                return new ArrayList<>();
            }

            List<T> result = new ArrayList<>();
            for (Object item : rawList) {
                result.add(RedisTypeConverter.convertValue(item, clazz));
            }

            RedisOperationUtils.logDebug("Retrieved {} random members from set {}", result.size(), key);
            return result;
        });
    }

    @Override
    public <T> T sPop(String key, Class<T> clazz) {
        return RedisOperationUtils.executeWithExceptionHandling("SPOP", key, () -> {
            RedisOperationUtils.logDebug("Popping random element from set - key: {}, type: {}", 
                key, clazz.getSimpleName());
            Object value = redisTemplate.opsForSet().pop(key);

            if (value == null) {
                RedisOperationUtils.logDebug("Set is empty or key not found: {}", key);
                return null;
            }

            T result = RedisTypeConverter.convertValue(value, clazz);
            RedisOperationUtils.logDebug("Popped random element from set {}: {}", key, result);
            return result;
        });
    }
}
