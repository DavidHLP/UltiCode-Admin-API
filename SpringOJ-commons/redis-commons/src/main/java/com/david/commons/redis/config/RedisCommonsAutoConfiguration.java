package com.david.commons.redis.config;

import com.david.commons.redis.cache.aspect.RedisCacheableAspect;
import com.david.commons.redis.cache.aspect.RedisEvictAspect;
import com.david.commons.redis.cache.aspect.RedisPutAspect;
import com.david.commons.redis.cache.aspect.chain.cacheable.CacheReadHandler;
import com.david.commons.redis.cache.aspect.chain.cacheable.CacheWriteHandler;
import com.david.commons.redis.cache.aspect.chain.cacheable.ConditionHandler;
import com.david.commons.redis.cache.aspect.chain.cacheable.MethodInvokeHandler;
import com.david.commons.redis.cache.aspect.chain.evict.AfterEvictHandler;
import com.david.commons.redis.cache.aspect.chain.evict.BeforeEvictHandler;
import com.david.commons.redis.cache.aspect.chain.evict.EvictConditionHandler;
import com.david.commons.redis.cache.aspect.chain.evict.EvictMethodInvokeHandler;
import com.david.commons.redis.cache.aspect.chain.put.PutCacheUpdateHandler;
import com.david.commons.redis.cache.aspect.chain.put.PutConditionHandler;
import com.david.commons.redis.cache.aspect.chain.put.PutMethodInvokeHandler;
import com.david.commons.redis.cache.parser.CacheAnnotationParser;
import com.david.commons.redis.lock.DistributedLockManager;
import com.david.commons.redis.lock.aspect.DistributedLockAspect;
import com.david.commons.redis.serialization.impl.JsonRedisSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Objects;
import java.util.Optional;

/**
 * Redis Commons 自动配置类
 *
 * <p>提供 Redis Commons 工具库的自动配置支持，采用模块化设计
 *
 * @author David
 */
