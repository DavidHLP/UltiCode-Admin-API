package com.david.commons.redis.operations.impl;

import com.david.commons.redis.RedisUtils;
import com.david.commons.redis.exception.RedisCommonsException;
import com.david.commons.redis.exception.RedisErrorCodes;
import com.david.commons.redis.operations.RedisCommonOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.function.Consumer;
import java.io.IOException;

/**
 * Redis 通用操作实现类
 *
 * @author David
 */
@Slf4j
public class RedisCommonOperationsImpl implements RedisCommonOperations {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisUtils redisUtils;

    public RedisCommonOperationsImpl(
            RedisTemplate<String, Object> redisTemplate, RedisUtils redisUtils) {
        this.redisTemplate = redisTemplate;
        this.redisUtils = redisUtils;
    }

    @Override
    public Boolean hasKey(String key) {
        try {
            validateKey(key);
            String fullKey = redisUtils.buildKey(key);
            Boolean result = redisTemplate.hasKey(fullKey);
            log.debug("Has key - key: {}, result: {}", fullKey, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to check key existence - key: {}", key, e);
            throw handleException("hasKey", key, e);
        }
    }

    @Override
    public Long countExistingKeys(Collection<String> keys) {
        try {
            if (keys == null || keys.isEmpty()) {
                return 0L;
            }

            RedisSerializer<String> keySerializer = redisTemplate.getStringSerializer();
            byte[][] rawKeys =
                    keys.stream()
                            .map(redisUtils::buildKey)
                            .map(keySerializer::serialize)
                            .filter(Objects::nonNull)
                            .toArray(byte[][]::new);

            Long count =
                    redisTemplate.execute(
                            (RedisConnection connection) -> connection.exists(rawKeys));
            log.debug("Count existing keys - request: {}, exist: {}", keys.size(), count);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("Failed to count existing keys", e);
            throw handleException("countExistingKeys", "", e);
        }
    }

    @Override
    public Boolean delete(String key) {
        try {
            validateKey(key);
            String fullKey = redisUtils.buildKey(key);
            Boolean result = redisTemplate.delete(fullKey);
            log.debug("Delete key - key: {}, result: {}", fullKey, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to delete key - key: {}", key, e);
            throw handleException("delete", key, e);
        }
    }

    @Override
    public Long delete(Collection<String> keys) {
        try {
            if (keys == null || keys.isEmpty()) {
                return 0L;
            }
            Set<String> fullKeys =
                    keys.stream().map(redisUtils::buildKey).collect(Collectors.toSet());
            Long deleted = redisTemplate.delete(fullKeys);
            log.debug("Delete keys - request: {}, deleted: {}", fullKeys.size(), deleted);
            return deleted;
        } catch (Exception e) {
            log.error("Failed to delete keys - size: {}", keys.size(), e);
            throw handleException("deleteMany", "", e);
        }
    }

    @Override
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            validateKey(key);
            validateTimeout(timeout);
            if (unit == null) {
                throw new RedisCommonsException(
                        RedisErrorCodes.CONFIG_PARAMETER_INVALID, "TimeUnit cannot be null");
            }
            String fullKey = redisUtils.buildKey(key);
            Boolean result = redisTemplate.expire(fullKey, timeout, unit);
            log.debug(
                    "Expire key - key: {}, timeout: {} {}, result: {}",
                    fullKey,
                    timeout,
                    unit,
                    result);
            return result;
        } catch (Exception e) {
            log.error("Failed to expire key - key: {}", key, e);
            throw handleException("expire", key, e);
        }
    }

    @Override
    public Boolean expire(String key, Duration duration) {
        try {
            validateKey(key);
            if (duration == null || duration.isZero() || duration.isNegative()) {
                throw new RedisCommonsException(
                        RedisErrorCodes.CACHE_TTL_INVALID, "Duration must be positive");
            }
            String fullKey = redisUtils.buildKey(key);
            Boolean result = redisTemplate.expire(fullKey, duration);
            log.debug("Expire key - key: {}, duration: {}, result: {}", fullKey, duration, result);
            return result != null ? result : false;
        } catch (Exception e) {
            log.error("Failed to expire key (Duration) - key: {}", key, e);
            throw handleException("expireDuration", key, e);
        }
    }

