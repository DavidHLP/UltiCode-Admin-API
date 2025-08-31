package com.david.commons.redis;

import com.david.commons.redis.operations.*;
import com.david.commons.redis.serialization.SerializationType;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

/**
 * 序列化感知的操作包装器
 * 为 Redis 操作添加序列化性能监控功能
 *
 * @author David
 */
@Slf4j
public class SerializationAwareOperationsWrapper {

    private final RedisUtilsImpl redisUtils;
    private final SerializationType serializationType;

    public SerializationAwareOperationsWrapper(RedisUtilsImpl redisUtils, SerializationType serializationType) {
        this.redisUtils = redisUtils;
        this.serializationType = serializationType;
    }

    /**
     * 包装字符串操作
     */
    public <T> RedisStringOperations<T> wrapStringOperations(RedisStringOperations<T> operations) {
        return new SerializationAwareStringOperations<>(operations, redisUtils, serializationType);
    }

    /**
     * 包装哈希操作
     */
    public <T> RedisHashOperations<T> wrapHashOperations(RedisHashOperations<T> operations) {
        return new SerializationAwareHashOperations<>(operations, redisUtils, serializationType);
    }

    /**
     * 包装列表操作
     * 注意：为了简化实现，暂时直接返回原始操作，不添加性能监控
     */
    public <T> RedisListOperations<T> wrapListOperations(RedisListOperations<T> operations) {
        // TODO: 实现完整的性能监控包装器
        return operations;
    }

    /**
     * 包装集合操作
     * 注意：为了简化实现，暂时直接返回原始操作，不添加性能监控
     */
    public <T> RedisSetOperations<T> wrapSetOperations(RedisSetOperations<T> operations) {
        // TODO: 实现完整的性能监控包装器
        return operations;
    }

    /**
     * 包装有序集合操作
     * 注意：为了简化实现，暂时直接返回原始操作，不添加性能监控
     */
    public <T> RedisZSetOperations<T> wrapZSetOperations(RedisZSetOperations<T> operations) {
        // TODO: 实现完整的性能监控包装器
        return operations;
    }

    /**
     * 序列化感知的字符串操作实现
     */
    private static class SerializationAwareStringOperations<T> implements RedisStringOperations<T> {
        private final RedisStringOperations<T> delegate;
        private final RedisUtilsImpl redisUtils;
        private final SerializationType serializationType;

        public SerializationAwareStringOperations(RedisStringOperations<T> delegate,
                RedisUtilsImpl redisUtils,
                SerializationType serializationType) {
            this.delegate = delegate;
            this.redisUtils = redisUtils;
            this.serializationType = serializationType;
        }

        @Override
        public Boolean set(String key, T value) {
            return executeWithMetrics("set", () -> delegate.set(key, value), value);
        }

        @Override
        public Boolean set(String key, T value, long timeout, java.util.concurrent.TimeUnit unit) {
            return executeWithMetrics("setWithTimeout", () -> delegate.set(key, value, timeout, unit), value);
        }

        @Override
        public Boolean set(String key, T value, Duration duration) {
            return executeWithMetrics("setWithDuration", () -> delegate.set(key, value, duration), value);
        }

        @Override
        public Boolean setIfAbsent(String key, T value) {
            return executeWithMetrics("setIfAbsent", () -> delegate.setIfAbsent(key, value), value);
        }

        @Override
        public Boolean setIfAbsent(String key, T value, long timeout, java.util.concurrent.TimeUnit unit) {
            return executeWithMetrics("setIfAbsentWithTimeout", () -> delegate.setIfAbsent(key, value, timeout, unit),
                    value);
        }

        @Override
        public T get(String key) {
            return executeWithMetrics("get", () -> delegate.get(key), key);
        }

        @Override
        public T getAndSet(String key, T value) {
            return executeWithMetrics("getAndSet", () -> delegate.getAndSet(key, value), value);
        }

        @Override
        public Long increment(String key) {
            return executeWithMetrics("increment", () -> delegate.increment(key), key);
        }

        @Override
        public Long increment(String key, long delta) {
            return executeWithMetrics("incrementByDelta", () -> delegate.increment(key, delta), key);
        }

        @Override
        public Long decrement(String key) {
            return executeWithMetrics("decrement", () -> delegate.decrement(key), key);
        }

        @Override
        public Long decrement(String key, long delta) {
            return executeWithMetrics("decrementByDelta", () -> delegate.decrement(key, delta), key);
        }