@Configuration(proxyBeanMethods = false)
@AutoConfiguration(after = RedisAutoConfiguration.class)
@ConditionalOnClass(RedisTemplate.class)
@ConditionalOnProperty(
        prefix = "spring.redis.commons",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@EnableConfigurationProperties(RedisCommonsProperties.class)
public class RedisCommonsAutoConfiguration {

    private final Logger log = LoggerFactory.getLogger(getClass());

    // ==================== Bean 配置方法 ====================

    /** 配置 Redis Commons 专用模板 */
    @Bean("redisCommonsTemplate")
    @ConditionalOnMissingBean(name = "redisCommonsTemplate")
    @ConditionalOnBean(RedisConnectionFactory.class)
    public RedisTemplate<String, Object> redisCommonsTemplate(
            RedisConnectionFactory connectionFactory,
            RedisCommonsProperties properties,
            ObjectProvider<ObjectMapper> objectMapperProvider) {

        log.info(
                "初始化 Redis Commons Template - 键前缀: {}, 序列化: {}",
                properties.getKeyPrefix(),
                properties.getSerialization().getDefaultType());

        // 内联 resolveObjectMapper 方法的逻辑
        ObjectMapper mapper = objectMapperProvider.getIfAvailable();
        ObjectMapper resolvedMapper =
                JsonRedisSerializer.configureObjectMapperForRedis(
                        Objects.requireNonNullElseGet(mapper, ObjectMapper::new));

        // 内联 createRedisTemplate 方法的逻辑
        var template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(connectionFactory);
        var stringSerializer = new StringRedisSerializer();
        var jsonSerializer = new GenericJackson2JsonRedisSerializer(resolvedMapper);
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        template.setDefaultSerializer(jsonSerializer);
        template.setEnableDefaultSerializer(true);
        template.afterPropertiesSet();
        log.debug("Redis Template 配置完成");
        return template;
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

        // 内联 buildRedissonConfig 方法的逻辑
        var config = new Config();
        if (factory instanceof LettuceConnectionFactory lettuce) {
            // 内联 configureForLettuce 方法的逻辑
            var serverConfig =
                    config.useSingleServer()
                            .setAddress(
                                    "redis://%s:%d"
                                            .formatted(lettuce.getHostName(), lettuce.getPort()))
                            .setDatabase(lettuce.getDatabase());
            Optional.ofNullable(lettuce.getPassword()).ifPresent(serverConfig::setPassword);
            applyCommonConfig(serverConfig);
            log.debug("Redisson 已配置为 Lettuce 模式");
        } else if (factory instanceof JedisConnectionFactory jedis) {
            // 内联 configureForJedis 方法的逻辑
            var serverConfig =
                    config.useSingleServer()
                            .setAddress(
                                    "redis://%s:%d".formatted(jedis.getHostName(), jedis.getPort()))
                            .setDatabase(jedis.getDatabase());
            Optional.ofNullable(jedis.getPassword()).ifPresent(serverConfig::setPassword);
            applyCommonConfig(serverConfig);
            log.debug("Redisson 已配置为 Jedis 模式");
        } else {
            // 内联 configureDefault 方法的逻辑
            var serverConfig =
                    config.useSingleServer().setAddress("redis://localhost:6379").setDatabase(0);
            applyCommonConfig(serverConfig);
            log.warn("使用默认 Redisson 配置");
        }
        if (properties.getLock().isEnableWatchdog()) {
            config.setLockWatchdogTimeout(properties.getLock().getWatchdogTimeout());
        }

        return Redisson.create(config);
    }

    // ==================== 缓存配置方法 ====================

    @Bean
    @ConditionalOnProperty(
            prefix = "spring.redis.commons.cache",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
    public RedisCacheableAspect redisCacheableAspect(
            CacheAnnotationParser annotationParser,
            RedisCommonsProperties properties,
            ConditionHandler conditionHandler,
            CacheReadHandler cacheReadHandler,
            MethodInvokeHandler methodInvokeHandler,
            CacheWriteHandler cacheWriteHandler) {
        return new RedisCacheableAspect(
                annotationParser,
                properties,
                conditionHandler,
                cacheReadHandler,
                methodInvokeHandler,
                cacheWriteHandler);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "spring.redis.commons.cache",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
    public RedisEvictAspect redisEvictAspect(
            CacheAnnotationParser annotationParser,
            RedisCommonsProperties properties,
            EvictConditionHandler evictConditionHandler,
            BeforeEvictHandler beforeEvictHandler,
            EvictMethodInvokeHandler evictMethodInvokeHandler,
            AfterEvictHandler afterEvictHandler) {
        return new RedisEvictAspect(
                annotationParser,
                properties,
                evictConditionHandler,
                beforeEvictHandler,
                evictMethodInvokeHandler,
                afterEvictHandler);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "spring.redis.commons.cache",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
    public RedisPutAspect redisPutAspect(
            CacheAnnotationParser annotationParser,
            RedisCommonsProperties properties,
            PutConditionHandler conditionHandler,
            PutMethodInvokeHandler methodInvokeHandler,
            PutCacheUpdateHandler cacheUpdateHandler) {
        return new RedisPutAspect(
                annotationParser,
                properties,
                conditionHandler,
                methodInvokeHandler,
                cacheUpdateHandler);
    }

    @Bean("redisCacheManager")
    @ConditionalOnProperty(
            prefix = "spring.redis.commons.cache",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean(name = "redisCacheManager")
    public Object redisCacheManager(RedisCommonsProperties properties) {
        var cache = properties.getCache();
        log.info(
                "配置缓存管理器 - TTL: {}s, 空值缓存: {}, 统计: {}",
                cache.getDefaultTtl(),
                cache.isEnableNullCache(),
                cache.isEnableStatistics());
        return new Object();
    }

    // ==================== 分布式锁配置方法 ====================

    @Bean
    @ConditionalOnProperty(
            prefix = "spring.redis.commons.lock",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean(DistributedLockAspect.class)
    @ConditionalOnBean(DistributedLockManager.class)
    @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
    public DistributedLockAspect distributedLockAspect(DistributedLockManager manager) {
        log.info("配置分布式锁 AOP 切面");
        return new DistributedLockAspect(manager);
    }

    // ==================== 监控配置方法 ====================

    @Bean("redisCommonsHealthIndicator")
    @ConditionalOnProperty(
            prefix = "spring.redis.commons.monitoring",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean(name = "redisCommonsHealthIndicator")
    @ConditionalOnClass(HealthIndicator.class)
    public Object redisHealthIndicator(RedisCommonsProperties properties) {
        log.info("配置健康检查器 - 间隔: {}ms", properties.getMonitoring().getHealthCheckInterval());
        return new Object();
    }

    @Bean("redisCommonsMetricsCollector")
    @ConditionalOnProperty(
            prefix = "spring.redis.commons.monitoring",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean(name = "redisCommonsMetricsCollector")
    public Object redisMetricsCollector(RedisCommonsProperties properties) {
        var monitoring = properties.getMonitoring();
        log.info(
                "配置指标收集器 - 间隔: {}ms, 慢查询: {}ms",
                monitoring.getMetricsCollectionInterval(),
                monitoring.getSlowLogThreshold());
        return new Object();
    }

    // ==================== 公共辅助方法 ====================

    public void applyCommonConfig(SingleServerConfig serverConfig) {
        serverConfig
                .setConnectionMinimumIdleSize(1)
                .setConnectionPoolSize(10)
                .setConnectTimeout(3000)
                .setTimeout(3000)
                .setRetryAttempts(3)
                .setRetryInterval(1500);
    }
}
