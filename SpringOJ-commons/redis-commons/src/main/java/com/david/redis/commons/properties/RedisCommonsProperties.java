package com.david.redis.commons.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Redis Commons 配置属性类
 *
 * @author david
 */
@Data
@Validated
@ConfigurationProperties(prefix = "spring.data.redis.commons")
public class RedisCommonsProperties {

    /** 缓存相关配置 */
    @Valid @NotNull private Cache cache = new Cache();

    /** 分布式锁相关配置 */
    @Valid @NotNull private Lock lock = new Lock();

    /** 事务相关配置 */
    @Valid @NotNull private Transaction transaction = new Transaction();

    /** 监控指标相关配置 */
    @Valid @NotNull private Metrics metrics = new Metrics();

    /** 缓存配置 */
    @Data
    public static class Cache {
        /** 默认缓存过期时间 */
        private Duration defaultTtl = Duration.ofHours(1);

        /** 缓存键前缀 */
        private String keyPrefix = "springoj:cache:";

        /** 是否启用空值缓存 */
        private boolean enableNullValues = false;

        /** 缓存序列化类型 */
        private String serializationType = "JSON";
    }

    /** 分布式锁配置 */
    @Data
    public static class Lock {
        /** 默认等待时间 */
        private Duration defaultWaitTime = Duration.ofSeconds(10);

        /** 默认租约时间 */
        private Duration defaultLeaseTime = Duration.ofSeconds(30);

        /** 重试次数 */
        private int retryAttempts = 3;

        /** 锁键前缀 */
        private String keyPrefix = "springoj:lock:";
    }

    /** 事务配置 */
    @Data
    public static class Transaction {
        /** 是否启用事务功能 */
        private boolean enabled = true;

        /** 是否启用自动回滚 */
        private boolean enableAutoRollback = true;

        /** 默认事务超时时间 */
        private Duration timeout = Duration.ofSeconds(30);

        /** 是否启用事务日志 */
        private boolean enableTransactionLog = false;

        /** 最大嵌套事务深度 */
        private int maxNestingDepth = 5;

        /** 事务监控是否启用 */
        private boolean enableMonitoring = true;

        /** 事务性能警告阈值 */
        private Duration performanceWarningThreshold = Duration.ofSeconds(5);
    }

    /** 监控指标配置 */
    @Data
    public static class Metrics {
        /** 是否启用监控 */
        private boolean enabled = true;

        /** 慢查询阈值 */
        private Duration slowQueryThreshold = Duration.ofMillis(100);

        /** 是否启用详细指标 */
        private boolean enableDetailedMetrics = false;

        /** 指标收集间隔 */
        private Duration collectionInterval = Duration.ofSeconds(30);
    }
}