        @Override
        public Long size(String key) {
            return executeWithMetrics("size", () -> delegate.size(key), key);
        }

        private <R> R executeWithMetrics(String operationName, Supplier<R> operation, Object data) {
            Instant start = Instant.now();
            boolean success = false;
            int dataSize = estimateDataSize(data);

            try {
                R result = operation.get();
                success = true;
                return result;
            } finally {
                Duration duration = Duration.between(start, Instant.now());
                redisUtils.recordSerializationMetrics(serializationType, duration, success, dataSize);
            }
        }

        private int estimateDataSize(Object data) {
            if (data == null)
                return 0;
            if (data instanceof String)
                return ((String) data).length() * 2;
            if (data instanceof byte[])
                return ((byte[]) data).length;
            return data.toString().length() * 2;
        }
    }

    /**
     * 序列化感知的哈希操作实现
     */
    private static class SerializationAwareHashOperations<T> implements RedisHashOperations<T> {
        private final RedisHashOperations<T> delegate;
        private final RedisUtilsImpl redisUtils;
        private final SerializationType serializationType;

        public SerializationAwareHashOperations(RedisHashOperations<T> delegate,
                RedisUtilsImpl redisUtils,
                SerializationType serializationType) {
            this.delegate = delegate;
            this.redisUtils = redisUtils;
            this.serializationType = serializationType;
        }

        @Override
        public void put(String key, String hashKey, T value) {
            executeWithMetrics("put", () -> {
                delegate.put(key, hashKey, value);
                return null;
            }, value);
        }

        @Override
        public void putAll(String key, java.util.Map<String, T> map) {
            executeWithMetrics("putAll", () -> {
                delegate.putAll(key, map);
                return null;
            }, map);
        }

        @Override
        public Boolean putIfAbsent(String key, String hashKey, T value) {
            return executeWithMetrics("putIfAbsent", () -> delegate.putIfAbsent(key, hashKey, value), value);
        }

        @Override
        public T get(String key, String hashKey) {
            return executeWithMetrics("get", () -> delegate.get(key, hashKey), key);
        }

        @Override
        public java.util.List<T> multiGet(String key, java.util.Collection<String> hashKeys) {
            return executeWithMetrics("multiGet", () -> delegate.multiGet(key, hashKeys), hashKeys);
        }

        @Override
        public java.util.Map<String, T> entries(String key) {
            return executeWithMetrics("entries", () -> delegate.entries(key), key);
        }

        @Override
        public java.util.Set<String> keys(String key) {
            return executeWithMetrics("keys", () -> delegate.keys(key), key);
        }

        @Override
        public java.util.List<T> values(String key) {
            return executeWithMetrics("values", () -> delegate.values(key), key);
        }

        @Override
        public Long delete(String key, String... hashKeys) {
            return executeWithMetrics("delete", () -> delegate.delete(key, hashKeys), hashKeys);
        }

        @Override
        public Boolean hasKey(String key, String hashKey) {
            return executeWithMetrics("hasKey", () -> delegate.hasKey(key, hashKey), key);
        }

        @Override
        public Long size(String key) {
            return executeWithMetrics("size", () -> delegate.size(key), key);
        }

        @Override
        public Long increment(String key, String hashKey, long delta) {
            return executeWithMetrics("increment", () -> delegate.increment(key, hashKey, delta), key);
        }

        @Override
        public Double increment(String key, String hashKey, double delta) {
            return executeWithMetrics("incrementDouble", () -> delegate.increment(key, hashKey, delta), key);
        }

        private <R> R executeWithMetrics(String operationName, Supplier<R> operation, Object data) {
            Instant start = Instant.now();
            boolean success = false;
            int dataSize = estimateDataSize(data);

            try {
                R result = operation.get();
                success = true;
                return result;
            } finally {
                Duration duration = Duration.between(start, Instant.now());
                redisUtils.recordSerializationMetrics(serializationType, duration, success, dataSize);
            }
        }

        private int estimateDataSize(Object data) {
            if (data == null)
                return 0;
            if (data instanceof String)
                return ((String) data).length() * 2;
            if (data instanceof byte[])
                return ((byte[]) data).length;
            if (data instanceof java.util.Collection)
                return ((java.util.Collection<?>) data).size() * 10;
            if (data instanceof java.util.Map)
                return ((java.util.Map<?, ?>) data).size() * 20;
            return data.toString().length() * 2;
        }
    }
}