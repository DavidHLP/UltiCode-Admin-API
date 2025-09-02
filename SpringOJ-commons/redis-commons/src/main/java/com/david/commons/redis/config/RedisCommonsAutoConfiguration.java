package com.david.commons.redis.config;

import com.david.commons.redis.RedisUtils;
import com.david.commons.redis.cache.CacheAnnotationParser;
import com.david.commons.redis.cache.aspect.cacheable.RedisCacheableAspect;
import com.david.commons.redis.cache.aspect.evict.RedisEvictAspect;
import com.david.commons.redis.cache.aspect.put.RedisPutAspect;
import com.david.commons.redis.cache.expression.CacheExpressionEvaluator;
import com.david.commons.redis.cache.fallback.CacheFallbackHandler;
import com.david.commons.redis.cache.handler.CacheOperationHandler;
import com.david.commons.redis.cache.metrics.CacheMetricsCollector;
import com.david.commons.redis.lock.DistributedLockManager;
import com.david.commons.redis.lock.aspect.DistributedLockAspect;
import com.david.commons.redis.lock.impl.RedissonDistributedLockManager;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.ObjectProvider;
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
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Optional;

/**
 * Redis Commons 自动配置类
 *
 * <p>
 * 提供 Redis Commons 工具库的自动配置支持，采用模块化设计
 *
 * @author David
 */
