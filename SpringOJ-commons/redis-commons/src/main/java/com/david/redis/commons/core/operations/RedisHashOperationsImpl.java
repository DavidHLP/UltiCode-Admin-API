package com.david.redis.commons.core.operations;

import com.david.redis.commons.core.operations.interfaces.RedisHashOperations;
import com.david.redis.commons.core.operations.support.AbstractRedisOperations;
import com.david.redis.commons.core.operations.support.RedisLoggerHelper;
import com.david.redis.commons.core.operations.support.RedisOperationExecutor;
import com.david.redis.commons.core.operations.support.RedisResultProcessor;
import com.david.redis.commons.core.transaction.RedisTransactionManager;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;

/**
 * Redis Hash类型操作实现类
 *
 * <p>实现所有Hash类型的Redis操作方法
 *
 * @author David
 */
public class RedisHashOperationsImpl extends AbstractRedisOperations
        implements RedisHashOperations {

    public RedisHashOperationsImpl(
            RedisTemplate<String, Object> redisTemplate,
            RedisTransactionManager transactionManager,
            RedisOperationExecutor executor,
            RedisResultProcessor resultProcessor,
            RedisLoggerHelper loggerHelper) {
        super(redisTemplate, transactionManager, executor, resultProcessor, loggerHelper);
    }

    @Override
    protected String getOperationType() {
        return "HASH";
    }

    @Override
    public void hSet(String key, String hashKey, Object value) {
        executeOperation(
                "HSET",
                key,
                new Object[] {hashKey, value},
                () -> {
                    redisTemplate.opsForHash().put(key, hashKey, value);
                });
    }

    @Override
    public <T> T hGet(String key, String hashKey, Class<T> clazz) {
        return executeOperation(
                "HGET",
                key,
                new Object[] {hashKey},
                () -> {
                    Object value = redisTemplate.opsForHash().get(key, hashKey);
                    return resultProcessor.convertSingle(value, clazz);
                });
    }

    @Override
    public String hGetString(String key, String hashKey) {
        return hGet(key, hashKey, String.class);
    }

    @Override
    public Map<String, Object> hGetAll(String key) {
        return executeOperation(
                "HGETALL",
                key,
                () -> {
                    Map<Object, Object> rawMap = redisTemplate.opsForHash().entries(key);
                    return resultProcessor.convertMapKeysToString(rawMap);
                });
    }

    @Override
    public Long hDelete(String key, String... hashKeys) {
        return executeOperation(
                "HDEL",
                key,
                new Object[] {hashKeys},
                () -> {
                    return redisTemplate.opsForHash().delete(key, (Object[]) hashKeys);
                });
    }

    @Override
    public Boolean hExists(String key, String hashKey) {
        return executeOperation(
                "HEXISTS",
                key,
                new Object[] {hashKey},
                () -> {
                    return redisTemplate.opsForHash().hasKey(key, hashKey);
                });
    }

    @Override
    public Long hSize(String key) {
        return executeOperation(
                "HLEN",
                key,
                () -> {
                    return redisTemplate.opsForHash().size(key);
                });
    }

    @Override
    public Set<String> hKeys(String key) {
        return executeOperation(
                "HKEYS",
                key,
                () -> {
                    Set<Object> rawKeys = redisTemplate.opsForHash().keys(key);
                    return resultProcessor.convertToStringSet(rawKeys);
                });
    }

    @Override
    public List<Object> hValues(String key) {
        return executeOperation(
                "HVALS",
                key,
                () -> {
                    return redisTemplate.opsForHash().values(key);
                });
    }

    @Override
    public Long hIncrBy(String key, String hashKey, long increment) {
        return executeOperation(
                "HINCRBY",
                key,
                new Object[] {hashKey, increment},
                () -> {
                    return redisTemplate.opsForHash().increment(key, hashKey, increment);
                });
    }

    @Override
    public Double hIncrByFloat(String key, String hashKey, double increment) {
        return executeOperation(
                "HINCRBYFLOAT",
                key,
                new Object[] {hashKey, increment},
                () -> {
                    return redisTemplate.opsForHash().increment(key, hashKey, increment);
                });
    }

    @Override
    public void hMSet(String key, Map<String, Object> map) {
        executeOperation(
                "HMSET",
                key,
                new Object[] {map},
                () -> {
                    redisTemplate.opsForHash().putAll(key, map);
                });
    }

    @Override
    public List<Object> hMGet(String key, String... hashKeys) {
        return executeOperation(
                "HMGET",
                key,
                new Object[] {Arrays.toString(hashKeys)},
                () -> redisTemplate.opsForHash().multiGet(key, Arrays.asList(hashKeys)));
    }
}
