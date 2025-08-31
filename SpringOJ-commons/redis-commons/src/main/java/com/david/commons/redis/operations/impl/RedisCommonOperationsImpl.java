package com.david.commons.redis.operations.impl;

import com.david.commons.redis.RedisUtils;
import com.david.commons.redis.exception.RedisCommonsException;
import com.david.commons.redis.exception.RedisErrorCodes;
import com.david.commons.redis.operations.RedisCommonOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis 通用操作实现类
 *
 * @author David
 */
@Slf4j
public class RedisCommonOperationsImpl implements RedisCommonOperations {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisUtils redisUtils;

    public RedisCommonOperationsImpl(RedisTemplate<String, Object> redisTemplate, RedisUtils redisUtils) {
        this.redisTemplate = redisTemplate;
        this.redisUtils = redisUtils;
    }

    @Override
    public Boolean hasKey(String key) {
        try {
            validateKey(key);
            String fullKey = redisUtils.buildKey(key);
            Boolean result = redisTemplate.hasKey(fullKey);
            log.debug("Check key existence - key: {}, exists: {}", fullKey, result);
            return result != null ? result : false;
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

            Set<String> fullKeys = keys.stream()
                    .filter(StringUtils::hasText)
                    .map(redisUtils::buildKey)
                    .collect(Collectors.toSet());

            if (fullKeys.isEmpty()) {
                return 0L;
            }

            Long count = redisTemplate.countExistingKeys(fullKeys);
            log.debug("Count existing keys - total: {}, existing: {}", fullKeys.size(), count);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("Failed to count existing keys - keys: {}", keys, e);
            throw handleException("countExistingKeys", keys.toString(), e);
        }
    }

    @Override
    public Boolean delete(String key) {
        try {
            validateKey(key);
            String fullKey = redisUtils.buildKey(key);
            Boolean result = redisTemplate.delete(fullKey);
            log.debug("Delete key - key: {}, deleted: {}", fullKey, result);
            return result != null ? result : false;
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

            Set<String> fullKeys = keys.stream()
                    .filter(StringUtils::hasText)
                    .map(redisUtils::buildKey)
                    .collect(Collectors.toSet());

            if (fullKeys.isEmpty()) {
                return 0L;
            }

            Long count = redisTemplate.delete(fullKeys);
            log.debug("Batch delete keys - total: {}, deleted: {}", fullKeys.size(), count);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("Failed to batch delete keys - keys: {}", keys, e);
            throw handleException("batchDelete", keys.toString(), e);
        }
    }

