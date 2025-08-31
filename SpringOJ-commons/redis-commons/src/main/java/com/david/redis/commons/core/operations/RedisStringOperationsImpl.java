package com.david.redis.commons.core.operations;

import com.david.log.commons.LogUtils;
import com.david.redis.commons.core.operations.interfaces.RedisStringOperations;
import com.david.redis.commons.core.operations.support.AbstractRedisOperations;
import com.david.redis.commons.core.operations.support.RedisOperationExecutor;
import com.david.redis.commons.core.operations.support.RedisOperationType;
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
            RedisResultProcessor resultProcessor,
            LogUtils logUtils) {
        super(redisTemplate, executor, resultProcessor, logUtils);
    }

    @Override
    public void set(String key, Object value) {
        executeVoidOperation(
                RedisOperationType.SET,
                key,
                value,
                () -> redisTemplate.opsForValue().set(key, value));
    }

    @Override
    public void set(String key, Object value, Duration timeout) {
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
    public String getString(String key) {
        return executeStringOperation(key, () -> redisTemplate.opsForValue().get(key));
    }

    @Override
    public Boolean delete(String key) {
        return executeBooleanOperation(
                RedisOperationType.DEL, key, () -> redisTemplate.delete(key));
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
    public Set<String> keys(String pattern) {
        return executeOperation(
                RedisOperationType.KEYS, pattern, Set.class, () -> redisTemplate.keys(pattern));
    }

    @Override
    public Set<String> scanKeys(String pattern) {
        return executeOperation(
                RedisOperationType.SCAN, pattern, Set.class, () -> performScanKeys(pattern));
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
            // 优化后的写法
            LogUtils.business()
                    .auto()
                    .message("Redis扫描键失败，原因：RedisConnectionFactory为空，已回退到RedisTemplate.keys()方法")
                    .info();
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
            LogUtils.business()
                    .trace(
                            "redis_scan_keys",
                            "scan_not_allowed",
                            "SCAN not allowed in current mode, fallback to KEYS",
                            "pattern: " + pattern);
            return fallbackToKeysWithConnection(factory, pattern);
        } catch (Exception e) {
            logUtils.exception()
                    .business(
                            "redis_scan_keys_failed",
                            e,
                            "Failed to scan keys by pattern",
                            "pattern: " + pattern);
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
        } catch (Exception ex) {
            logUtils.exception()
                    .business(
                            "redis_keys_fallback_failed",
                            ex,
                            "Fallback KEYS failed on standalone connection");
            return keys(pattern);
        }
    }

    @Override
    public java.util.List<Object> multiGet(java.util.List<String> keys) {
        String keyStr = keys != null && !keys.isEmpty() ? keys.get(0) : "batch";
        return executeOperation(
                RedisOperationType.MGET,
                keyStr,
                keys,
                java.util.List.class,
                () -> {
                    if (keys == null || keys.isEmpty()) {
                        throw new IllegalArgumentException("Keys list cannot be null or empty");
                    }
                    return redisTemplate.opsForValue().multiGet(keys);
                });
    }

    @Override
    public void multiSet(java.util.Map<String, Object> keyValues) {
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

    @Override
    public Boolean expire(String key, long timeout, java.util.concurrent.TimeUnit timeUnit) {
        return executeBooleanOperation(
                key,
                new Object[] {timeout, timeUnit},
                () -> {
                    if (key == null || key.trim().isEmpty()) {
                        throw new IllegalArgumentException("Key cannot be null or empty");
                    }
                    if (timeout <= 0) {
                        throw new IllegalArgumentException("Timeout must be positive");
                    }
                    if (timeUnit == null) {
                        throw new IllegalArgumentException("TimeUnit cannot be null");
                    }
                    return redisTemplate.expire(key, timeout, timeUnit);
                });
    }
}
