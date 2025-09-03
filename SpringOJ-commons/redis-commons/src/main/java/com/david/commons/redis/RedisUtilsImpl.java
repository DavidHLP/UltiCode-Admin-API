package com.david.commons.redis;

import com.david.commons.redis.config.RedisCommonsProperties;
import com.david.commons.redis.lock.DistributedLockManager;
import com.david.commons.redis.operations.RedisCommonOperations;
import com.david.commons.redis.operations.RedisHashOperations;
import com.david.commons.redis.operations.RedisStringOperations;
import com.david.commons.redis.operations.impl.RedisCommonOperationsImpl;
import com.david.commons.redis.operations.impl.RedisHashOperationsImpl;
import com.david.commons.redis.operations.impl.RedisStringOperationsImpl;
import com.david.commons.redis.serialization.SerializationStrategySelector;
import com.david.commons.redis.serialization.enums.SerializationType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Redis 工具类门面实现
 *
 * <p>提供统一的 Redis 操作接口，集成各种操作类和序列化策略。 使用工厂模式和缓存机制优化性能，支持多种序列化类型。
 *
 * @author David
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtilsImpl implements RedisUtils {
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisCommonsProperties properties;
    private final SerializationStrategySelector strategySelector;
    private final DistributedLockManager lockManager;

    /** 字符串操作缓存，按序列化类型缓存实例 */
    private final ConcurrentMap<SerializationType, RedisStringOperations> stringOpsCache =
            new ConcurrentHashMap<>();

    /** Hash 操作缓存，按序列化类型缓存实例 */
    private final ConcurrentMap<SerializationType, RedisHashOperations> hashOpsCache =
            new ConcurrentHashMap<>();

    /** 通用操作实例 */
    private volatile RedisCommonOperations commonOperations;

    /** 全局键前缀 */
    private volatile String keyPrefix;

    /** 默认序列化类型 */
    private volatile SerializationType defaultSerializationType = SerializationType.JSON;

    /** 默认字符串操作实例（使用默认序列化类型） */
    private volatile RedisStringOperations defaultStringOps;

    /** 默认 Hash 操作实例（使用默认序列化类型） */
    private volatile RedisHashOperations defaultHashOps;

    @Override
    public RedisStringOperations string() {
        if (defaultStringOps == null) {
            synchronized (this) {
                if (defaultStringOps == null) {
                    defaultStringOps =
                            new RedisStringOperationsImpl(redisTemplate, strategySelector);
                    log.debug("创建默认的 RedisStringOperations 实例");
                }
            }
        }
        return defaultStringOps;
    }

    @Override
    public RedisStringOperations string(SerializationType serializationType) {
        SerializationType target =
                (serializationType != null) ? serializationType : getDefaultSerializationType();
        return stringOpsCache.computeIfAbsent(target, t -> string().using(t));
    }

    @Override
    public RedisHashOperations hash() {
        if (defaultHashOps == null) {
            synchronized (this) {
                if (defaultHashOps == null) {
                    defaultHashOps = new RedisHashOperationsImpl(redisTemplate, strategySelector);
                    log.debug("创建默认的 RedisHashOperations 实例");
                }
            }
        }
        return defaultHashOps;
    }

    @Override
    public RedisHashOperations hash(SerializationType serializationType) {
        SerializationType target =
                (serializationType != null) ? serializationType : getDefaultSerializationType();
        return hashOpsCache.computeIfAbsent(target, t -> hash().using(t));
    }

    @Override
    public RedisCommonOperations common() {
        if (commonOperations == null) {
            synchronized (this) {
                if (commonOperations == null) {
                    commonOperations = new RedisCommonOperationsImpl(redisTemplate, this);
                    log.debug("创建 RedisCommonOperations 实例");
                }
            }
        }
        return commonOperations;
    }

    @Override
    public DistributedLockManager lock() {
        return lockManager;
    }

    @Override
    public String getKeyPrefix() {
        if (keyPrefix == null) {
            synchronized (this) {
                if (keyPrefix == null) {
                    keyPrefix = properties.getKeyPrefix();
                }
            }
        }
        return keyPrefix;
    }

    @Override
    public void setKeyPrefix(String prefix) {
        this.keyPrefix = prefix;
        log.info("更新 Redis 键前缀为: {}", prefix);
    }

    @Override
    public String buildKey(String key) {
        if (!StringUtils.hasText(key)) {
            throw new IllegalArgumentException("键不能为空");
        }

        String prefix = getKeyPrefix();
        if (StringUtils.hasText(prefix)) {
            return prefix.endsWith(":") ? prefix + key : prefix + ":" + key;
        }
        return key;
    }

    @Override
    public SerializationType getDefaultSerializationType() {
        if (defaultSerializationType == null) {
            synchronized (this) {
                if (defaultSerializationType == null) {
                    defaultSerializationType = properties.getSerialization().getDefaultType();
                }
            }
        }
        return defaultSerializationType;
    }

    @Override
    public void setDefaultSerializationType(SerializationType serializationType) {
        this.defaultSerializationType = serializationType;
        log.info("更新默认序列化类型为: {}", serializationType);
    }

    /** 清理操作实例缓存 */
    public void clearOperationsCache() {
        stringOpsCache.clear();
        hashOpsCache.clear();
        defaultStringOps = null;
        defaultHashOps = null;
        commonOperations = null;
        log.info("清除 Redis 操作缓存");
    }

    /** 获取缓存统计信息 */
    public int getOperationsCacheSize() {
        return stringOpsCache.size() + hashOpsCache.size();
    }
}