    @Override
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            validateKey(key);
            validateTimeout(timeout);
            String fullKey = redisUtils.buildKey(key);
            Boolean result = redisTemplate.expire(fullKey, timeout, unit);
            log.debug("Set expiration - key: {}, timeout: {} {}, result: {}",
                    fullKey, timeout, unit, result);
            return result != null ? result : false;
        } catch (Exception e) {
            log.error("Failed to set expiration - key: {}, timeout: {} {}", key, timeout, unit, e);
            throw handleException("expire", key, e);
        }
    }

    @Override
    public Boolean expire(String key, Duration duration) {
        try {
            validateKey(key);
            if (duration == null || duration.isNegative()) {
                throw new RedisCommonsException(RedisErrorCodes.CACHE_TTL_INVALID,
                        "Duration cannot be null or negative");
            }
            String fullKey = redisUtils.buildKey(key);
            Boolean result = redisTemplate.expire(fullKey, duration);
            log.debug("Set expiration - key: {}, duration: {}, result: {}",
                    fullKey, duration, result);
            return result != null ? result : false;
        } catch (Exception e) {
            log.error("Failed to set expiration - key: {}, duration: {}", key, duration, e);
            throw handleException("expire", key, e);
        }
    }

    @Override
    public Boolean expireAt(String key, Date date) {
        try {
            validateKey(key);
            if (date == null) {
                throw new RedisCommonsException(RedisErrorCodes.CACHE_TTL_INVALID,
                        "Expiration date cannot be null");
            }
            String fullKey = redisUtils.buildKey(key);
            Boolean result = redisTemplate.expireAt(fullKey, date);
            log.debug("Set expiration at - key: {}, date: {}, result: {}",
                    fullKey, date, result);
            return result != null ? result : false;
        } catch (Exception e) {
            log.error("Failed to set expiration at - key: {}, date: {}", key, date, e);
            throw handleException("expireAt", key, e);
        }
    }

    @Override
    public Boolean persist(String key) {
        try {
            validateKey(key);
            String fullKey = redisUtils.buildKey(key);
            Boolean result = redisTemplate.persist(fullKey);
            log.debug("Remove expiration - key: {}, result: {}", fullKey, result);
            return result != null ? result : false;
        } catch (Exception e) {
            log.error("Failed to remove expiration - key: {}", key, e);
            throw handleException("persist", key, e);
        }
    }

    @Override
    public Long getExpire(String key, TimeUnit unit) {
        try {
            validateKey(key);
            String fullKey = redisUtils.buildKey(key);
            Long expire = redisTemplate.getExpire(fullKey, unit);
            log.debug("Get expiration - key: {}, expire: {} {}", fullKey, expire, unit);
            return expire;
        } catch (Exception e) {
            log.error("Failed to get expiration - key: {}, unit: {}", key, unit, e);
            throw handleException("getExpire", key, e);
        }
    }

    @Override
    public Long getExpire(String key) {
        return getExpire(key, TimeUnit.SECONDS);
    }

    @Override
    public void rename(String oldKey, String newKey) {
        try {
            validateKey(oldKey);
            validateKey(newKey);
            String fullOldKey = redisUtils.buildKey(oldKey);
            String fullNewKey = redisUtils.buildKey(newKey);
            redisTemplate.rename(fullOldKey, fullNewKey);
            log.debug("Rename key - oldKey: {}, newKey: {}", fullOldKey, fullNewKey);
        } catch (Exception e) {
            log.error("Failed to rename key - oldKey: {}, newKey: {}", oldKey, newKey, e);
            throw handleException("rename", oldKey + " -> " + newKey, e);
        }
    }

    @Override
    public Boolean renameIfAbsent(String oldKey, String newKey) {
        try {
            validateKey(oldKey);
            validateKey(newKey);
            String fullOldKey = redisUtils.buildKey(oldKey);
            String fullNewKey = redisUtils.buildKey(newKey);
            Boolean result = redisTemplate.renameIfAbsent(fullOldKey, fullNewKey);
            log.debug("Rename key if absent - oldKey: {}, newKey: {}, result: {}",
                    fullOldKey, fullNewKey, result);
            return result != null ? result : false;
        } catch (Exception e) {
            log.error("Failed to rename key if absent - oldKey: {}, newKey: {}", oldKey, newKey, e);
            throw handleException("renameIfAbsent", oldKey + " -> " + newKey, e);
        }
    }

    @Override
    public DataType type(String key) {
        try {
            validateKey(key);
            String fullKey = redisUtils.buildKey(key);
            org.springframework.data.redis.connection.DataType springDataType = redisTemplate.type(fullKey);
            DataType result = convertDataType(springDataType);
            log.debug("Get key type - key: {}, type: {}", fullKey, result);
            return result;
        } catch (Exception e) {
            log.error("Failed to get key type - key: {}", key, e);
            throw handleException("type", key, e);
        }
    }

    @Override
    public Set<String> keys(String pattern) {
        try {
            if (!StringUtils.hasText(pattern)) {
                throw new RedisCommonsException(RedisErrorCodes.CONFIG_PARAMETER_INVALID,
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
            if (StringUtils.hasText(keyPrefix) && keys != null) {
                keys = keys.stream()
                        .map(key -> key.startsWith(keyPrefix) ? key.substring(keyPrefix.length()) : key)
                        .collect(Collectors.toSet());
            }

            log.debug("Find keys by pattern - pattern: {}, found: {}", pattern,
                    keys != null ? keys.size() : 0);
            return keys;
        } catch (Exception e) {
            log.error("Failed to find keys by pattern - pattern: {}", pattern, e);
            throw handleException("keys", pattern, e);
        }
    }

    @Override
    public String randomKey() {
        try {
            String key = redisTemplate.randomKey();

            // 移除前缀返回原始键名
            String keyPrefix = redisUtils.getKeyPrefix();
            if (StringUtils.hasText(keyPrefix) && key != null && key.startsWith(keyPrefix)) {
                key = key.substring(keyPrefix.length());
            }

            log.debug("Get random key - key: {}", key);
            return key;
        } catch (Exception e) {
            log.error("Failed to get random key", e);
            throw handleException("randomKey", "", e);
        }
    }

    @Override
    public Boolean move(String key, int dbIndex) {
        try {
            validateKey(key);
            if (dbIndex < 0) {
                throw new RedisCommonsException(RedisErrorCodes.CONFIG_PARAMETER_INVALID,
                        "Database index cannot be negative");
            }
            String fullKey = redisUtils.buildKey(key);
            Boolean result = redisTemplate.move(fullKey, dbIndex);
            log.debug("Move key to database - key: {}, dbIndex: {}, result: {}",
                    fullKey, dbIndex, result);
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

    /**
     * 验证键名
     */
    private void validateKey(String key) {
        if (!StringUtils.hasText(key)) {
            throw new RedisCommonsException(RedisErrorCodes.CACHE_KEY_EMPTY,
                    "Key cannot be null or empty");
        }
    }

    /**
     * 验证超时时间
     */
    private void validateTimeout(long timeout) {
        if (timeout <= 0) {
            throw new RedisCommonsException(RedisErrorCodes.CACHE_TTL_INVALID,
                    "Timeout must be positive");
        }
    }

    /**
     * 转换数据类型
     */
    private DataType convertDataType(org.springframework.data.redis.connection.DataType springDataType) {
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

    /**
     * 统一异常处理
     */
    private RedisCommonsException handleException(String operation, String key, Exception e) {
        if (e instanceof RedisCommonsException) {
            return (RedisCommonsException) e;
        }

        if (e instanceof org.springframework.data.redis.RedisConnectionFailureException) {
            return new RedisCommonsException(RedisErrorCodes.CONNECTION_FAILED,
                    "Redis connection failed during " + operation, e);
        } else if (e instanceof org.springframework.dao.QueryTimeoutException) {
            return new RedisCommonsException(RedisErrorCodes.OPERATION_TIMEOUT,
                    "Redis operation timeout during " + operation, e);
        } else {
            return new RedisCommonsException(RedisErrorCodes.CACHE_OPERATION_FAILED,
                    "Redis operation failed: " + operation, e);
        }
    }
}