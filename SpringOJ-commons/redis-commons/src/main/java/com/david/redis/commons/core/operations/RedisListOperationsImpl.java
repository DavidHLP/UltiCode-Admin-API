package com.david.redis.commons.core.operations;

import com.david.log.commons.core.LogUtils;
import com.david.redis.commons.core.operations.interfaces.RedisListOperations;
import com.david.redis.commons.core.operations.support.AbstractRedisOperations;
import com.david.redis.commons.core.operations.support.RedisLoggerHelper;
import com.david.redis.commons.core.operations.support.RedisOperationExecutor;
import com.david.redis.commons.core.operations.support.RedisResultProcessor;
import com.david.redis.commons.core.transaction.RedisTransactionManager;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * Redis List类型操作实现类
 *
 * <p>实现所有List类型的Redis操作方法，继承抽象基类以复用通用逻辑
 *
 * @author David
 */
public class RedisListOperationsImpl extends AbstractRedisOperations
        implements RedisListOperations {

    public RedisListOperationsImpl(
            RedisTemplate<String, Object> redisTemplate,
            RedisTransactionManager transactionManager,
            RedisOperationExecutor executor,
            RedisResultProcessor resultProcessor,
            RedisLoggerHelper loggerHelper,
            LogUtils logUtils) {
        super(redisTemplate, transactionManager, executor, resultProcessor, loggerHelper, logUtils);
    }

    @Override
    protected String getOperationType() {
        return "LIST";
    }

    @Override
    public Long lPush(String key, Object... values) {
        return executeOperation(
                "LPUSH",
                key,
                () -> {
                    return redisTemplate.opsForList().leftPushAll(key, values);
                });
    }

    @Override
    public Long rPush(String key, Object... values) {
        return executeOperation(
                "RPUSH",
                key,
                () -> {
                    return redisTemplate.opsForList().rightPushAll(key, values);
                });
    }

    @Override
    public <T> T lPop(String key, Class<T> clazz) {
        return executeOperation(
                "LPOP",
                key,
                () -> {
                    Object value = redisTemplate.opsForList().leftPop(key);
                    return resultProcessor.convertSingle(value, clazz);
                });
    }

    @Override
    public <T> T rPop(String key, Class<T> clazz) {
        return executeOperation(
                "RPOP",
                key,
                () -> {
                    Object value = redisTemplate.opsForList().rightPop(key);
                    return resultProcessor.convertSingle(value, clazz);
                });
    }

    @Override
    public <T> List<T> lRange(String key, long start, long end, Class<T> clazz) {
        return executeOperation(
                "LRANGE",
                key,
                () -> {
                    List<Object> rawList = redisTemplate.opsForList().range(key, start, end);
                    return resultProcessor.convertList(rawList, clazz);
                });
    }

    @Override
    public Long lSize(String key) {
        return executeOperation(
                "LLEN",
                key,
                () -> {
                    return redisTemplate.opsForList().size(key);
                });
    }

    @Override
    public <T> T lIndex(String key, long index, Class<T> clazz) {
        return executeOperation(
                "LINDEX",
                key,
                () -> {
                    Object value = redisTemplate.opsForList().index(key, index);
                    return resultProcessor.convertSingle(value, clazz);
                });
    }

    @Override
    public void lSet(String key, long index, Object value) {
        executeOperation(
                "LSET",
                key,
                () -> {
                    redisTemplate.opsForList().set(key, index, value);
                    return null;
                });
    }
}
