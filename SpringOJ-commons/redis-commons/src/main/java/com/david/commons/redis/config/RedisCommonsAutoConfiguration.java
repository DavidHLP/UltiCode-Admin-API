package com.david.commons.redis.config;

import com.david.commons.redis.RedisUtils;
import com.david.commons.redis.lock.DistributedLockManager;
import com.david.commons.redis.operations.*;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis Commons 自动配置类
 *
 * 提供 Redis Commons 工具库的自动配置支持，包括：
 * - RedisTemplate 配置
 * - Redisson 客户端配置
 * - Redis 操作类配置
 * - 分布式锁管理器配置
 * - RedisUtils 门面类配置
 *
 * @author David
 */
@Slf4j
@AutoConfiguration(after = RedisAutoConfiguration.class)
@ConditionalOnClass({ RedisTemplate.class })
@ConditionalOnProperty(prefix = "spring.redis.commons", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RedisCommonsProperties.class)
@Import({
        RedisCommonsAutoConfiguration.SerializationConfiguration.class,
        RedisCommonsAutoConfiguration.CacheConfiguration.class,
        RedisCommonsAutoConfiguration.LockConfiguration.class,
        RedisCommonsAutoConfiguration.MonitoringConfiguration.class
})
public class RedisCommonsAutoConfiguration {

    /**
     * 配置 Redis Commons 专用的 RedisTemplate
     *
     * @param connectionFactory Redis 连接工厂
     * @param properties        Redis Commons 配置属性
     * @return 配置好的 RedisTemplate
     */
    @Bean("redisCommonsTemplate")
    @ConditionalOnMissingBean(name = "redisCommonsTemplate")
    @ConditionalOnBean(RedisConnectionFactory.class)
    public RedisTemplate<String, Object> redisCommonsTemplate(
            RedisConnectionFactory connectionFactory,
            RedisCommonsProperties properties) {

        log.info("配置 Redis Commons Template，键前缀: {}, 默认序列化类型: {}",
                properties.getKeyPrefix(),
                properties.getSerialization().getDefaultType());

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 设置键序列化器 - 统一使用字符串序列化
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // 值序列化器暂时使用字符串序列化，后续任务中会实现动态序列化策略
        template.setValueSerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        // 启用默认序列化器
        template.setDefaultSerializer(stringSerializer);
        template.setEnableDefaultSerializer(true);

        template.afterPropertiesSet();

        log.debug("Redis Commons Template 配置完成");
        return template;
    }

    /**
     * 配置 Redisson 客户端
     *
     * @param connectionFactory Redis 连接工厂
     * @param properties        Redis Commons 配置属性
     * @return Redisson 客户端
     */
    @Bean
    @ConditionalOnMissingBean(RedissonClient.class)
    @ConditionalOnClass(RedissonClient.class)
    @ConditionalOnBean(RedisConnectionFactory.class)
    @ConditionalOnProperty(prefix = "spring.redis.commons.lock", name = "enabled", havingValue = "true", matchIfMissing = true)
    public RedissonClient redissonClient(RedisConnectionFactory connectionFactory,
            RedisCommonsProperties properties) {

        log.info("配置 Redisson 客户端，锁配置: 等待时间={}s, 持有时间={}s, 看门狗={}",
                properties.getLock().getDefaultWaitTime(),
                properties.getLock().getDefaultLeaseTime(),
                properties.getLock().isEnableWatchdog());

        Config config = new Config();

        try {
            // 根据连接工厂类型配置 Redisson
            if (connectionFactory instanceof LettuceConnectionFactory) {
                LettuceConnectionFactory lettuceFactory = (LettuceConnectionFactory) connectionFactory;
                configureRedissonForLettuce(config, lettuceFactory, properties);
            } else if (connectionFactory instanceof JedisConnectionFactory) {
                JedisConnectionFactory jedisFactory = (JedisConnectionFactory) connectionFactory;
                configureRedissonForJedis(config, jedisFactory, properties);
            } else {
                // 默认配置
                configureRedissonDefault(config, properties);
            }

            // 配置看门狗
            if (properties.getLock().isEnableWatchdog()) {
                config.setLockWatchdogTimeout(properties.getLock().getWatchdogTimeout());
            }

            RedissonClient client = Redisson.create(config);
            log.info("Redisson 客户端配置完成");
            return client;

        } catch (Exception e) {
            log.error("配置 Redisson 客户端失败", e);
            throw new IllegalStateException("Failed to configure Redisson client", e);
        }
    }

