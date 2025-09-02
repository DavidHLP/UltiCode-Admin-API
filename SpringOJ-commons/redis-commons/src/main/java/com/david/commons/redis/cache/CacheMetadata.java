package com.david.commons.redis.cache;

import com.david.commons.redis.serialization.SerializationType;

import java.lang.reflect.Method;

/**
 * 缓存元数据
 * <p>
 * 封装缓存注解的元数据信息，用于缓存操作的统一处理。
 * </p>
 *
 * @author David
 */
public class CacheMetadata {

    /**
     * 缓存操作类型
     */
    private final CacheOperation operation;

    /**
     * 目标方法
     */
    private final Method method;

    /**
     * 缓存键表达式
     */
    private final String key;

    /**
     * 缓存键前缀
     */
    private final String keyPrefix;

    /**
     * 缓存条件表达式
     */
    private final String condition;

    /**
     * 缓存排除条件表达式
     */
    private final String unless;

    /**
     * 缓存过期时间（秒）
     */
    private final long ttl;

    /**
     * 序列化类型
     */
    private final SerializationType serialization;

    /**
     * 是否同步执行
     */
    private final boolean sync;

    /**
     * 是否缓存空值
     */
    private final boolean cacheNull;

    /**
     * 空值缓存时间（秒）
     */
    private final long nullTtl;

    /**
     * 是否清除所有匹配项（仅 EVICT 操作）
     */
    private final boolean allEntries;

    /**
     * 是否在方法执行前操作（仅 EVICT 操作）
     */
    private final boolean beforeInvocation;

    /**
     * 批量操作大小（仅 EVICT 操作）
     */
    private final int batchSize;

    /**
     * 缓存值表达式（仅 PUT 操作）
     */
    private final String value;

    /**
     * 缓存值类型
     * <p>
     * 解析自注解的 type 属性；当注解为 Void.class 时，默认取方法返回类型。
     * </p>
     */
    private final Class<?> returnType;

    private CacheMetadata(Builder builder) {
        this.operation = builder.operation;
        this.method = builder.method;
        this.key = builder.key;
        this.keyPrefix = builder.keyPrefix;
        this.condition = builder.condition;
        this.unless = builder.unless;
        this.ttl = builder.ttl;
        this.serialization = builder.serialization;
        this.sync = builder.sync;
        this.cacheNull = builder.cacheNull;
        this.nullTtl = builder.nullTtl;
        this.allEntries = builder.allEntries;
        this.beforeInvocation = builder.beforeInvocation;
        this.batchSize = builder.batchSize;
        this.value = builder.value;
        this.returnType = builder.returnType;
    }

    // Getters
    public CacheOperation getOperation() {
        return operation;
    }

    public Method getMethod() {
        return method;
    }

    public String getKey() {
        return key;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public String getCondition() {
        return condition;
    }

    public String getUnless() {
        return unless;
    }

    public long getTtl() {
        return ttl;
    }

    public SerializationType getSerialization() {
        return serialization;
    }

    public boolean isSync() {
        return sync;
    }

    public boolean isCacheNull() {
        return cacheNull;
    }

    public long getNullTtl() {
        return nullTtl;
    }

    public boolean isAllEntries() {
        return allEntries;
    }

    public boolean isBeforeInvocation() {
        return beforeInvocation;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public String getValue() {
        return value;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    /**
     * 创建构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 构建器类
     */
    public static class Builder {
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

        public CacheMetadata build() {
            if (operation == null) {
                throw new IllegalArgumentException("Cache operation cannot be null");
            }
            if (method == null) {
                throw new IllegalArgumentException("Target method cannot be null");
            }
            if (key == null || key.trim().isEmpty()) {
                throw new IllegalArgumentException("Cache key cannot be null or empty");
            }
            return new CacheMetadata(this);
        }
    }

    @Override
    public String toString() {
        return "CacheMetadata{" +
                "operation=" + operation +
                ", method=" + method.getName() +
                ", key='" + key + '\'' +
                ", keyPrefix='" + keyPrefix + '\'' +
                ", ttl=" + ttl +
                ", serialization=" + serialization +
                '}';
    }
}