@Slf4j
@AutoConfiguration(after = RedisAutoConfiguration.class)
@ConditionalOnClass(RedisTemplate.class)
@ConditionalOnProperty(prefix = "spring.redis.commons", name = "enabled", havingValue = "true", matchIfMissing = true)
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
            RedisConnectionFactory connectionFactory, RedisCommonsProperties properties,
            ObjectProvider<ObjectMapper> objectMapperProvider) {

        log.info(
                "初始化 Redis Commons Template - 键前缀: {}, 序列化: {}",
                properties.getKeyPrefix(),
                properties.getSerialization().getDefaultType());

        return createRedisTemplate(connectionFactory, resolveObjectMapper(objectMapperProvider));
    }

    /** 配置 Redisson 客户端 */
    @Bean
    @ConditionalOnMissingBean(RedissonClient.class)
    @ConditionalOnClass(RedissonClient.class)
    @ConditionalOnBean(RedisConnectionFactory.class)
    @ConditionalOnProperty(prefix = "spring.redis.commons.lock", name = "enabled", havingValue = "true", matchIfMissing = true)
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

    /**
     * 优先使用 Spring 上下文中的全局 ObjectMapper；若不存在，则创建一个内置配置：
     * - 注册 JavaTimeModule 支持 LocalDateTime 等
     * - 关闭时间戳写出
     * - 容忍未知字段
     */
    private ObjectMapper resolveObjectMapper(ObjectProvider<ObjectMapper> objectMapperProvider) {
        ObjectMapper mapper = objectMapperProvider.getIfAvailable();
        if (mapper != null) {
            // 复制并为 Redis 定制，避免影响全局 ObjectMapper
            return configureForRedis(mapper);
        }
        // 无全局 mapper 时，创建并定制一个本地的
        return configureForRedis(new ObjectMapper());
    }

    /**
     * 为 Redis 序列化定制 ObjectMapper：
     * - JavaTimeModule
     * - 日期以 ISO-8601 文本写出
     * - 忽略未知字段
     * - 启用默认多态类型信息（NON_FINAL，As.PROPERTY），写入类型元数据，确保反序列化得到目标类型
     */
    private ObjectMapper configureForRedis(ObjectMapper base) {
        ObjectMapper om = base.copy();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        var ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.david")
                .allowIfSubType("java.time")
                .allowIfSubType("java.util")
                .build();
        om.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        return om;
    }

    private RedisTemplate<String, Object> createRedisTemplate(RedisConnectionFactory factory,
            ObjectMapper objectMapper) {
        var template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(factory);

        // 键使用字符串序列化器，值使用通用 JSON 序列化器
        var stringSerializer = new StringRedisSerializer();
        var jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
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
        var serverConfig = config.useSingleServer()
                .setAddress(
                        "redis://%s:%d".formatted(factory.getHostName(), factory.getPort()))
                .setDatabase(factory.getDatabase());

        Optional.ofNullable(factory.getPassword()).ifPresent(serverConfig::setPassword);
        applyCommonConfig(serverConfig);

        log.debug("Redisson 已配置为 Lettuce 模式");
    }

    private void configureForJedis(Config config, JedisConnectionFactory factory) {
        var serverConfig = config.useSingleServer()
                .setAddress(
                        "redis://%s:%d".formatted(factory.getHostName(), factory.getPort()))
                .setDatabase(factory.getDatabase());

        Optional.ofNullable(factory.getPassword()).ifPresent(serverConfig::setPassword);
        applyCommonConfig(serverConfig);

        log.debug("Redisson 已配置为 Jedis 模式");
    }

    private void configureDefault(Config config) {
        var serverConfig = config.useSingleServer().setAddress("redis://localhost:6379").setDatabase(0);

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
    @ConditionalOnProperty(prefix = "spring.redis.commons.serialization", name = "enabled", havingValue = "true", matchIfMissing = true)
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
    @ConditionalOnProperty(prefix = "spring.redis.commons.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
    static class CacheConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public CacheAnnotationParser cacheAnnotationParser() {
            log.info("配置缓存注解解析器");
            return new CacheAnnotationParser();
        }

        @Bean
        @ConditionalOnMissingBean
        public CacheExpressionEvaluator cacheExpressionEvaluator() {
            log.info("配置缓存表达式求值器");
            return new CacheExpressionEvaluator();
        }

        @Bean
        @ConditionalOnMissingBean
        public CacheOperationHandler cacheOperationHandler(
                com.david.commons.redis.RedisUtils redisUtils,
                CacheExpressionEvaluator expressionEvaluator,
                RedisCommonsProperties properties) {
            log.info("配置缓存操作处理器");
            return new CacheOperationHandler(
                    redisUtils, expressionEvaluator, properties);
        }

        @Bean
        @ConditionalOnMissingBean
        public CacheFallbackHandler cacheFallbackHandler(
                RedisCommonsProperties properties) {
            log.info("配置缓存降级处理器 - 本地缓存降级: {}, 熔断器: {}",
                    properties.getCache().isEnableLocalCacheFallback(),
                    properties.getProtection().isEnableCircuitBreaker());
            return new CacheFallbackHandler(properties);
        }

        @Bean
        @ConditionalOnMissingBean
        public CacheMetricsCollector cacheMetricsCollector() {
            log.info("配置缓存指标收集器");
            return new CacheMetricsCollector();
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
        public RedisCacheableAspect redisCacheableAspect(
                com.david.commons.redis.RedisUtils redisUtils,
                CacheAnnotationParser annotationParser,
                CacheExpressionEvaluator expressionEvaluator,
                CacheOperationHandler operationHandler,
                CacheFallbackHandler fallbackHandler,
                CacheMetricsCollector metricsCollector,
                RedisCommonsProperties properties) {
            return new RedisCacheableAspect(
                    redisUtils, annotationParser, expressionEvaluator, operationHandler,
                    fallbackHandler, metricsCollector, properties);
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
        public RedisEvictAspect redisEvictAspect(
                com.david.commons.redis.RedisUtils redisUtils,
                CacheAnnotationParser annotationParser,
                CacheExpressionEvaluator expressionEvaluator,
                CacheOperationHandler operationHandler,
                CacheFallbackHandler fallbackHandler,
                CacheMetricsCollector metricsCollector,
                RedisCommonsProperties properties) {
            return new RedisEvictAspect(
                    redisUtils, annotationParser, expressionEvaluator, operationHandler,
                    fallbackHandler, metricsCollector, properties);
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
        public RedisPutAspect redisPutAspect(
                RedisUtils redisUtils,
                CacheAnnotationParser annotationParser,
                CacheExpressionEvaluator expressionEvaluator,
                CacheOperationHandler operationHandler,
                CacheFallbackHandler fallbackHandler,
                CacheMetricsCollector metricsCollector,
                RedisCommonsProperties properties) {
            return new RedisPutAspect(
                    redisUtils, annotationParser, expressionEvaluator, operationHandler,
                    fallbackHandler, metricsCollector, properties);
        }

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
    @ConditionalOnProperty(prefix = "spring.redis.commons.lock", name = "enabled", havingValue = "true", matchIfMissing = true)
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
    @ConditionalOnProperty(prefix = "spring.redis.commons.monitoring", name = "enabled", havingValue = "true", matchIfMissing = true)
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
