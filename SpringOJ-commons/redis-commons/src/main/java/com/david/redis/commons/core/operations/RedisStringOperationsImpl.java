package com.david.redis.commons.core.operations;

import com.david.log.commons.LogUtils;
import com.david.redis.commons.core.operations.interfaces.RedisStringOperations;
import com.david.redis.commons.core.operations.abstracts.AbstractRedisOperations;
import com.david.redis.commons.core.operations.support.RedisOperationExecutor;
import com.david.redis.commons.core.operations.enums.RedisOperationType;
import com.david.redis.commons.core.operations.support.RedisResultProcessor;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis String类型操作实现类
 *
 * <p>实现所有String类型的Redis操作方法
 *
 * @author David
 */
@Component
public class RedisStringOperationsImpl extends AbstractRedisOperations
        implements RedisStringOperations {

    public RedisStringOperationsImpl(
            RedisTemplate<String, Object> redisTemplate,
            RedisOperationExecutor executor,
            RedisResultProcessor resultProcessor) {
        super(redisTemplate, executor, resultProcessor);
    }

    @Override
    public <T> void set(String key, T value) {
        executeVoidOperation(
                RedisOperationType.SET,
                key,
                value,
                () -> redisTemplate.opsForValue().set(key, value));
    }

    @Override
    public <T> void set(String key, T value, Duration timeout) {
        executeVoidOperation(
                RedisOperationType.SETEX,
                key,
                new Object[] {value, timeout},
                () ->
                        redisTemplate
                                .opsForValue()
                                .set(key, value, timeout.toMillis(), TimeUnit.MILLISECONDS));
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        return executeOperation(
                RedisOperationType.GET, key, clazz, () -> redisTemplate.opsForValue().get(key));
    }

    @Override
    public String get(String key) {
        return executeStringOperation(key, () -> redisTemplate.opsForValue().get(key));
    }

    @Override
    public Long delete(String... keys) {
        return executeLongOperation(
                Arrays.toString(keys),
                keys,
                () -> {
                    Long result = redisTemplate.delete(Arrays.asList(keys));
                    return resultProcessor.handleNullLong(result, 0L);
                });
    }

    @Override
    public Boolean expire(String key, Duration timeout) {
        return executeBooleanOperation(
                key,
                timeout,
                () -> redisTemplate.expire(key, timeout.toMillis(), TimeUnit.MILLISECONDS));
    }

    @Override
    public Boolean hasKey(String key) {
        return executeBooleanOperation(
                RedisOperationType.EXISTS, key, () -> redisTemplate.hasKey(key));
    }

    @Override
    public Long getExpire(String key) {
        return executeLongOperation(
                RedisOperationType.TTL, key, () -> redisTemplate.getExpire(key, TimeUnit.SECONDS));
    }

    @Override
    public Set<String> scanKeys(String pattern) {
        return executeOperation(RedisOperationType.SCAN, pattern, () -> performScanKeys(pattern));
    }

    /** 内部使用的keys方法，用于回退场景 */
    private Set<String> keys(String pattern) {
        return executeOperation(
                RedisOperationType.KEYS, pattern, () -> redisTemplate.keys(pattern));
    }

    /**
     * 执行SCAN操作获取键
     *
     * @param pattern 匹配模式
     * @return 匹配的键集合
     */
    private Set<String> performScanKeys(String pattern) {
        Set<String> result = new LinkedHashSet<>();
        RedisConnectionFactory factory = redisTemplate.getConnectionFactory();

        if (factory == null) {
            LogUtils.error("Redis扫描键失败，原因：RedisConnectionFactory为空，已回退到RedisTemplate.keys()方法");
            return keys(pattern);
        }

        try (RedisConnection connection = factory.getConnection()) {
            ScanOptions options = ScanOptions.scanOptions().match(pattern).count(1000).build();
            RedisKeyCommands keyCommands = connection.keyCommands();

            try (Cursor<byte[]> cursor = keyCommands.scan(options)) {
                while (cursor.hasNext()) {
                    result.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            }
            return result;

        } catch (InvalidDataAccessApiUsageException e) {
            LogUtils.error("按模式扫描Redis键失败，已回退到RedisTemplate的keys()方法。模式: " + pattern, e);
            return fallbackToKeysWithConnection(factory, pattern);
        } catch (Exception e) {
            LogUtils.error("按模式扫描Redis键失败，已回退到RedisTemplate的keys()方法。模式: " + pattern, e);
            return keys(pattern);
        }
    }

    /** 使用KEYS命令作为回退方案 */
    private Set<String> fallbackToKeysCommand(RedisConnection connection, String pattern) {
        Set<String> result = new LinkedHashSet<>();
        Set<byte[]> raw = connection.keyCommands().keys(pattern.getBytes(StandardCharsets.UTF_8));
        if (raw != null) {
            for (byte[] b : raw) {
                result.add(new String(b, StandardCharsets.UTF_8));
            }
        }
        return result;
    }

    /** 使用独立连接执行KEYS命令 */
    private Set<String> fallbackToKeysWithConnection(
            RedisConnectionFactory factory, String pattern) {
        try (RedisConnection connection = factory.getConnection()) {
            return fallbackToKeysCommand(connection, pattern);
        } catch (Exception e) {
            LogUtils.error("回退到KEYS方法在单机连接上失败", e);
            return keys(pattern);
        }
    }

    @Override
    public <T> List<T> multiGet(java.util.List<String> keys, Class<T> clazz) {
        String keyStr = keys != null && !keys.isEmpty() ? keys.get(0) : "batch";
        return executeOperation(
                        RedisOperationType.MGET,
                        keyStr,
                        keys,
                        () -> {
                            if (keys == null || keys.isEmpty()) {
                                throw new IllegalArgumentException(
                                        "Keys list cannot be null or empty");
                            }
                            java.util.List<Object> rawResults =
                                    redisTemplate.opsForValue().multiGet(keys);
                            return resultProcessor.convertList(rawResults, clazz);
                        });
    }

    @Override
    public <T> void multiSet(java.util.Map<String, T> keyValues) {
        String keyStr =
                keyValues != null && !keyValues.isEmpty()
                        ? keyValues.keySet().iterator().next()
                        : "batch";
        executeVoidOperation(
                RedisOperationType.MSET,
                keyStr,
                keyValues,
                () -> {
                    if (keyValues == null || keyValues.isEmpty()) {
                        throw new IllegalArgumentException("Key-value map cannot be null or empty");
                    }
                    redisTemplate.opsForValue().multiSet(keyValues);
                });
    }
}