    /**
     * 为 Lettuce 连接工厂配置 Redisson
     */
    private void configureRedissonForLettuce(Config config, LettuceConnectionFactory factory,
            RedisCommonsProperties properties) {
        String host = factory.getHostName();
        int port = factory.getPort();
        int database = factory.getDatabase();
        String password = factory.getPassword();

        String address = String.format("redis://%s:%d", host, port);

        config.useSingleServer()
                .setAddress(address)
                .setDatabase(database)
                .setPassword(password)
                .setConnectionMinimumIdleSize(1)
                .setConnectionPoolSize(10)
                .setConnectTimeout(3000)
                .setTimeout(3000)
                .setRetryAttempts(3)
                .setRetryInterval(1500);

        log.debug("Redisson 配置为 Lettuce 模式: {}:{}, database: {}", host, port, database);
    }

    /**
     * 为 Jedis 连接工厂配置 Redisson
     */
    private void configureRedissonForJedis(Config config, JedisConnectionFactory factory,
            RedisCommonsProperties properties) {
        String host = factory.getHostName();
        int port = factory.getPort();
        int database = factory.getDatabase();
        String password = factory.getPassword();

        String address = String.format("redis://%s:%d", host, port);

        config.useSingleServer()
                .setAddress(address)
                .setDatabase(database)
                .setPassword(password)
                .setConnectionMinimumIdleSize(1)
                .setConnectionPoolSize(10)
                .setConnectTimeout(3000)
                .setTimeout(3000)
                .setRetryAttempts(3)
                .setRetryInterval(1500);

        log.debug("Redisson 配置为 Jedis 模式: {}:{}, database: {}", host, port, database);
    }

    /**
     * 默认 Redisson 配置
     */
    private void configureRedissonDefault(Config config, RedisCommonsProperties properties) {
        // 默认本地 Redis 配置
        config.useSingleServer()
                .setAddress("redis://localhost:6379")
                .setDatabase(0)
                .setConnectionMinimumIdleSize(1)
                .setConnectionPoolSize(10)
                .setConnectTimeout(3000)
                .setTimeout(3000)
                .setRetryAttempts(3)
                .setRetryInterval(1500);

        log.warn("使用默认 Redisson 配置: redis://localhost:6379");
    }

    // 注意：Redis 操作类、分布式锁管理器和 RedisUtils 门面类的具体实现
    // 将在后续任务中完成，这里只提供基础的 Bean 配置框架