    @Override
    public Boolean expireAt(String key, Date date) {
        try {
            validateKey(key);
            if (date == null) {
                throw new RedisCommonsException(
                        RedisErrorCodes.CONFIG_PARAMETER_INVALID, "Expire date cannot be null");
            }
            String fullKey = redisUtils.buildKey(key);
            Boolean result = redisTemplate.expireAt(fullKey, date);
            log.debug("ExpireAt key - key: {}, date: {}, result: {}", fullKey, date, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to expireAt key - key: {}", key, e);
            throw handleException("expireAt", key, e);
        }
    }

    @Override
    public Boolean persist(String key) {
        try {
            validateKey(key);
            String fullKey = redisUtils.buildKey(key);
            Boolean result = redisTemplate.persist(fullKey);
            log.debug("Persist key - key: {}, result: {}", fullKey, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to persist key - key: {}", key, e);
            throw handleException("persist", key, e);
        }
    }

    @Override
    public Long getExpire(String key, TimeUnit unit) {
        try {
            validateKey(key);
            if (unit == null) {
                throw new RedisCommonsException(
                        RedisErrorCodes.CONFIG_PARAMETER_INVALID, "TimeUnit cannot be null");
            }
            String fullKey = redisUtils.buildKey(key);
            Long ttl = redisTemplate.getExpire(fullKey, unit);
            log.debug("Get expire - key: {}, ttl: {} {}", fullKey, ttl, unit);
            return ttl;
        } catch (Exception e) {
            log.error("Failed to get expire - key: {}", key, e);
            throw handleException("getExpireUnit", key, e);
        }
    }

    @Override
    public Long getExpire(String key) {
        try {
            validateKey(key);
            String fullKey = redisUtils.buildKey(key);
            Long ttl = redisTemplate.getExpire(fullKey);
            log.debug("Get expire - key: {}, ttl: {} seconds", fullKey, ttl);
            return ttl;
        } catch (Exception e) {
            log.error("Failed to get expire (seconds) - key: {}", key, e);
            throw handleException("getExpire", key, e);
        }
    }

    @Override
    public void rename(String oldKey, String newKey) {
        try {
            validateKey(oldKey);
            validateKey(newKey);
            String fullOld = redisUtils.buildKey(oldKey);
            String fullNew = redisUtils.buildKey(newKey);
            redisTemplate.rename(fullOld, fullNew);
            log.debug("Rename key - old: {}, new: {}", fullOld, fullNew);
        } catch (Exception e) {
            log.error("Failed to rename key - old: {}, new: {}", oldKey, newKey, e);
            throw handleException("rename", oldKey, e);
        }
    }

    @Override
    public Boolean renameIfAbsent(String oldKey, String newKey) {
        try {
            validateKey(oldKey);
            validateKey(newKey);
            String fullOld = redisUtils.buildKey(oldKey);
            String fullNew = redisUtils.buildKey(newKey);
            Boolean result = redisTemplate.renameIfAbsent(fullOld, fullNew);
            log.debug("RenameIfAbsent - old: {}, new: {}, result: {}", fullOld, fullNew, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to renameIfAbsent - old: {}, new: {}", oldKey, newKey, e);
            throw handleException("renameIfAbsent", oldKey, e);
        }
    }

    @Override
    public DataType type(String key) {
        try {
            validateKey(key);
            String fullKey = redisUtils.buildKey(key);
            org.springframework.data.redis.connection.DataType springType =
                    redisTemplate.type(fullKey);
            DataType type = convertDataType(springType);
            log.debug("Type - key: {}, type: {}", fullKey, type);
            return type;
        } catch (Exception e) {
            log.error("Failed to get type - key: {}", key, e);
            throw handleException("type", key, e);
        }
    }

    @Override
    public String randomKey() {
        try {
            final String keyPrefix = redisUtils.getKeyPrefix();
            return redisTemplate.execute(
                    (RedisConnection connection) -> {
                        byte[] raw = connection.randomKey();
                        RedisSerializer<String> keySerializer = redisTemplate.getStringSerializer();
                        String full = keySerializer.deserialize(raw);
                        if (!StringUtils.hasText(full)) {
                            return null;
                        }
                        if (StringUtils.hasText(keyPrefix) && full.startsWith(keyPrefix)) {
                            return full.substring(keyPrefix.length());
                        }
                        return full;
                    });
        } catch (Exception e) {
            log.error("Failed to get random key", e);
            throw handleException("randomKey", "", e);
        }
    }

    @Override
    public Set<String> keys(String pattern) {
        try {
            if (!StringUtils.hasText(pattern)) {
                throw new RedisCommonsException(
                        RedisErrorCodes.CONFIG_PARAMETER_INVALID,
                        "Pattern cannot be null or empty");
            }

            // 如果模式不包含前缀，则添加前缀
            String fullPattern = pattern;
            String keyPrefix = redisUtils.getKeyPrefix();
            if (StringUtils.hasText(keyPrefix) && !pattern.startsWith(keyPrefix)) {
                fullPattern = keyPrefix + pattern;
            }

            Set<String> keys = redisTemplate.keys(fullPattern);

            // 移除前缀返回原始键名
            if (StringUtils.hasText(keyPrefix)) {
                keys =
                        keys.stream()
                                .map(
                                        key ->
                                                key.startsWith(keyPrefix)
                                                        ? key.substring(keyPrefix.length())
                                                        : key)
                                .collect(Collectors.toSet());
            }

            log.debug(
                    "Find keys by pattern - pattern: {}, found: {}",
                    pattern,
                    keys.size());
            return keys;
        } catch (Exception e) {
            log.error("Failed to find keys by pattern - pattern: {}", pattern, e);
            throw handleException("keys", pattern, e);
        }
    }

    @Override
    public void scan(String pattern, int count, Consumer<String> keyConsumer) {
        try {
            if (!StringUtils.hasText(pattern)) {
                throw new RedisCommonsException(
                        RedisErrorCodes.CONFIG_PARAMETER_INVALID,
                        "Pattern cannot be null or empty");
            }
            if (keyConsumer == null) {
                throw new RedisCommonsException(
                        RedisErrorCodes.CONFIG_PARAMETER_INVALID, "Key consumer cannot be null");
            }

            final String keyPrefix = redisUtils.getKeyPrefix();
            final String fullPattern =
                    StringUtils.hasText(keyPrefix) && !pattern.startsWith(keyPrefix)
                            ? keyPrefix + pattern
                            : pattern;
            final int scanCount = count > 0 ? count : 1000;

            redisTemplate.execute(
                    (RedisConnection connection) -> {
                        ScanOptions options =
                                ScanOptions.scanOptions()
                                        .match(fullPattern)
                                        .count(scanCount)
                                        .build();

                        RedisSerializer<String> keySerializer = redisTemplate.getStringSerializer();

                        try (Cursor<byte[]> cursor = connection.scan(options)) {
                            long matched = 0L;
                            while (cursor.hasNext()) {
                                byte[] raw = cursor.next();
                                String fullKey = keySerializer.deserialize(raw);
                                if (fullKey == null) {
                                    continue;
                                }
                                String businessKey = fullKey;
                                if (StringUtils.hasText(keyPrefix)
                                        && fullKey.startsWith(keyPrefix)) {
                                    businessKey = fullKey.substring(keyPrefix.length());
                                }
                                keyConsumer.accept(businessKey);
                                matched++;
                            }
                            log.debug(
                                    "SCAN matched {} keys for pattern: {} (count={})",
                                    matched,
                                    pattern,
                                    scanCount);
                        }
                        return null;
                    });
        } catch (Exception e) {
            log.error("Failed to scan keys by pattern - pattern: {}", pattern, e);
            throw handleException("scan", pattern, e);
        }
    }

    @Override
    public Boolean move(String key, int dbIndex) {
        try {
            validateKey(key);
            if (dbIndex < 0) {
                throw new RedisCommonsException(
                        RedisErrorCodes.CONFIG_PARAMETER_INVALID,
                        "Database index cannot be negative");
            }
            String fullKey = redisUtils.buildKey(key);
            Boolean result = redisTemplate.move(fullKey, dbIndex);
            log.debug(
                    "Move key to database - key: {}, dbIndex: {}, result: {}",
                    fullKey,
                    dbIndex,
                    result);
            return result != null ? result : false;
        } catch (Exception e) {
            log.error("Failed to move key - key: {}, dbIndex: {}", key, dbIndex, e);
            throw handleException("move", key, e);
        }
    }

    @Override
    public void flushDb() {
        try {
            redisTemplate.getConnectionFactory().getConnection().flushDb();
            log.warn("Database flushed");
        } catch (Exception e) {
            log.error("Failed to flush database", e);
            throw handleException("flushDb", "", e);
        }
    }

    @Override
    public void flushAll() {
        try {
            redisTemplate.getConnectionFactory().getConnection().flushAll();
            log.warn("All databases flushed");
        } catch (Exception e) {
            log.error("Failed to flush all databases", e);
            throw handleException("flushAll", "", e);
        }
    }

    @Override
    public Long dbSize() {
        try {
            Long size = redisTemplate.getConnectionFactory().getConnection().dbSize();
            log.debug("Database size: {}", size);
            return size;
        } catch (Exception e) {
            log.error("Failed to get database size", e);
            throw handleException("dbSize", "", e);
        }
    }

    /** 验证键名 */
    private void validateKey(String key) {
        if (!StringUtils.hasText(key)) {
            throw new RedisCommonsException(
                    RedisErrorCodes.CACHE_KEY_EMPTY, "Key cannot be null or empty");
        }
    }

    /** 验证超时时间 */
    private void validateTimeout(long timeout) {
        if (timeout <= 0) {
            throw new RedisCommonsException(
                    RedisErrorCodes.CACHE_TTL_INVALID, "Timeout must be positive");
        }
    }

    /** 转换数据类型 */
    private DataType convertDataType(
            org.springframework.data.redis.connection.DataType springDataType) {
        if (springDataType == null) {
            return DataType.NONE;
        }

        switch (springDataType) {
            case STRING:
                return DataType.STRING;
            case LIST:
                return DataType.LIST;
            case SET:
                return DataType.SET;
            case ZSET:
                return DataType.ZSET;
            case HASH:
                return DataType.HASH;
            case STREAM:
                return DataType.STREAM;
            default:
                return DataType.NONE;
        }
    }

    /** 统一异常处理 */
    private RedisCommonsException handleException(String operation, String key, Exception e) {
        if (e instanceof RedisCommonsException) {
            return (RedisCommonsException) e;
        }

        if (e instanceof org.springframework.data.redis.RedisConnectionFailureException) {
            return new RedisCommonsException(
                    RedisErrorCodes.CONNECTION_FAILED,
                    "Redis connection failed during " + operation,
                    e);
        } else if (e instanceof org.springframework.dao.QueryTimeoutException) {
            return new RedisCommonsException(
                    RedisErrorCodes.OPERATION_TIMEOUT,
                    "Redis operation timeout during " + operation,
                    e);
        } else {
            return new RedisCommonsException(
                    RedisErrorCodes.CACHE_OPERATION_FAILED,
                    "Redis operation failed: " + operation,
                    e);
        }
    }
}
