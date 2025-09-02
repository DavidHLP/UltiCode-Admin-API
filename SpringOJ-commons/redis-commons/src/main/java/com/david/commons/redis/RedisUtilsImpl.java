package com.david.commons.redis;

import com.david.commons.redis.config.RedisCommonsProperties;
import com.david.commons.redis.exception.RedisCommonsException;
import com.david.commons.redis.exception.RedisErrorCodes;
import com.david.commons.redis.lock.DistributedLockManager;
import com.david.commons.redis.operations.*;
import com.david.commons.redis.operations.impl.*;
import com.david.commons.redis.serialization.SerializationStrategySelector;
import com.david.commons.redis.serialization.SerializationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.time.Duration;

/**
 * Redis 工具类门面实现
 * 提供统一的 Redis 操作接口，集成各种操作类和序列化策略
 *
 * @author David
 */
@Slf4j
@Component
public class RedisUtilsImpl implements RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisCommonsProperties properties;
    private final SerializationStrategySelector strategySelector;
    private final DistributedLockManager lockManager;

    /**
     * 操作实例缓存，避免重复创建
     */
    private final ConcurrentMap<String, Object> operationCache = new ConcurrentHashMap<>();

    /**
     * 序列化性能监控
     */
    private final ConcurrentMap<SerializationType, SerializationMetrics> serializationMetrics = new ConcurrentHashMap<>();

    /**
     * 全局键前缀
     */
    private volatile String keyPrefix;

    /**
     * 默认序列化类型
     */
    private volatile SerializationType defaultSerializationType;

    public RedisUtilsImpl(@Qualifier("redisCommonsTemplate") RedisTemplate<String, Object> redisTemplate,
            RedisCommonsProperties properties,
            SerializationStrategySelector strategySelector,
            DistributedLockManager lockManager) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.strategySelector = strategySelector;
        this.lockManager = lockManager;

        // 初始化配置
        this.keyPrefix = properties.getKeyPrefix();
        this.defaultSerializationType = properties.getSerialization().getDefaultType();

        log.info("RedisUtils initialized with keyPrefix: {}, defaultSerialization: {}",
                keyPrefix, defaultSerializationType);
    }

    @Override
    public <T> RedisStringOperations<T> string() {
        return string(defaultSerializationType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> RedisStringOperations<T> string(SerializationType serializationType) {
        String cacheKey = "string:" + serializationType.name();
        return (RedisStringOperations<T>) operationCache.computeIfAbsent(cacheKey,
                k -> createStringOperations(serializationType));
    }

    @Override
    public <T> RedisStringOperations<T> string(Class<T> valueType) {
        return string(defaultSerializationType, valueType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> RedisStringOperations<T> string(SerializationType serializationType, Class<T> valueType) {
        Class<T> effectiveType = valueType != null ? valueType : (Class<T>) Object.class;
        String cacheKey = "string:" + serializationType.name() + ":" + effectiveType.getName();
        return (RedisStringOperations<T>) operationCache.computeIfAbsent(cacheKey,
                k -> createStringOperations(serializationType, effectiveType));
    }

    @Override
    public <T> RedisHashOperations<T> hash() {
        return hash(defaultSerializationType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> RedisHashOperations<T> hash(SerializationType serializationType) {
        String cacheKey = "hash:" + serializationType.name();
        return (RedisHashOperations<T>) operationCache.computeIfAbsent(cacheKey,
                k -> createHashOperations(serializationType));
    }

    @Override
    public <T> RedisListOperations<T> list() {
        return list(defaultSerializationType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> RedisListOperations<T> list(SerializationType serializationType) {
        String cacheKey = "list:" + serializationType.name();
        return (RedisListOperations<T>) operationCache.computeIfAbsent(cacheKey,
                k -> createListOperations(serializationType));
    }

    @Override
    public <T> RedisSetOperations<T> set() {
        return set(defaultSerializationType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> RedisSetOperations<T> set(SerializationType serializationType) {
        String cacheKey = "set:" + serializationType.name();
        return (RedisSetOperations<T>) operationCache.computeIfAbsent(cacheKey,
                k -> createSetOperations(serializationType));
    }

    @Override
    public <T> RedisZSetOperations<T> zset() {
        return zset(defaultSerializationType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> RedisZSetOperations<T> zset(SerializationType serializationType) {
        String cacheKey = "zset:" + serializationType.name();
        return (RedisZSetOperations<T>) operationCache.computeIfAbsent(cacheKey,
                k -> createZSetOperations(serializationType));
    }

    @Override
    public RedisCommonOperations common() {
        return (RedisCommonOperations) operationCache.computeIfAbsent("common",
                k -> createCommonOperations());
    }

    @Override
    public DistributedLockManager lock() {
        return lockManager;
    }

    @Override
    public void setKeyPrefix(String prefix) {
        if (prefix == null) {
            prefix = "";
        }
        this.keyPrefix = prefix;
        // 清除操作缓存，因为键前缀变化会影响所有操作
        clearOperationCache();
        log.info("Key prefix updated to: {}", prefix);
    }

    @Override
    public String getKeyPrefix() {
        return keyPrefix;
    }

    @Override
    public String buildKey(String key) {
        if (!StringUtils.hasText(key)) {
            throw new RedisCommonsException(RedisErrorCodes.CACHE_KEY_EMPTY,
                    "Redis key cannot be null or empty");
        }

        if (!StringUtils.hasText(keyPrefix)) {
            return key;
        }

        // 避免重复添加前缀
        if (key.startsWith(keyPrefix)) {
            return key;
        }

        return keyPrefix + key;
    }

    @Override
    public void setDefaultSerializationType(SerializationType serializationType) {
        if (serializationType == null) {
            throw new RedisCommonsException(RedisErrorCodes.CONFIG_PARAMETER_INVALID,
                    "Serialization type cannot be null");
        }

        this.defaultSerializationType = serializationType;
        // 清除操作缓存，因为默认序列化类型变化会影响默认操作
        clearOperationCache();
        log.info("Default serialization type updated to: {}", serializationType);
    }

    @Override
    public SerializationType getDefaultSerializationType() {
        return defaultSerializationType;
    }

    /**
     * 创建字符串操作实例
     */
    private RedisStringOperations<?> createStringOperations(SerializationType serializationType) {
        try {
            RedisStringOperations<?> operations = new RedisStringOperationsImpl<>(redisTemplate, strategySelector,
                    Object.class);

            // 如果启用了性能监控，则包装操作
            if (properties.getSerialization().isEnablePerformanceMonitoring()) {
                SerializationAwareOperationsWrapper wrapper = new SerializationAwareOperationsWrapper(this,
                        serializationType);
                return wrapper.wrapStringOperations(operations);
            }

            return operations;
        } catch (Exception e) {
            log.error("Failed to create string operations with serialization type: {}", serializationType, e);
            throw new RedisCommonsException(RedisErrorCodes.AUTO_CONFIGURATION_FAILED,
                    "Failed to create string operations", e);
        }
    }

    /**
     * 创建指定值类型的字符串操作实例
     */
    private <T> RedisStringOperations<T> createStringOperations(SerializationType serializationType, Class<T> valueType) {
        try {
            RedisStringOperations<T> operations = new RedisStringOperationsImpl<>(redisTemplate, strategySelector,
                    valueType);

            if (properties.getSerialization().isEnablePerformanceMonitoring()) {
                SerializationAwareOperationsWrapper wrapper = new SerializationAwareOperationsWrapper(this,
                        serializationType);
                return wrapper.wrapStringOperations(operations);
            }

            return operations;
        } catch (Exception e) {
            log.error("Failed to create typed string operations with serialization type: {}, valueType: {}",
                    serializationType, valueType, e);
            throw new RedisCommonsException(RedisErrorCodes.AUTO_CONFIGURATION_FAILED,
                    "Failed to create typed string operations", e);
        }
    }

    /**
     * 创建哈希操作实例
     */
    private RedisHashOperations<?> createHashOperations(SerializationType serializationType) {
        try {
            RedisHashOperations<?> operations = new RedisHashOperationsImpl<>(redisTemplate, strategySelector,
                    Object.class);

            // 如果启用了性能监控，则包装操作
            if (properties.getSerialization().isEnablePerformanceMonitoring()) {
                SerializationAwareOperationsWrapper wrapper = new SerializationAwareOperationsWrapper(this,
                        serializationType);
                return wrapper.wrapHashOperations(operations);
            }

            return operations;
        } catch (Exception e) {
            log.error("Failed to create hash operations with serialization type: {}", serializationType, e);
            throw new RedisCommonsException(RedisErrorCodes.AUTO_CONFIGURATION_FAILED,
                    "Failed to create hash operations", e);
        }
    }

    /**
     * 创建列表操作实例
     */
    private RedisListOperations<?> createListOperations(SerializationType serializationType) {
        try {
            RedisListOperations<?> operations = new RedisListOperationsImpl<>(redisTemplate, strategySelector,
                    Object.class);

            // 如果启用了性能监控，则包装操作
            if (properties.getSerialization().isEnablePerformanceMonitoring()) {
                SerializationAwareOperationsWrapper wrapper = new SerializationAwareOperationsWrapper(this,
                        serializationType);
                return wrapper.wrapListOperations(operations);
            }

            return operations;
        } catch (Exception e) {
            log.error("Failed to create list operations with serialization type: {}", serializationType, e);
            throw new RedisCommonsException(RedisErrorCodes.AUTO_CONFIGURATION_FAILED,
                    "Failed to create list operations", e);
        }
    }

    /**
     * 创建集合操作实例
     */
    private RedisSetOperations<?> createSetOperations(SerializationType serializationType) {
        try {
            RedisSetOperations<?> operations = new RedisSetOperationsImpl<>(redisTemplate, strategySelector,
                    Object.class);

            // 如果启用了性能监控，则包装操作
            if (properties.getSerialization().isEnablePerformanceMonitoring()) {
                SerializationAwareOperationsWrapper wrapper = new SerializationAwareOperationsWrapper(this,
                        serializationType);
                return wrapper.wrapSetOperations(operations);
            }

            return operations;
        } catch (Exception e) {
            log.error("Failed to create set operations with serialization type: {}", serializationType, e);
            throw new RedisCommonsException(RedisErrorCodes.AUTO_CONFIGURATION_FAILED,
                    "Failed to create set operations", e);
        }
    }

    /**
     * 创建有序集合操作实例
     */
    private RedisZSetOperations<?> createZSetOperations(SerializationType serializationType) {
        try {
            RedisZSetOperations<?> operations = new RedisZSetOperationsImpl<>(redisTemplate, strategySelector,
                    Object.class);

            // 如果启用了性能监控，则包装操作
            if (properties.getSerialization().isEnablePerformanceMonitoring()) {
                SerializationAwareOperationsWrapper wrapper = new SerializationAwareOperationsWrapper(this,
                        serializationType);
                return wrapper.wrapZSetOperations(operations);
            }

            return operations;
        } catch (Exception e) {
            log.error("Failed to create zset operations with serialization type: {}", serializationType, e);
            throw new RedisCommonsException(RedisErrorCodes.AUTO_CONFIGURATION_FAILED,
                    "Failed to create zset operations", e);
        }
    }

    /**
     * 创建通用操作实例
     */
    private RedisCommonOperations createCommonOperations() {
        try {
            // 需要检查 RedisCommonOperations 的实现类
            // 假设存在 RedisCommonOperationsImpl
            return new RedisCommonOperationsImpl(redisTemplate, this);
        } catch (Exception e) {
            log.error("Failed to create common operations", e);
            throw new RedisCommonsException(RedisErrorCodes.AUTO_CONFIGURATION_FAILED,
                    "Failed to create common operations", e);
        }
    }

    /**
     * 清除操作缓存
     */
    private void clearOperationCache() {
        operationCache.clear();
        log.debug("Operation cache cleared");
    }

    /**
     * 统一异常处理
     */
    public RuntimeException handleException(String operation, String key, Exception e) {
        log.error("Redis operation failed - operation: {}, key: {}", operation, key, e);

        // 根据异常类型进行分类处理
        if (e instanceof org.springframework.data.redis.RedisConnectionFailureException) {
            return new RedisCommonsException(RedisErrorCodes.CONNECTION_FAILED,
                    "Redis connection failed during " + operation, e);
        } else if (e instanceof org.springframework.dao.QueryTimeoutException) {
            return new RedisCommonsException(RedisErrorCodes.OPERATION_TIMEOUT,
                    "Redis operation timeout during " + operation, e);
        } else if (e instanceof RedisCommonsException) {
            // 重新抛出已知的 Redis Commons 异常
            return (RedisCommonsException) e;
        } else {
            // 包装未知异常
            return new RedisCommonsException(RedisErrorCodes.CACHE_OPERATION_FAILED,
                    "Redis operation failed: " + operation, e);
        }
    }

    /**
     * 获取操作缓存统计信息
     */
    public String getCacheStats() {
        return String.format("Operation cache size: %d entries", operationCache.size());
    }

    /**
     * 健康检查
     */
    public boolean isHealthy() {
        try {
            // 执行简单的 ping 操作来检查连接状态
            String result = redisTemplate.getConnectionFactory().getConnection().ping();
            return "PONG".equals(result);
        } catch (Exception e) {
            log.warn("Redis health check failed", e);
            return false;
        }
    }

    /**
     * 动态切换序列化策略
     */
    public void switchSerializationStrategy(SerializationType newStrategy) {
        if (newStrategy == null) {
            throw new RedisCommonsException(RedisErrorCodes.CONFIG_PARAMETER_INVALID,
                    "Serialization strategy cannot be null");
        }

        SerializationType oldStrategy = this.defaultSerializationType;
        this.defaultSerializationType = newStrategy;

        // 清除操作缓存以使用新的序列化策略
        clearOperationCache();

        log.info("Serialization strategy switched from {} to {}", oldStrategy, newStrategy);
    }

    /**
     * 获取序列化性能指标
     */
    public SerializationMetrics getSerializationMetrics(SerializationType type) {
        return serializationMetrics.computeIfAbsent(type, k -> new SerializationMetrics());
    }

    /**
     * 获取所有序列化策略的性能指标
     */
    public ConcurrentMap<SerializationType, SerializationMetrics> getAllSerializationMetrics() {
        return new ConcurrentHashMap<>(serializationMetrics);
    }

    /**
     * 重置序列化性能指标
     */
    public void resetSerializationMetrics() {
        serializationMetrics.clear();
        log.info("Serialization metrics reset");
    }

    /**
     * 记录序列化操作性能
     */
    public void recordSerializationMetrics(SerializationType type, Duration duration, boolean success, int dataSize) {
        SerializationMetrics metrics = getSerializationMetrics(type);
        metrics.recordOperation(duration, success, dataSize);
    }

    /**
     * 序列化性能指标类
     */
    public static class SerializationMetrics {
        private final AtomicLong totalOperations = new AtomicLong(0);
        private final AtomicLong successfulOperations = new AtomicLong(0);
        private final AtomicLong failedOperations = new AtomicLong(0);
        private final AtomicLong totalDurationNanos = new AtomicLong(0);
        private final AtomicLong totalDataSize = new AtomicLong(0);
        private volatile long maxDurationNanos = 0;
        private volatile long minDurationNanos = Long.MAX_VALUE;

        public void recordOperation(Duration duration, boolean success, int dataSize) {
            long durationNanos = duration.toNanos();

            totalOperations.incrementAndGet();
            totalDurationNanos.addAndGet(durationNanos);
            totalDataSize.addAndGet(dataSize);

            if (success) {
                successfulOperations.incrementAndGet();
            } else {
                failedOperations.incrementAndGet();
            }

            // 更新最大最小耗时
            updateMaxDuration(durationNanos);
            updateMinDuration(durationNanos);
        }

        private synchronized void updateMaxDuration(long durationNanos) {
            if (durationNanos > maxDurationNanos) {
                maxDurationNanos = durationNanos;
            }
        }

        private synchronized void updateMinDuration(long durationNanos) {
            if (durationNanos < minDurationNanos) {
                minDurationNanos = durationNanos;
            }
        }

        public long getTotalOperations() {
            return totalOperations.get();
        }

        public long getSuccessfulOperations() {
            return successfulOperations.get();
        }

        public long getFailedOperations() {
            return failedOperations.get();
        }

        public double getSuccessRate() {
            long total = getTotalOperations();
            return total > 0 ? (double) getSuccessfulOperations() / total : 0.0;
        }

        public Duration getAverageDuration() {
            long total = getTotalOperations();
            return total > 0 ? Duration.ofNanos(totalDurationNanos.get() / total) : Duration.ZERO;
        }

        public Duration getMaxDuration() {
            return maxDurationNanos > 0 ? Duration.ofNanos(maxDurationNanos) : Duration.ZERO;
        }

        public Duration getMinDuration() {
            return minDurationNanos < Long.MAX_VALUE ? Duration.ofNanos(minDurationNanos) : Duration.ZERO;
        }

        public long getTotalDataSize() {
            return totalDataSize.get();
        }

        public double getAverageDataSize() {
            long total = getTotalOperations();
            return total > 0 ? (double) getTotalDataSize() / total : 0.0;
        }

        @Override
        public String toString() {
            return String.format(
                    "SerializationMetrics{total=%d, success=%d, failed=%d, successRate=%.2f%%, " +
                            "avgDuration=%dms, maxDuration=%dms, minDuration=%dms, avgDataSize=%.1f bytes}",
                    getTotalOperations(),
                    getSuccessfulOperations(),
                    getFailedOperations(),
                    getSuccessRate() * 100,
                    getAverageDuration().toMillis(),
                    getMaxDuration().toMillis(),
                    getMinDuration().toMillis(),
                    getAverageDataSize());
        }
    }
}