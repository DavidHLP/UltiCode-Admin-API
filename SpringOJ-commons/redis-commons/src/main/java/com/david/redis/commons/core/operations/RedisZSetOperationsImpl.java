package com.david.redis.commons.core.operations;

import com.david.redis.commons.core.operations.interfaces.RedisZSetOperations;
import com.david.redis.commons.core.operations.support.AbstractRedisOperations;
import com.david.redis.commons.core.operations.support.RedisLoggerHelper;
import com.david.redis.commons.core.operations.support.RedisOperationExecutor;
import com.david.redis.commons.core.operations.support.RedisResultProcessor;
import com.david.redis.commons.core.transaction.RedisTransactionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Redis ZSet类型操作实现类
 * 
 * <p>
 * 实现所有ZSet类型的Redis操作方法
 * 
 * @author David
 */
@Slf4j
public class RedisZSetOperationsImpl extends AbstractRedisOperations implements RedisZSetOperations {

    public RedisZSetOperationsImpl(RedisTemplate<String, Object> redisTemplate,
            RedisTransactionManager transactionManager,
            RedisOperationExecutor executor,
            RedisResultProcessor resultProcessor,
            RedisLoggerHelper loggerHelper) {
        super(redisTemplate, transactionManager, executor, resultProcessor, loggerHelper);
    }

    @Override
    protected String getOperationType() {
        return "ZSET";
    }

    @Override
    public Boolean zAdd(String key, Object value, double score) {
        return executeOperation("ZADD", key, new Object[] { value, score }, () -> {
            return redisTemplate.opsForZSet().add(key, value, score);
        });
    }

    @Override
    public Long zAdd(String key, Map<Object, Double> scoreValueMap) {
        return executeOperation("ZADD", key, new Object[] { scoreValueMap }, () -> {
            // 转换为Spring Data Redis需要的格式
            Set<ZSetOperations.TypedTuple<Object>> tuples = new HashSet<>();

            for (Map.Entry<Object, Double> entry : scoreValueMap.entrySet()) {
                tuples.add(ZSetOperations.TypedTuple.of(entry.getKey(), entry.getValue()));
            }

            return redisTemplate.opsForZSet().add(key, tuples);
        });
    }

    @Override
    public Long zRem(String key, Object... values) {
        return executeOperation("ZREM", key, new Object[] { values }, () -> {
            return redisTemplate.opsForZSet().remove(key, values);
        });
    }

    @Override
    public <T> Set<T> zRange(String key, long start, long end, Class<T> clazz) {
        return executeOperation("ZRANGE", key, new Object[] { start, end }, () -> {
            Set<Object> rawSet = redisTemplate.opsForZSet().range(key, start, end);

            if (rawSet == null || rawSet.isEmpty()) {
                return new LinkedHashSet<>();
            }

            Set<T> result = new LinkedHashSet<>();
            for (Object item : rawSet) {
                result.add(resultProcessor.convertSingle(item, clazz));
            }

            return result;
        });
    }

    @Override
    public <T> Set<ZSetOperations.TypedTuple<T>> zRangeWithScores(String key, long start, long end, Class<T> clazz) {
        return executeOperation("ZRANGE", key, new Object[] { start, end }, () -> {
            Set<ZSetOperations.TypedTuple<Object>> rawSet = redisTemplate.opsForZSet().rangeWithScores(key, start, end);

            if (rawSet == null || rawSet.isEmpty()) {
                return new LinkedHashSet<>();
            }

            Set<ZSetOperations.TypedTuple<T>> result = new LinkedHashSet<>();

            for (ZSetOperations.TypedTuple<Object> tuple : rawSet) {
                T convertedValue = resultProcessor.convertSingle(tuple.getValue(), clazz);
                if (convertedValue != null) {
                    result.add(ZSetOperations.TypedTuple.of(convertedValue, tuple.getScore()));
                }
            }

            return result;
        });
    }

    @Override
    public <T> Set<T> zRevRange(String key, long start, long end, Class<T> clazz) {
        return executeOperation("ZREVRANGE", key, new Object[] { start, end }, () -> {
            Set<Object> rawSet = redisTemplate.opsForZSet().reverseRange(key, start, end);

            if (rawSet == null || rawSet.isEmpty()) {
                return new LinkedHashSet<>();
            }

            Set<T> result = new LinkedHashSet<>();
            for (Object item : rawSet) {
                result.add(resultProcessor.convertSingle(item, clazz));
            }

            return result;
        });
    }

    @Override
    public Double zScore(String key, Object value) {
        return executeOperation("ZSCORE", key, new Object[] { value }, () -> {
            return redisTemplate.opsForZSet().score(key, value);
        });
    }

    @Override
    public Long zRank(String key, Object value) {
        return executeOperation("ZRANK", key, new Object[] { value }, () -> {
            return redisTemplate.opsForZSet().rank(key, value);
        });
    }

    @Override
    public Long zRevRank(String key, Object value) {
        return executeOperation("ZREVRANK", key, new Object[] { value }, () -> {
            return redisTemplate.opsForZSet().reverseRank(key, value);
        });
    }

    @Override
    public Long zSize(String key) {
        return executeOperation("ZCARD", key, () -> {
            return redisTemplate.opsForZSet().size(key);
        });
    }

    @Override
    public Long zCount(String key, double min, double max) {
        return executeOperation("ZCOUNT", key, new Object[] { min, max }, () -> {
            return redisTemplate.opsForZSet().count(key, min, max);
        });
    }

    @Override
    public Double zIncrBy(String key, Object value, double increment) {
        return executeOperation("ZINCRBY", key, new Object[] { value, increment }, () -> {
            return redisTemplate.opsForZSet().incrementScore(key, value, increment);
        });
    }

    @Override
    public <T> Set<T> zRangeByScore(String key, double min, double max, Class<T> clazz) {
        return executeOperation("ZRANGEBYSCORE", key, new Object[] { min, max }, () -> {
            Set<Object> rawSet = redisTemplate.opsForZSet().rangeByScore(key, min, max);

            if (rawSet == null || rawSet.isEmpty()) {
                return new LinkedHashSet<>();
            }

            Set<T> result = new LinkedHashSet<>();
            for (Object item : rawSet) {
                result.add(resultProcessor.convertSingle(item, clazz));
            }

            return result;
        });
    }

    @Override
    public Long zRemRangeByScore(String key, double min, double max) {
        return executeOperation("ZREMRANGEBYSCORE", key, new Object[] { min, max }, () -> {
            return redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
        });
    }

    @Override
    public Long zRemRangeByRank(String key, long start, long end) {
        return executeOperation("ZREMRANGEBYRANK", key, new Object[] { start, end }, () -> {
            return redisTemplate.opsForZSet().removeRange(key, start, end);
        });
    }
}
