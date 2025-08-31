package com.david.commons.redis.config;

import com.david.commons.redis.serialization.SerializationType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Redis Commons 配置属性
 *
 * @author David
 */
@Data
@ConfigurationProperties(prefix = "spring.redis.commons")
public class RedisCommonsProperties {

    /**
     * 是否启用 Redis Commons
     */
    private boolean enabled = true;

    /**
     * 全局键前缀
     */
    @NotBlank
    private String keyPrefix = "springoj:";

    /**
     * 序列化配置
     */
    @Valid
    @NotNull
    private SerializationConfig serialization = new SerializationConfig();

    /**
     * 缓存配置
     */
    @Valid
    @NotNull
    private CacheConfig cache = new CacheConfig();

    /**
     * 锁配置
     */
    @Valid
    @NotNull
    private LockConfig lock = new LockConfig();

    /**
     * 防护配置
     */
    @Valid
    @NotNull
    private ProtectionConfig protection = new ProtectionConfig();

    /**
     * 监控配置
     */
    @Valid
    @NotNull
    private MonitoringConfig monitoring = new MonitoringConfig();

    /**
     * 序列化配置
     */
    @Data
    public static class SerializationConfig {
        /**
         * 默认序列化类型
         */
        @NotNull
        private SerializationType defaultType = SerializationType.JSON;

        /**
         * 是否启用压缩
         */
        private boolean enableCompression = false;

        /**
         * 压缩阈值（字节）
         */
        @Min(1)
        private int compressionThreshold = 1024;

        /**
         * 是否启用性能监控
         */
        private boolean enablePerformanceMonitoring = true;
    }

    /**
     * 缓存配置
     */
    @Data
    public static class CacheConfig {
        /**
         * 默认 TTL（秒）
         */
        @Min(1)
        private long defaultTtl = 3600; // 1小时

        /**
         * 是否启用空值缓存
         */
        private boolean enableNullCache = true;

        /**
         * 空值缓存 TTL（秒）
         */
        @Min(1)
        private int nullCacheTtl = 300; // 5分钟

        /**
         * 是否启用缓存统计
         */
        private boolean enableStatistics = true;

        /**
         * 缓存键最大长度
         */
        @Min(1)
        private int maxKeyLength = 250;
    }

    /**
     * 锁配置
     */
    @Data
    public static class LockConfig {
        /**
         * 默认等待时间（秒）
         */
        @Min(0)
        private long defaultWaitTime = 10; // 10秒

        /**
         * 默认持有时间（秒）
         */
        @Min(1)
        private long defaultLeaseTime = 30; // 30秒

        /**
         * 是否启用看门狗
         */
        private boolean enableWatchdog = true;

        /**
         * 看门狗续约间隔（毫秒）
         */
        @Min(1000)
        private long watchdogTimeout = 30000; // 30秒

        /**
         * 锁重试间隔（毫秒）
         */
        @Min(10)
        private long retryInterval = 100;
    }

    /**
     * 防护配置
     */
    @Data
    public static class ProtectionConfig {
        /**
         * 是否启用布隆过滤器
         */
        private boolean enableBloomFilter = true;

        /**
         * 布隆过滤器预期插入数量
         */
        @Min(1000)
        private int bloomFilterExpectedInsertions = 1000000;

        /**
         * 布隆过滤器误判率
         */
        @Min(0)
        private double bloomFilterFpp = 0.01;

        /**
         * 是否启用熔断器
         */
        private boolean enableCircuitBreaker = true;

        /**
         * 熔断器失败阈值
         */
        @Min(1)
        private int circuitBreakerFailureThreshold = 5;

        /**
         * 熔断器恢复时间（毫秒）
         */
        @Min(1000)
        private long circuitBreakerRecoveryTimeout = 60000; // 1分钟

        /**
         * 是否启用限流
         */
        private boolean enableRateLimit = false;

        /**
         * 限流每秒请求数
         */
        @Min(1)
        private int rateLimitPermitsPerSecond = 1000;
    }

    /**
     * 监控配置
     */
    @Data
    public static class MonitoringConfig {
        /**
         * 是否启用监控
         */
        private boolean enabled = true;

        /**
         * 是否启用健康检查
         */
        private boolean enableHealthCheck = true;

        /**
         * 健康检查间隔（毫秒）
         */
        @Min(1000)
        private long healthCheckInterval = 30000; // 30秒

        /**
         * 是否启用指标收集
         */
        private boolean enableMetrics = true;

        /**
         * 指标收集间隔（毫秒）
         */
        @Min(1000)
        private long metricsCollectionInterval = 10000; // 10秒

        /**
         * 是否启用慢查询日志
         */
        private boolean enableSlowLog = true;

        /**
         * 慢查询阈值（毫秒）
         */
        @Min(1)
        private long slowLogThreshold = 1000; // 1秒
    }
}