package com.david.commons.redis.cache;

import com.david.commons.redis.cache.enums.CacheOperation;
import com.david.commons.redis.serialization.SerializationType;

import java.lang.reflect.Method;

/**
 * 缓存元数据 (Record 版本)
 *
 * <p>封装缓存注解的元数据信息，用于缓存操作的统一处理。 此版本使用 Java 14+ 的 record 类型，以减少样板代码并确保数据的不可变性。
 *
 * @author David
 */
public record CacheMetadata(
        CacheOperation operation,
        Method method,
        String key,
        String keyPrefix,
        String condition,
        String unless,
        long ttl,
        SerializationType serialization,
        boolean sync,
        boolean cacheNull,
        long nullTtl,
        boolean allEntries,
        boolean beforeInvocation,
        int batchSize,
        String value,
        Class<?> returnType) {

    /** 创建构建器 */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 构建器类
     *
     * <p>提供链式调用的方式来构造 CacheMetadata 实例，并支持默认值。
     */
    public static class Builder {
        // 成员变量，并在此处进行默认初始化
        private CacheOperation operation;
        private Method method;
        private String key;
        private String keyPrefix = "";
        private String condition = "";
        private String unless = "";
        private long ttl = -1L;
        private SerializationType serialization = SerializationType.JSON;
        private boolean sync = true;
        private boolean cacheNull = true;
        private long nullTtl = -1L;
        private boolean allEntries = false;
        private boolean beforeInvocation = false;
        private int batchSize = 1000;
        private String value = "";
        private Class<?> returnType;

        // Setter 方法，使用链式调用
        public Builder operation(CacheOperation operation) {
            this.operation = operation;
            return this;
        }

        public Builder method(Method method) {
            this.method = method;
            return this;
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder keyPrefix(String keyPrefix) {
            this.keyPrefix = keyPrefix;
            return this;
        }

        public Builder condition(String condition) {
            this.condition = condition;
            return this;
        }

        public Builder unless(String unless) {
            this.unless = unless;
            return this;
        }

        public Builder ttl(long ttl) {
            this.ttl = ttl;
            return this;
        }

        public Builder serialization(SerializationType serialization) {
            this.serialization = serialization;
            return this;
        }

        public Builder sync(boolean sync) {
            this.sync = sync;
            return this;
        }

        public Builder cacheNull(boolean cacheNull) {
            this.cacheNull = cacheNull;
            return this;
        }

        public Builder nullTtl(long nullTtl) {
            this.nullTtl = nullTtl;
            return this;
        }

        public Builder allEntries(boolean allEntries) {
            this.allEntries = allEntries;
            return this;
        }

        public Builder beforeInvocation(boolean beforeInvocation) {
            this.beforeInvocation = beforeInvocation;
            return this;
        }

        public Builder batchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder returnType(Class<?> returnType) {
            this.returnType = returnType;
            return this;
        }

        /** 构建 CacheMetadata 实例 */
        public CacheMetadata build() {
            // 参数验证
            if (operation == null) {
                throw new IllegalArgumentException("Cache operation cannot be null");
            }
            if (method == null) {
                throw new IllegalArgumentException("Target method cannot be null");
            }
            if (key == null || key.trim().isEmpty()) {
                throw new IllegalArgumentException("Cache key cannot be null or empty");
            }

            // 调用 record 的规范构造器，使用构建器中的值进行初始化
            return new CacheMetadata(
                    operation,
                    method,
                    key,
                    keyPrefix,
                    condition,
                    unless,
                    ttl,
                    serialization,
                    sync,
                    cacheNull,
                    nullTtl,
                    allEntries,
                    beforeInvocation,
                    batchSize,
                    value,
                    returnType);
        }
    }
}