    /**
     * 序列化相关配置
     *
     * 提供 Redis 序列化策略的自动配置支持
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "spring.redis.commons.serialization", name = "enabled", havingValue = "true", matchIfMissing = true)
    static class SerializationConfiguration {

        /**
         * Redis 序列化器工厂占位符
         * 实际实现将在任务 3 中完成
         */
        @Bean("redisSerializerFactory")
        @ConditionalOnMissingBean(name = "redisSerializerFactory")
        public Object redisSerializerFactory(RedisCommonsProperties properties) {
            log.info("配置 Redis 序列化器工厂，默认类型: {}, 压缩: {}",
                    properties.getSerialization().getDefaultType(),
                    properties.getSerialization().isEnableCompression());

            // 占位符实现，实际序列化器工厂将在后续任务中实现
            return new Object();
        }
    }

    /**
     * 缓存相关配置
     *
     * 提供缓存注解和管理器的自动配置支持
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "spring.redis.commons.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
    static class CacheConfiguration {

        /**
         * 缓存管理器占位符
         * 实际实现将在任务 7 中完成
         */
        @Bean("redisCacheManager")
        @ConditionalOnMissingBean(name = "redisCacheManager")
        public Object redisCacheManager(RedisCommonsProperties properties) {
            log.info("配置 Redis 缓存管理器，默认TTL: {}s, 空值缓存: {}, 统计: {}",
                    properties.getCache().getDefaultTtl(),
                    properties.getCache().isEnableNullCache(),
                    properties.getCache().isEnableStatistics());

            // 占位符实现，实际缓存管理器将在后续任务中实现
            return new Object();
        }
    }

    /**
     * 分布式锁相关配置
     *
     * 提供分布式锁管理器的自动配置支持
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(RedissonClient.class)
    @ConditionalOnProperty(prefix = "spring.redis.commons.lock", name = "enabled", havingValue = "true", matchIfMissing = true)
    static class LockConfiguration {

        /**
         * 分布式锁管理器
         */
        @Bean
        @ConditionalOnMissingBean(DistributedLockManager.class)
        @ConditionalOnBean(RedissonClient.class)
        public DistributedLockManager distributedLockManager(RedissonClient redissonClient,
                RedisCommonsProperties properties) {
            log.info("配置分布式锁管理器，等待时间: {}s, 持有时间: {}s, 看门狗: {}",
                    properties.getLock().getDefaultWaitTime(),
                    properties.getLock().getDefaultLeaseTime(),
                    properties.getLock().isEnableWatchdog());

            return new com.david.commons.redis.lock.impl.RedissonDistributedLockManager(redissonClient, properties);
        }

        /**
         * 分布式锁 AOP 切面
         */
        @Bean
        @ConditionalOnMissingBean(com.david.commons.redis.lock.aspect.DistributedLockAspect.class)
        @ConditionalOnBean(DistributedLockManager.class)
        @ConditionalOnClass(org.aspectj.lang.annotation.Aspect.class)
        public com.david.commons.redis.lock.aspect.DistributedLockAspect distributedLockAspect(DistributedLockManager lockManager) {
            log.info("配置分布式锁 AOP 切面");
            return new com.david.commons.redis.lock.aspect.DistributedLockAspect(lockManager);
        }
    }

    /**
     * 监控相关配置
     *
     * 提供健康检查和指标收集的自动配置支持
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "spring.redis.commons.monitoring", name = "enabled", havingValue = "true", matchIfMissing = true)
    static class MonitoringConfiguration {

        /**
         * Redis 健康检查指示器占位符
         * 实际实现将在任务 9 中完成
         */
        @Bean("redisCommonsHealthIndicator")
        @ConditionalOnMissingBean(name = "redisCommonsHealthIndicator")
        @ConditionalOnClass(HealthIndicator.class)
        public Object redisHealthIndicator(RedisCommonsProperties properties) {
            log.info("配置 Redis 健康检查指示器，检查间隔: {}ms",
                    properties.getMonitoring().getHealthCheckInterval());

            // 占位符实现，实际健康检查器将在后续任务中实现
            return new Object();
        }

        /**
         * Redis 指标收集器占位符
         * 实际实现将在任务 9 中完成
         */
        @Bean("redisCommonsMetricsCollector")
        @ConditionalOnMissingBean(name = "redisCommonsMetricsCollector")
        public Object redisMetricsCollector(RedisCommonsProperties properties) {
            log.info("配置 Redis 指标收集器，收集间隔: {}ms, 慢查询阈值: {}ms",
                    properties.getMonitoring().getMetricsCollectionInterval(),
                    properties.getMonitoring().getSlowLogThreshold());

            // 占位符实现，实际指标收集器将在后续任务中实现
            return new Object();
        }
    }
}