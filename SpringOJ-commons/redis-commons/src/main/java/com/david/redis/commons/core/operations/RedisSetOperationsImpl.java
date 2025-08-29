package com.david.redis.commons.core.operations;

import com.david.log.commons.core.LogUtils;
import com.david.redis.commons.core.operations.interfaces.RedisSetOperations;
import com.david.redis.commons.core.operations.support.AbstractRedisOperations;
import com.david.redis.commons.core.operations.support.RedisLoggerHelper;
import com.david.redis.commons.core.operations.support.RedisOperationExecutor;
import com.david.redis.commons.core.operations.support.RedisResultProcessor;
import com.david.redis.commons.core.transaction.RedisTransactionManager;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Set;

/**
 * Redis Set类型操作实现类
 * 
 * <p>
 * 实现所有Set类型的Redis操作方法，继承抽象基类以复用通用逻辑
 * 
 * @author David
 */
public class RedisSetOperationsImpl extends AbstractRedisOperations implements RedisSetOperations {


    public RedisSetOperationsImpl(RedisTemplate<String, Object> redisTemplate, RedisTransactionManager transactionManager, RedisOperationExecutor executor, RedisResultProcessor resultProcessor, RedisLoggerHelper loggerHelper, LogUtils logUtils) {
        super(redisTemplate, transactionManager, executor, resultProcessor, loggerHelper, logUtils);
    }

    @Override
    protected String getOperationType() {
        return "SET";
    }

    @Override
    public Long sAdd(String key, Object... values) {
        return executeOperation("SADD", key, () -> {
            return redisTemplate.opsForSet().add(key, values);
        });
    }

    @Override
    public Long sRem(String key, Object... values) {
        return executeOperation("SREM", key, () -> {
            return redisTemplate.opsForSet().remove(key, values);
        });
    }

    @Override
    public <T> Set<T> sMembers(String key, Class<T> clazz) {
        return executeOperation("SMEMBERS", key, () -> {
            Set<Object> rawSet = redisTemplate.opsForSet().members(key);
            return resultProcessor.convertSet(rawSet, clazz);
        });
    }

    @Override
    public Boolean sIsMember(String key, Object value) {
        return executeOperation("SISMEMBER", key, () -> {
            return redisTemplate.opsForSet().isMember(key, value);
        });
    }

    @Override
    public Long sSize(String key) {
        return executeOperation("SCARD", key, () -> {
            return redisTemplate.opsForSet().size(key);
        });
    }

    @Override
    public <T> T sRandomMember(String key, Class<T> clazz) {
        return executeOperation("SRANDMEMBER", key, () -> {
            Object value = redisTemplate.opsForSet().randomMember(key);
            return resultProcessor.convertSingle(value, clazz);
        });
    }

    @Override
    public <T> List<T> sRandomMembers(String key, long count, Class<T> clazz) {
        return executeOperation("SRANDMEMBER", key, () -> {
            List<Object> rawList = redisTemplate.opsForSet().randomMembers(key, count);
            return resultProcessor.convertList(rawList, clazz);
        });
    }

    @Override
    public <T> T sPop(String key, Class<T> clazz) {
        return executeOperation("SPOP", key, () -> {
            Object value = redisTemplate.opsForSet().pop(key);
            return resultProcessor.convertSingle(value, clazz);
        });
    }
}
