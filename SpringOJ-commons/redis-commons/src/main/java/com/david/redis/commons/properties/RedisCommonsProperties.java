package com.david.redis.commons.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import lombok.Data;
import lombok.Getter;

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
    /** 缓存配置 */
    @Data
    @Getter
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
}
