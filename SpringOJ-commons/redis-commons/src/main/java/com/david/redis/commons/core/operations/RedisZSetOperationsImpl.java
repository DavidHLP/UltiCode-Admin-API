package com.david.redis.commons.core.operations;

import com.david.redis.commons.core.transaction.RedisTransactionManager;
import com.david.redis.commons.core.utils.RedisOperationUtils;
import com.david.redis.commons.core.utils.RedisTypeConverter;
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
 * <p>实现所有ZSet类型的Redis操作方法
 * 
 * @author David
 */
@Slf4j
public class RedisZSetOperationsImpl implements RedisZSetOperations {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTransactionManager transactionManager;

    public RedisZSetOperationsImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.transactionManager = null;
    }

    public RedisZSetOperationsImpl(RedisTemplate<String, Object> redisTemplate, 
                                 RedisTransactionManager transactionManager) {
        this.redisTemplate = redisTemplate;
        this.transactionManager = transactionManager;
    }

    @Override
    public Boolean zAdd(String key, Object value, double score) {
        return RedisOperationUtils.executeWithExceptionHandling("ZADD", key, 
            new Object[]{value, score}, () -> {
            RedisOperationUtils.logDebug("Adding member to sorted set - key: {}, value: {}, score: {}", 
                key, value, score);
            Boolean result = redisTemplate.opsForZSet().add(key, value, score);
            RedisOperationUtils.logDebug("ZSet add result - key: {}, value: {}, isNew: {}", key, value, result);
            return result;
        });
    }

    @Override
    public Long zAdd(String key, Map<Object, Double> scoreValueMap) {
        return RedisOperationUtils.executeWithExceptionHandling("ZADD", key, 
            new Object[]{scoreValueMap}, () -> {
            RedisOperationUtils.logDebug("Adding multiple members to sorted set - key: {}, members count: {}", 
                key, scoreValueMap.size());

            // 转换为Spring Data Redis需要的格式
            Set<ZSetOperations.TypedTuple<Object>> tuples = new HashSet<>();

            for (Map.Entry<Object, Double> entry : scoreValueMap.entrySet()) {
                tuples.add(ZSetOperations.TypedTuple.of(entry.getKey(), entry.getValue()));
            }

            Long result = redisTemplate.opsForZSet().add(key, tuples);
            RedisOperationUtils.logDebug("ZSet batch add result - key: {}, new members: {}", key, result);
            return result;
        });
    }

    @Override
    public Long zRem(String key, Object... values) {
        return RedisOperationUtils.executeWithExceptionHandling("ZREM", key, 
            new Object[]{values}, () -> {
            RedisOperationUtils.logDebug("Removing members from sorted set - key: {}, values count: {}", 
                key, values.length);
            Long result = redisTemplate.opsForZSet().remove(key, values);
            RedisOperationUtils.logDebug("ZSet remove result - key: {}, removed count: {}", key, result);
            return result;
        });
    }

    @Override
    public <T> Set<T> zRange(String key, long start, long end, Class<T> clazz) {
        return RedisOperationUtils.executeWithExceptionHandling("ZRANGE", key, 
            new Object[]{start, end}, () -> {
            RedisOperationUtils.logDebug("Getting sorted set range - key: {}, start: {}, end: {}, type: {}", 
                key, start, end, clazz.getSimpleName());
            Set<Object> rawSet = redisTemplate.opsForZSet().range(key, start, end);

            if (rawSet == null || rawSet.isEmpty()) {
                RedisOperationUtils.logDebug("ZSet range is empty - key: {}, start: {}, end: {}", key, start, end);
                return new LinkedHashSet<>();
            }

            Set<T> result = new LinkedHashSet<>();
            for (Object item : rawSet) {
                result.add(RedisTypeConverter.convertValue(item, clazz));
            }

            RedisOperationUtils.logDebug("Retrieved {} members from sorted set range - key: {}", result.size(), key);
            return result;
        });
    }

    @Override
    public <T> Set<ZSetOperations.TypedTuple<T>> zRangeWithScores(String key, long start, long end, Class<T> clazz) {
        return RedisOperationUtils.executeWithExceptionHandling("ZRANGE", key, 
            new Object[]{start, end}, () -> {
            RedisOperationUtils.logDebug("Getting sorted set range with scores - key: {}, start: {}, end: {}, type: {}", 
                key, start, end, clazz.getSimpleName());
            Set<ZSetOperations.TypedTuple<Object>> rawSet = 
                redisTemplate.opsForZSet().rangeWithScores(key, start, end);

            if (rawSet == null || rawSet.isEmpty()) {
                RedisOperationUtils.logDebug("ZSet range with scores is empty - key: {}, start: {}, end: {}", 
                    key, start, end);
                return new LinkedHashSet<>();
            }

            Set<ZSetOperations.TypedTuple<T>> result = new LinkedHashSet<>();

            for (ZSetOperations.TypedTuple<Object> tuple : rawSet) {
                T convertedValue = RedisTypeConverter.convertValue(tuple.getValue(), clazz);
                if (convertedValue != null) {
                    result.add(ZSetOperations.TypedTuple.of(convertedValue, tuple.getScore()));
                }
            }

            RedisOperationUtils.logDebug("Retrieved {} members with scores from sorted set range - key: {}", 
                result.size(), key);
            return result;
        });
    }

    @Override
    public <T> Set<T> zRevRange(String key, long start, long end, Class<T> clazz) {
        return RedisOperationUtils.executeWithExceptionHandling("ZREVRANGE", key, 
            new Object[]{start, end}, () -> {
            RedisOperationUtils.logDebug("Getting sorted set reverse range - key: {}, start: {}, end: {}, type: {}", 
                key, start, end, clazz.getSimpleName());
            Set<Object> rawSet = redisTemplate.opsForZSet().reverseRange(key, start, end);

            if (rawSet == null || rawSet.isEmpty()) {
                RedisOperationUtils.logDebug("ZSet reverse range is empty - key: {}, start: {}, end: {}", 
                    key, start, end);
                return new LinkedHashSet<>();
            }

            Set<T> result = new LinkedHashSet<>();
            for (Object item : rawSet) {
                result.add(RedisTypeConverter.convertValue(item, clazz));
            }

            RedisOperationUtils.logDebug("Retrieved {} members from sorted set reverse range - key: {}", 
                result.size(), key);
            return result;
        });
    }

    @Override
    public Double zScore(String key, Object value) {
        return RedisOperationUtils.executeWithExceptionHandling("ZSCORE", key, 
            new Object[]{value}, () -> {
            RedisOperationUtils.logDebug("Getting member score from sorted set - key: {}, value: {}", key, value);
            Double result = redisTemplate.opsForZSet().score(key, value);
            RedisOperationUtils.logDebug("ZSet score result - key: {}, value: {}, score: {}", key, value, result);
            return result;
        });
    }

    @Override
    public Long zRank(String key, Object value) {
        return RedisOperationUtils.executeWithExceptionHandling("ZRANK", key, 
            new Object[]{value}, () -> {
            RedisOperationUtils.logDebug("Getting member rank from sorted set - key: {}, value: {}", key, value);
            Long result = redisTemplate.opsForZSet().rank(key, value);
            RedisOperationUtils.logDebug("ZSet rank result - key: {}, value: {}, rank: {}", key, value, result);
            return result;
        });
    }

    @Override
    public Long zRevRank(String key, Object value) {
        return RedisOperationUtils.executeWithExceptionHandling("ZREVRANK", key, 
            new Object[]{value}, () -> {
            RedisOperationUtils.logDebug("Getting member reverse rank from sorted set - key: {}, value: {}", key, value);
            Long result = redisTemplate.opsForZSet().reverseRank(key, value);
            RedisOperationUtils.logDebug("ZSet reverse rank result - key: {}, value: {}, rank: {}", key, value, result);
            return result;
        });
    }

    @Override
    public Long zSize(String key) {
        return RedisOperationUtils.executeWithExceptionHandling("ZCARD", key, () -> {
            RedisOperationUtils.logDebug("Getting sorted set size for key: {}", key);
            Long result = redisTemplate.opsForZSet().size(key);
            RedisOperationUtils.logDebug("ZSet size for key {}: {}", key, result);
            return result;
        });
    }

    @Override
    public Long zCount(String key, double min, double max) {
        return RedisOperationUtils.executeWithExceptionHandling("ZCOUNT", key, 
            new Object[]{min, max}, () -> {
            RedisOperationUtils.logDebug("Counting sorted set members by score range - key: {}, min: {}, max: {}", 
                key, min, max);
            Long result = redisTemplate.opsForZSet().count(key, min, max);
            RedisOperationUtils.logDebug("ZSet count result - key: {}, min: {}, max: {}, count: {}", 
                key, min, max, result);
            return result;
        });
    }

    @Override
    public Double zIncrBy(String key, Object value, double increment) {
        return RedisOperationUtils.executeWithExceptionHandling("ZINCRBY", key, 
            new Object[]{value, increment}, () -> {
            RedisOperationUtils.logDebug("Incrementing sorted set member score - key: {}, value: {}, increment: {}", 
                key, value, increment);
            Double result = redisTemplate.opsForZSet().incrementScore(key, value, increment);
            RedisOperationUtils.logDebug("ZSet increment result - key: {}, value: {}, new score: {}", 
                key, value, result);
            return result;
        });
    }

    @Override
    public <T> Set<T> zRangeByScore(String key, double min, double max, Class<T> clazz) {
        return RedisOperationUtils.executeWithExceptionHandling("ZRANGEBYSCORE", key, 
            new Object[]{min, max}, () -> {
            RedisOperationUtils.logDebug("Getting sorted set members by score range - key: {}, min: {}, max: {}, type: {}", 
                key, min, max, clazz.getSimpleName());
            Set<Object> rawSet = redisTemplate.opsForZSet().rangeByScore(key, min, max);

            if (rawSet == null || rawSet.isEmpty()) {
                RedisOperationUtils.logDebug("ZSet range by score is empty - key: {}, min: {}, max: {}", key, min, max);
                return new LinkedHashSet<>();
            }

            Set<T> result = new LinkedHashSet<>();
            for (Object item : rawSet) {
                result.add(RedisTypeConverter.convertValue(item, clazz));
            }

            RedisOperationUtils.logDebug("Retrieved {} members from sorted set by score range - key: {}", 
                result.size(), key);
            return result;
        });
    }

    @Override
    public Long zRemRangeByScore(String key, double min, double max) {
        return RedisOperationUtils.executeWithExceptionHandling("ZREMRANGEBYSCORE", key, 
            new Object[]{min, max}, () -> {
            RedisOperationUtils.logDebug("Removing sorted set members by score range - key: {}, min: {}, max: {}", 
                key, min, max);
            Long result = redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
            RedisOperationUtils.logDebug("ZSet remove by score result - key: {}, min: {}, max: {}, removed: {}", 
                key, min, max, result);
            return result;
        });
    }

    @Override
    public Long zRemRangeByRank(String key, long start, long end) {
        return RedisOperationUtils.executeWithExceptionHandling("ZREMRANGEBYRANK", key, 
            new Object[]{start, end}, () -> {
            RedisOperationUtils.logDebug("Removing sorted set members by rank range - key: {}, start: {}, end: {}", 
                key, start, end);
            Long result = redisTemplate.opsForZSet().removeRange(key, start, end);
            RedisOperationUtils.logDebug("ZSet remove by rank result - key: {}, start: {}, end: {}, removed: {}", 
                key, start, end, result);
            return result;
        });
    }
}
