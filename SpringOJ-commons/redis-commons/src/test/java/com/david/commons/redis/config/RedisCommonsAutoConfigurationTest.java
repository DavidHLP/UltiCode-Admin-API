package com.david.commons.redis.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Redis Commons 自动配置集成测试
 *
 * @author David
 */
@DisplayName("Redis Commons 自动配置测试")
class RedisCommonsAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    HealthContributorAutoConfiguration.class,
                    RedisCommonsAutoConfiguration.class));

    private final ApplicationContextRunner contextRunnerWithRedis = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    RedisAutoConfiguration.class,
                    HealthContributorAutoConfiguration.class,
                    RedisCommonsAutoConfiguration.class));

    @Test
    @DisplayName("禁用Redis Commons时不应该装配Bean")
    void testDisabledAutoConfiguration() {
        contextRunner
                .withPropertyValues("spring.redis.commons.enabled=false")
                .run(context -> {
                    // 验证Redis Commons相关Bean不存在
                    assertThat(context).doesNotHaveBean(RedisCommonsProperties.class);
                });
    }

    @Test
    @DisplayName("自定义配置属性应该生效")
    void testCustomProperties() {
        contextRunner
                .withPropertyValues(
                        "spring.redis.commons.key-prefix=test:",
                        "spring.redis.commons.cache.default-ttl=7200",
                        "spring.redis.commons.lock.default-wait-time=20",
                        "spring.redis.commons.serialization.default-type=KRYO")
                .run(context -> {
                    assertThat(context).hasSingleBean(RedisCommonsProperties.class);
                    RedisCommonsProperties properties = context.getBean(RedisCommonsProperties.class);

                    assertThat(properties.getKeyPrefix()).isEqualTo("test:");
                    assertThat(properties.getCache().getDefaultTtl()).isEqualTo(7200);
                    assertThat(properties.getLock().getDefaultWaitTime()).isEqualTo(20);
                    assertThat(properties.getSerialization().getDefaultType().name()).isEqualTo("KRYO");
                });
    }

    @Test
    @DisplayName("序列化配置禁用时不应该装配序列化相关Bean")
    void testSerializationConfigurationDisabled() {
        contextRunner
                .withPropertyValues("spring.redis.commons.serialization.enabled=false")
                .run(context -> {
                    // 验证序列化相关Bean不存在
                    assertThat(context).doesNotHaveBean("redisSerializerFactory");
                });
    }

    @Test
    @DisplayName("缓存配置禁用时不应该装配缓存相关Bean")
    void testCacheConfigurationDisabled() {
        contextRunner
                .withPropertyValues("spring.redis.commons.cache.enabled=false")
                .run(context -> {
                    // 验证缓存相关Bean不存在
                    assertThat(context).doesNotHaveBean("redisCacheManager");
                });
    }

    @Test
    @DisplayName("监控配置禁用时不应该装配监控相关Bean")
    void testMonitoringConfigurationDisabled() {
        contextRunner
                .withPropertyValues("spring.redis.commons.monitoring.enabled=false")
                .run(context -> {
                    // 验证监控相关Bean不存在
                    assertThat(context).doesNotHaveBean("redisCommonsHealthIndicator");
                    assertThat(context).doesNotHaveBean("redisCommonsMetricsCollector");
                });
    }

    @Test
    @DisplayName("条件注解应该正确工作")
    void testConditionalAnnotations() {
        // 测试没有RedisConnectionFactory时，RedisTemplate相关Bean不装配
        contextRunner
                .withPropertyValues("spring.redis.commons.enabled=true")
                .run(context -> {
                    // 有配置属性Bean
                    assertThat(context).hasSingleBean(RedisCommonsProperties.class);
                    // 但没有RedisTemplate相关Bean（因为缺少RedisConnectionFactory）
                    assertThat(context).doesNotHaveBean("redisCommonsTemplate");
                });
    }

    @Test
    @DisplayName("配置属性Bean应该正确创建")
    void testConfigurationPropertiesBean() {
        contextRunner
                .withPropertyValues("spring.redis.commons.enabled=true")
                .run(context -> {
                    // 验证配置属性Bean存在
                    assertThat(context).hasSingleBean(RedisCommonsProperties.class);
                    RedisCommonsProperties properties = context.getBean(RedisCommonsProperties.class);
                    assertThat(properties).isNotNull();
                    assertThat(properties.isEnabled()).isTrue();
                    assertThat(properties.getKeyPrefix()).isEqualTo("springoj:");
                });
    }

    @Test
    @DisplayName("嵌套配置类应该正确工作")
    void testNestedConfigurationClasses() {
        contextRunner
                .withPropertyValues("spring.redis.commons.enabled=true")
                .run(context -> {
                    // 验证嵌套配置类的Bean存在
                    assertThat(context).hasBean("redisSerializerFactory");
                    assertThat(context).hasBean("redisCacheManager");
                    assertThat(context).hasBean("redisCommonsHealthIndicator");
                    assertThat(context).hasBean("redisCommonsMetricsCollector");
                });
    }

    @Test
    @DisplayName("锁配置禁用时不应该装配锁相关Bean")
    void testLockConfigurationDisabled() {
        contextRunner
                .withPropertyValues("spring.redis.commons.lock.enabled=false")
                .run(context -> {
                    // 验证锁相关Bean不存在
                    assertThat(context).doesNotHaveBean("distributedLockManager");
                });
    }

    @Test
    @DisplayName("RedisTemplate配置应该正确")
    void testRedisTemplateConfiguration() {
        contextRunner
                .withPropertyValues(
                        "spring.redis.commons.enabled=true",
                        "spring.redis.commons.key-prefix=test:")
                .run(context -> {
                    // 验证配置属性正确加载
                    assertThat(context).hasSingleBean(RedisCommonsProperties.class);
                    RedisCommonsProperties properties = context.getBean(RedisCommonsProperties.class);
                    assertThat(properties.getKeyPrefix()).isEqualTo("test:");

                    // 没有RedisConnectionFactory时，不应该有RedisTemplate
                    assertThat(context).doesNotHaveBean("redisCommonsTemplate");
                });
    }

    @Test
    @DisplayName("防护配置应该正确加载")
    void testProtectionConfiguration() {
        contextRunner
                .withPropertyValues(
                        "spring.redis.commons.protection.enable-bloom-filter=false",
                        "spring.redis.commons.protection.enable-circuit-breaker=false",
                        "spring.redis.commons.protection.bloom-filter-expected-insertions=500000")
                .run(context -> {
                    assertThat(context).hasSingleBean(RedisCommonsProperties.class);
                    RedisCommonsProperties properties = context.getBean(RedisCommonsProperties.class);
                    assertThat(properties.getProtection().isEnableBloomFilter()).isFalse();
                    assertThat(properties.getProtection().isEnableCircuitBreaker()).isFalse();
                    assertThat(properties.getProtection().getBloomFilterExpectedInsertions()).isEqualTo(500000);
                });
    }

    @Test
    @DisplayName("监控配置应该正确加载")
    void testMonitoringConfiguration() {
        contextRunner
                .withPropertyValues(
                        "spring.redis.commons.monitoring.health-check-interval=60000",
                        "spring.redis.commons.monitoring.metrics-collection-interval=5000",
                        "spring.redis.commons.monitoring.slow-log-threshold=2000")
                .run(context -> {
                    assertThat(context).hasSingleBean(RedisCommonsProperties.class);
                    RedisCommonsProperties properties = context.getBean(RedisCommonsProperties.class);
                    assertThat(properties.getMonitoring().getHealthCheckInterval()).isEqualTo(60000);
                    assertThat(properties.getMonitoring().getMetricsCollectionInterval()).isEqualTo(5000);
                    assertThat(properties.getMonitoring().getSlowLogThreshold()).isEqualTo(2000);
                });
    }
}