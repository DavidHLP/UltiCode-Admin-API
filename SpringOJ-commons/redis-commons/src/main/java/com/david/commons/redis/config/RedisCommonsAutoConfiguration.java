package com.david.commons.redis.config;

import com.david.commons.redis.lock.DistributedLockManager;
import com.david.commons.redis.lock.aspect.DistributedLockAspect;
import com.david.commons.redis.lock.impl.RedissonDistributedLockManager;

import lombok.extern.slf4j.Slf4j;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Optional;

/**
 * Redis Commons 自动配置类
 *
 * <p>提供 Redis Commons 工具库的自动配置支持，采用模块化设计
 *
 * @author David
 */
@Slf4j
@AutoConfiguration(after = RedisAutoConfiguration.class)
@ConditionalOnClass(RedisTemplate.class)
@ConditionalOnProperty(
        prefix = "spring.redis.commons",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@EnableConfigurationProperties(RedisCommonsProperties.class)
@Import({
    RedisCommonsAutoConfiguration.SerializationConfiguration.class,
    RedisCommonsAutoConfiguration.CacheConfiguration.class,
    RedisCommonsAutoConfiguration.LockConfiguration.class,
    RedisCommonsAutoConfiguration.MonitoringConfiguration.class
})
public class RedisCommonsAutoConfiguration {

    /** 配置 Redis Commons 专用模板 */
    @Bean("redisCommonsTemplate")
    @ConditionalOnMissingBean(name = "redisCommonsTemplate")
    @ConditionalOnBean(RedisConnectionFactory.class)
    public RedisTemplate<String, Object> redisCommonsTemplate(
            RedisConnectionFactory connectionFactory, RedisCommonsProperties properties) {

        log.info(
                "初始化 Redis Commons Template - 键前缀: {}, 序列化: {}",
                properties.getKeyPrefix(),
                properties.getSerialization().getDefaultType());

        return createRedisTemplate(connectionFactory);
    }

    /** 配置 Redisson 客户端 */
    @Bean
    @ConditionalOnMissingBean(RedissonClient.class)
    @ConditionalOnClass(RedissonClient.class)
    @ConditionalOnBean(RedisConnectionFactory.class)
    @ConditionalOnProperty(
            prefix = "spring.redis.commons.lock",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    public RedissonClient redissonClient(
            RedisConnectionFactory factory, RedisCommonsProperties properties) {

        var lockConfig = properties.getLock();
        log.info(
                "初始化 Redisson - 等待: {}s, 持有: {}s, 看门狗: {}",
                lockConfig.getDefaultWaitTime(),
                lockConfig.getDefaultLeaseTime(),
                lockConfig.isEnableWatchdog());

        return Redisson.create(buildRedissonConfig(factory, properties));
    }

    // ==================== 私有方法 ====================

    private RedisTemplate<String, Object> createRedisTemplate(RedisConnectionFactory factory) {
        var template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(factory);

        // 统一使用字符串序列化器
        var stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);
        template.setDefaultSerializer(stringSerializer);
        template.setEnableDefaultSerializer(true);

        template.afterPropertiesSet();
        log.debug("Redis Template 配置完成");
        return template;
    }

    private Config buildRedissonConfig(
            RedisConnectionFactory factory, RedisCommonsProperties properties) {
        var config = new Config();

        if (factory instanceof LettuceConnectionFactory lettuce) {
            configureForLettuce(config, lettuce);
        } else if (factory instanceof JedisConnectionFactory jedis) {
            configureForJedis(config, jedis);
        } else {
            configureDefault(config);
        }

        // 配置看门狗
        if (properties.getLock().isEnableWatchdog()) {
            config.setLockWatchdogTimeout(properties.getLock().getWatchdogTimeout());
        }

        return config;
    }

    private void configureForLettuce(Config config, LettuceConnectionFactory factory) {
        var serverConfig =
                config.useSingleServer()
                        .setAddress(
                                "redis://%s:%d".formatted(factory.getHostName(), factory.getPort()))
                        .setDatabase(factory.getDatabase());

        Optional.ofNullable(factory.getPassword()).ifPresent(serverConfig::setPassword);
        applyCommonConfig(serverConfig);

        log.debug("Redisson 已配置为 Lettuce 模式");
    }

    private void configureForJedis(Config config, JedisConnectionFactory factory) {
        var serverConfig =
                config.useSingleServer()
                        .setAddress(
                                "redis://%s:%d".formatted(factory.getHostName(), factory.getPort()))
                        .setDatabase(factory.getDatabase());

        Optional.ofNullable(factory.getPassword()).ifPresent(serverConfig::setPassword);
        applyCommonConfig(serverConfig);

        log.debug("Redisson 已配置为 Jedis 模式");
    }

    private void configureDefault(Config config) {
        var serverConfig =
                config.useSingleServer().setAddress("redis://localhost:6379").setDatabase(0);

        applyCommonConfig(serverConfig);
        log.warn("使用默认 Redisson 配置");
    }

    private void applyCommonConfig(org.redisson.config.SingleServerConfig serverConfig) {
        serverConfig
                .setConnectionMinimumIdleSize(1)
                .setConnectionPoolSize(10)
                .setConnectTimeout(3000)
                .setTimeout(3000)
                .setRetryAttempts(3)
                .setRetryInterval(1500);
    }

    // ==================== 内部配置类 ====================

    /** 序列化配置 */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(
            prefix = "spring.redis.commons.serialization",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    static class SerializationConfiguration {

        @Bean("redisSerializerFactory")
        @ConditionalOnMissingBean(name = "redisSerializerFactory")
        public Object redisSerializerFactory(RedisCommonsProperties properties) {
            var serialization = properties.getSerialization();
            log.info(
                    "配置序列化器工厂 - 类型: {}, 压缩: {}",
                    serialization.getDefaultType(),
                    serialization.isEnableCompression());

            // TODO: 实际序列化器工厂实现
            return new Object();
        }
    }

    /** 缓存配置 */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(
            prefix = "spring.redis.commons.cache",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    static class CacheConfiguration {

        @Bean("redisCacheManager")
        @ConditionalOnMissingBean(name = "redisCacheManager")
        public Object redisCacheManager(RedisCommonsProperties properties) {
            var cache = properties.getCache();
            log.info(
                    "配置缓存管理器 - TTL: {}s, 空值缓存: {}, 统计: {}",
                    cache.getDefaultTtl(),
                    cache.isEnableNullCache(),
                    cache.isEnableStatistics());

            // TODO: 实际缓存管理器实现
            return new Object();
        }
    }

    /** 分布式锁配置 */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(RedissonClient.class)
    @ConditionalOnProperty(
            prefix = "spring.redis.commons.lock",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    static class LockConfiguration {

        @Bean
        @ConditionalOnMissingBean(DistributedLockManager.class)
        @ConditionalOnBean(RedissonClient.class)
        public DistributedLockManager distributedLockManager(
                RedissonClient client, RedisCommonsProperties properties) {
            var lock = properties.getLock();
            log.info(
                    "配置分布式锁管理器 - 等待: {}s, 持有: {}s",
                    lock.getDefaultWaitTime(),
                    lock.getDefaultLeaseTime());

            return new RedissonDistributedLockManager(client, properties);
        }

        @Bean
        @ConditionalOnMissingBean(DistributedLockAspect.class)
        @ConditionalOnBean(DistributedLockManager.class)
        @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
        public DistributedLockAspect distributedLockAspect(DistributedLockManager manager) {
            log.info("配置分布式锁 AOP 切面");
            return new DistributedLockAspect(manager);
        }
    }

    /** 监控配置 */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(
            prefix = "spring.redis.commons.monitoring",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    static class MonitoringConfiguration {

        @Bean("redisCommonsHealthIndicator")
        @ConditionalOnMissingBean(name = "redisCommonsHealthIndicator")
        @ConditionalOnClass(HealthIndicator.class)
        public Object redisHealthIndicator(RedisCommonsProperties properties) {
            log.info("配置健康检查器 - 间隔: {}ms", properties.getMonitoring().getHealthCheckInterval());

            // TODO: 实际健康检查器实现
            return new Object();
        }

        @Bean("redisCommonsMetricsCollector")
        @ConditionalOnMissingBean(name = "redisCommonsMetricsCollector")
        public Object redisMetricsCollector(RedisCommonsProperties properties) {
            var monitoring = properties.getMonitoring();
            log.info(
                    "配置指标收集器 - 间隔: {}ms, 慢查询: {}ms",
                    monitoring.getMetricsCollectionInterval(),
                    monitoring.getSlowLogThreshold());

            // TODO: 实际指标收集器实现
            return new Object();
        }
    }
}
