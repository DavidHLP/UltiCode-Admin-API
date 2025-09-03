package com.david.commons.redis.operations.impl;

import com.david.commons.redis.RedisUtils;
import com.david.commons.redis.exception.RedisCommonsException;
import com.david.commons.redis.exception.RedisErrorCodes;
import com.david.commons.redis.operations.RedisCommonOperations;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Redis 通用操作实现类
 *
 * @author David
 */
@Slf4j
@Component
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
            log.debug("检查键是否存在 - 键: {}, 结果: {}", fullKey, result);
            return result;
        } catch (Exception e) {
            log.error("检查键是否存在失败 - 键: {}", key, e);
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
            log.debug("统计存在的键数量 - 请求: {}, 存在: {}", keys.size(), count);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("统计存在的键数量失败", e);
            throw handleException("countExistingKeys", "", e);
        }
    }

    @Override
    public Boolean delete(String key) {
        try {
            validateKey(key);
            String fullKey = redisUtils.buildKey(key);
            Boolean result = redisTemplate.delete(fullKey);
            log.debug("删除键 - 键: {}, 结果: {}", fullKey, result);
            return result;
        } catch (Exception e) {
            log.error("删除键失败 - 键: {}", key, e);
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
            log.debug("批量删除键 - 请求: {}, 已删除: {}", fullKeys.size(), deleted);
            return deleted;
        } catch (Exception e) {
            log.error("批量删除键失败 - 数量: {}", keys.size(), e);
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
                        RedisErrorCodes.CONFIG_PARAMETER_INVALID, "时间单位不能为空");
            }
            String fullKey = redisUtils.buildKey(key);
            Boolean result = redisTemplate.expire(fullKey, timeout, unit);
            log.debug("设置键过期时间 - 键: {}, 超时: {} {}, 结果: {}", fullKey, timeout, unit, result);
            return result;
        } catch (Exception e) {
            log.error("设置键过期时间失败 - 键: {}", key, e);
            throw handleException("expire", key, e);
        }
    }

    @Override
    public Boolean expire(String key, Duration duration) {
        try {
            validateKey(key);
            if (duration == null || duration.isZero() || duration.isNegative()) {
                throw new RedisCommonsException(RedisErrorCodes.CACHE_TTL_INVALID, "持续时间必须为正数");
            }
            String fullKey = redisUtils.buildKey(key);
            Boolean result = redisTemplate.expire(fullKey, duration);
            log.debug("设置键过期时间 - 键: {}, 持续时间: {}, 结果: {}", fullKey, duration, result);
            return result != null ? result : false;
        } catch (Exception e) {
            log.error("设置键过期时间(D)失败 - 键: {}", key, e);
            throw handleException("expireDuration", key, e);
        }
    }

    @Override
    public Boolean expireAt(String key, Date date) {
        try {
            validateKey(key);
            if (date == null) {
                throw new RedisCommonsException(
                        RedisErrorCodes.CONFIG_PARAMETER_INVALID, "过期日期不能为空");
            }
            String fullKey = redisUtils.buildKey(key);
            Boolean result = redisTemplate.expireAt(fullKey, date);
            log.debug("设置键在指定时间过期 - 键: {}, 日期: {}, 结果: {}", fullKey, date, result);
            return result;
        } catch (Exception e) {
            log.error("设置键在指定时间过期失败 - 键: {}", key, e);
            throw handleException("expireAt", key, e);
        }
    }

    @Override
    public Boolean persist(String key) {
        try {
            validateKey(key);
            String fullKey = redisUtils.buildKey(key);
            Boolean result = redisTemplate.persist(fullKey);
            log.debug("移除键的过期时间 - 键: {}, 结果: {}", fullKey, result);
            return result;
        } catch (Exception e) {
            log.error("移除键的过期时间失败 - 键: {}", key, e);
            throw handleException("persist", key, e);
        }
    }

    @Override
    public Long getExpire(String key, TimeUnit unit) {
        try {
            validateKey(key);
            if (unit == null) {
                throw new RedisCommonsException(
                        RedisErrorCodes.CONFIG_PARAMETER_INVALID, "时间单位不能为空");
            }
            String fullKey = redisUtils.buildKey(key);
            Long ttl = redisTemplate.getExpire(fullKey, unit);
            log.debug("获取键的剩余过期时间 - 键: {}, TTL: {} {}", fullKey, ttl, unit);
            return ttl;
        } catch (Exception e) {
            log.error("获取键的剩余过期时间失败 - 键: {}", key, e);
            throw handleException("getExpireUnit", key, e);
        }
    }

    @Override
    public Long getExpire(String key) {
        try {
            validateKey(key);
            String fullKey = redisUtils.buildKey(key);
            Long ttl = redisTemplate.getExpire(fullKey);
            log.debug("获取键的剩余过期时间 - 键: {}, TTL: {} 秒", fullKey, ttl);
            return ttl;
        } catch (Exception e) {
            log.error("获取键的剩余过期时间(秒)失败 - 键: {}", key, e);
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
            log.debug("重命名键 - 旧键: {}, 新键: {}", fullOld, fullNew);
        } catch (Exception e) {
            log.error("重命名键失败 - 旧键: {}, 新键: {}", oldKey, newKey, e);
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
            log.debug("仅当新键不存在时重命名 - 旧键: {}, 新键: {}, 结果: {}", fullOld, fullNew, result);
            return result;
        } catch (Exception e) {
            log.error("仅当新键不存在时重命名失败 - 旧键: {}, 新键: {}", oldKey, newKey, e);
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
            log.debug("获取键的数据类型 - 键: {}, 类型: {}", fullKey, type);
            return type;
        } catch (Exception e) {
            log.error("获取键的数据类型失败 - 键: {}", key, e);
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
            log.error("获取随机键失败", e);
            throw handleException("randomKey", "", e);
        }
    }

    @Override
    public Set<String> keys(String pattern) {
        try {
            if (!StringUtils.hasText(pattern)) {
                throw new RedisCommonsException(RedisErrorCodes.CONFIG_PARAMETER_INVALID, "模式不能为空");
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

            log.debug("根据模式查找键 - 模式: {}, 找到: {}", pattern, keys.size());
            return keys;
        } catch (Exception e) {
            log.error("根据模式查找键失败 - 模式: {}", pattern, e);
            throw handleException("keys", pattern, e);
        }
    }

    @Override
    public void scan(String pattern, int count, Consumer<String> keyConsumer) {
        try {
            if (!StringUtils.hasText(pattern)) {
                throw new RedisCommonsException(RedisErrorCodes.CONFIG_PARAMETER_INVALID, "模式不能为空");
            }
            if (keyConsumer == null) {
                throw new RedisCommonsException(
                        RedisErrorCodes.CONFIG_PARAMETER_INVALID, "键处理回调不能为空");
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
                            log.debug("SCAN 匹配 {} 个键，模式: {} (数量={})", matched, pattern, scanCount);
                        }
                        return null;
                    });
        } catch (Exception e) {
            log.error("扫描键失败 - 模式: {}", pattern, e);
            throw handleException("scan", pattern, e);
        }
    }

    @Override
    public Boolean move(String key, int dbIndex) {
        try {
            validateKey(key);
            if (dbIndex < 0) {
                throw new RedisCommonsException(
                        RedisErrorCodes.CONFIG_PARAMETER_INVALID, "数据库索引不能为负数");
            }
            String fullKey = redisUtils.buildKey(key);
            Boolean result = redisTemplate.move(fullKey, dbIndex);
            log.debug("将键移动到数据库 - 键: {}, 数据库索引: {}, 结果: {}", fullKey, dbIndex, result);
            return result != null ? result : false;
        } catch (Exception e) {
            log.error("将键移动到数据库失败 - 键: {}, 数据库索引: {}", key, dbIndex, e);
            throw handleException("move", key, e);
        }
    }

    @Override
    public void flushDb() {
        try {
            redisTemplate.getConnectionFactory().getConnection().flushDb();
            log.warn("清空当前数据库");
        } catch (Exception e) {
            log.error("清空当前数据库失败", e);
            throw handleException("flushDb", "", e);
        }
    }

    @Override
    public void flushAll() {
        try {
            redisTemplate.getConnectionFactory().getConnection().flushAll();
            log.warn("清空所有数据库");
        } catch (Exception e) {
            log.error("清空所有数据库失败", e);
            throw handleException("flushAll", "", e);
        }
    }

    @Override
    public Long dbSize() {
        try {
            Long size = redisTemplate.getConnectionFactory().getConnection().dbSize();
            log.debug("数据库大小: {}", size);
            return size;
        } catch (Exception e) {
            log.error("获取数据库大小失败", e);
            throw handleException("dbSize", "", e);
        }
    }

    /** 验证键名 */
    private void validateKey(String key) {
        if (!StringUtils.hasText(key)) {
            throw new RedisCommonsException(RedisErrorCodes.CACHE_KEY_EMPTY, "键不能为空");
        }
    }

    /** 验证超时时间 */
    private void validateTimeout(long timeout) {
        if (timeout <= 0) {
            throw new RedisCommonsException(RedisErrorCodes.CACHE_TTL_INVALID, "超时时间必须为正数");
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
                    RedisErrorCodes.CONNECTION_FAILED, "Redis 连接失败，操作: " + operation, e);
        } else if (e instanceof org.springframework.dao.QueryTimeoutException) {
            return new RedisCommonsException(
                    RedisErrorCodes.OPERATION_TIMEOUT, "Redis 操作超时，操作: " + operation, e);
        } else {
            return new RedisCommonsException(
                    RedisErrorCodes.CACHE_OPERATION_FAILED, "Redis 操作失败: " + operation, e);
        }
    }
}
