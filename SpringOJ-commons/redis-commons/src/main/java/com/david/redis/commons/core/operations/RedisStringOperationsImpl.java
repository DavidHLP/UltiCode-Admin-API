package com.david.redis.commons.core.operations;

import com.david.redis.commons.core.transaction.RedisTransactionManager;
import com.david.redis.commons.core.utils.RedisOperationUtils;
import com.david.redis.commons.core.utils.RedisTypeConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

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
@Slf4j
public class RedisStringOperationsImpl implements RedisStringOperations {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTransactionManager transactionManager;

    public RedisStringOperationsImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.transactionManager = null;
    }

    public RedisStringOperationsImpl(RedisTemplate<String, Object> redisTemplate, 
                                   RedisTransactionManager transactionManager) {
        this.redisTemplate = redisTemplate;
        this.transactionManager = transactionManager;
    }

    @Override
    public void set(String key, Object value) {
        RedisOperationUtils.executeWithExceptionHandling("SET", key, () -> {
            RedisOperationUtils.logDebug("Setting key: {}, value: {}", key, value);
            RedisOperationUtils.recordOperation(transactionManager, "SET " + key);
            redisTemplate.opsForValue().set(key, value);
        });
    }

    @Override
    public void set(String key, Object value, Duration timeout) {
        RedisOperationUtils.executeWithExceptionHandling("SETEX", key, 
            new Object[]{value, timeout}, () -> {
            RedisOperationUtils.logDebug("Setting key: {}, value: {}, timeout: {}", key, value, timeout);
            RedisOperationUtils.recordOperation(transactionManager, "SETEX " + key + " " + timeout.getSeconds());
            redisTemplate.opsForValue().set(key, value, timeout.toMillis(), TimeUnit.MILLISECONDS);
            return null;
        });
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        return RedisOperationUtils.executeWithExceptionHandling("GET", key, () -> {
            RedisOperationUtils.logDebug("Getting key: {}, expected type: {}", key, clazz.getSimpleName());
            Object value = redisTemplate.opsForValue().get(key);

            if (value == null) {
                RedisOperationUtils.logDebug("Key not found: {}", key);
                return null;
            }

            return RedisTypeConverter.convertValue(value, clazz);
        });
    }

    @Override
    public String getString(String key) {
        return get(key, String.class);
    }

    @Override
    public Boolean delete(String key) {
        return RedisOperationUtils.executeWithExceptionHandling("DEL", key, () -> {
            RedisOperationUtils.logDebug("Deleting key: {}", key);
            Boolean result = redisTemplate.delete(key);
            RedisOperationUtils.logDebug("Delete result for key {}: {}", key, result);
            return result;
        });
    }

    @Override
    public Long delete(String... keys) {
        return RedisOperationUtils.executeWithExceptionHandling("DEL", Arrays.toString(keys), () -> {
            RedisOperationUtils.logDebug("Deleting keys: {}", Arrays.toString(keys));
            Long result = redisTemplate.delete(Arrays.asList(keys));
            Long normalizedResult = result != null ? result : 0L;
            RedisOperationUtils.logDebug("Delete result for keys {}: {}", Arrays.toString(keys), normalizedResult);
            return normalizedResult;
        });
    }

    @Override
    public Boolean expire(String key, Duration timeout) {
        return RedisOperationUtils.executeWithExceptionHandling("EXPIRE", key, 
            new Object[]{timeout}, () -> {
            RedisOperationUtils.logDebug("Setting expiration for key: {}, timeout: {}", key, timeout);
            Boolean result = redisTemplate.expire(key, timeout.toMillis(), TimeUnit.MILLISECONDS);
            RedisOperationUtils.logDebug("Expire result for key {}: {}", key, result);
            return result;
        });
    }

    @Override
    public Boolean hasKey(String key) {
        return RedisOperationUtils.executeWithExceptionHandling("EXISTS", key, () -> {
            RedisOperationUtils.logDebug("Checking existence of key: {}", key);
            Boolean result = redisTemplate.hasKey(key);
            RedisOperationUtils.logDebug("Key existence result for {}: {}", key, result);
            return result;
        });
    }

    @Override
    public Long getExpire(String key) {
        return RedisOperationUtils.executeWithExceptionHandling("TTL", key, () -> {
            RedisOperationUtils.logDebug("Getting expiration time for key: {}", key);
            Long result = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            RedisOperationUtils.logDebug("Expiration time for key {}: {} seconds", key, result);
            return result;
        });
    }

    @Override
    public Set<String> keys(String pattern) {
        return RedisOperationUtils.executeWithExceptionHandling("KEYS", pattern, () -> {
            RedisOperationUtils.logDebug("Getting keys by pattern: {}", pattern);
            Set<String> result = redisTemplate.keys(pattern);
            if (result == null) {
                result = new LinkedHashSet<>();
            }
            RedisOperationUtils.logDebug("Found {} keys matching pattern: {}", result.size(), pattern);
            return result;
        });
    }

    @Override
    public Set<String> scanKeys(String pattern) {
        return RedisOperationUtils.executeWithExceptionHandling("SCAN", pattern, () -> {
            RedisOperationUtils.logDebug("Scanning keys by pattern: {}", pattern);
            Set<String> result = new LinkedHashSet<>();

            RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
            if (factory == null) {
                log.warn("RedisConnectionFactory is null, fallback to RedisTemplate.keys()");
                return keys(pattern);
            }

            // 使用独立的非事务连接执行 SCAN，避免受当前线程事务/管道影响
            try (RedisConnection connection = factory.getConnection()) {
                ScanOptions options = ScanOptions.scanOptions().match(pattern).count(1000).build();
                RedisKeyCommands keyCommands = connection.keyCommands();

                if (keyCommands != null) {
                    try (Cursor<byte[]> cursor = keyCommands.scan(options)) {
                        while (cursor.hasNext()) {
                            result.add(new String(cursor.next(), StandardCharsets.UTF_8));
                        }
                    }
                    RedisOperationUtils.logDebug("Found {} keys matching pattern via SCAN: {}", result.size(), pattern);
                    return result;
                } else {
                    RedisOperationUtils.logDebug("KeyCommands is null on connection, fallback to KEYS on standalone connection");
                    Set<byte[]> raw = connection.keys(pattern.getBytes(StandardCharsets.UTF_8));
                    if (raw != null) {
                        for (byte[] b : raw) {
                            result.add(new String(b, StandardCharsets.UTF_8));
                        }
                    }
                    return result;
                }

            } catch (InvalidDataAccessApiUsageException e) {
                // 明确处理 SCAN 在事务/管道模式受限的场景，回退到 KEYS（同样使用独立连接）
                log.warn("SCAN not allowed in current mode, fallback to KEYS - pattern: {}", pattern, e);
                try (RedisConnection connection = factory.getConnection()) {
                    Set<byte[]> raw = connection.keys(pattern.getBytes(StandardCharsets.UTF_8));
                    if (raw != null) {
                        for (byte[] b : raw) {
                            result.add(new String(b, StandardCharsets.UTF_8));
                        }
                    }
                    return result;
                } catch (Exception ex) {
                    log.error("Fallback KEYS failed on standalone connection, final fallback to RedisTemplate.keys()", ex);
                    return keys(pattern);
                }
            } catch (Exception e) {
                // 任何其它异常，记录并回退到 RedisTemplate.keys()，避免影响调用方
                log.error("Failed to scan keys by pattern (will fallback to KEYS via RedisTemplate): {}", pattern, e);
                return keys(pattern);
            }
        });
    }
}
