package com.david.commons.redis.config;

import com.david.commons.redis.serialization.SerializationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RedisCommonsProperties 配置类单元测试
 *
 * @author David
 */
@DisplayName("Redis Commons 配置属性测试")
class RedisCommonsPropertiesTest {

    private Validator validator;
    private RedisCommonsProperties properties;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        properties = new RedisCommonsProperties();
    }

    @Test
    @DisplayName("默认配置应该有效")
    void testDefaultConfiguration() {
        Set<ConstraintViolation<RedisCommonsProperties>> violations = validator.validate(properties);
        assertTrue(violations.isEmpty(), "默认配置应该通过验证");
    }

    @Test
    @DisplayName("基础配置测试")
    void testBasicConfiguration() {
        // 测试默认值
        assertTrue(properties.isEnabled());
        assertEquals("springoj:", properties.getKeyPrefix());

        // 测试设置值
        properties.setEnabled(false);
        properties.setKeyPrefix("test:");

        assertFalse(properties.isEnabled());
        assertEquals("test:", properties.getKeyPrefix());
    }

    @Test
    @DisplayName("键前缀验证测试")
    void testKeyPrefixValidation() {
        // 空字符串应该失败
        properties.setKeyPrefix("");
        Set<ConstraintViolation<RedisCommonsProperties>> violations = validator.validate(properties);
        assertFalse(violations.isEmpty());

        // null 应该失败
        properties.setKeyPrefix(null);
        violations = validator.validate(properties);
        assertFalse(violations.isEmpty());

        // 有效字符串应该通过
        properties.setKeyPrefix("valid:");
        violations = validator.validate(properties);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("序列化配置测试")
    void testSerializationConfig() {
        RedisCommonsProperties.SerializationConfig config = properties.getSerialization();

        // 测试默认值
        assertEquals(SerializationType.JSON, config.getDefaultType());
        assertFalse(config.isEnableCompression());
        assertEquals(1024, config.getCompressionThreshold());

        // 测试设置值
        config.setDefaultType(SerializationType.KRYO);
        config.setEnableCompression(true);
        config.setCompressionThreshold(2048);

        assertEquals(SerializationType.KRYO, config.getDefaultType());
        assertTrue(config.isEnableCompression());
        assertEquals(2048, config.getCompressionThreshold());
    }

@Test
    @DisplayName("序列化配置验证测试")
    void testSerializationConfigValidation() {
        RedisCommonsProperties.SerializationConfig config = properties.getSerialization();

        // 压缩阈值不能小于1
        config.setCompressionThreshold(0);
        Set<ConstraintViolation<RedisCommonsProperties>> violations = validator.validate(properties);
        assertFalse(violations.isEmpty());

    // 序列化类型不能为null
        config.setCompressionThreshold(1024);
        config.setDefaultType(null);
        violations = validator.validate(properties);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("缓存配置测试")
    void testCacheConfig() {
        RedisCommonsProperties.CacheConfig config = properties.getCache();

        // 测试默认值
        assertEquals(3600, config.getDefaultTtl());
        assertTrue(config.isEnableNullCache());
        assertEquals(300, config.getNullCacheTtl());
        assertTrue(config.isEnableStatistics());
        assertEquals(250, config.getMaxKeyLength());

        // 测试设置值
        config.setDefaultTtl(7200);
        config.setEnableNullCache(false);
        config.setNullCacheTtl(600);
        config.setEnableStatistics(false);
        config.setMaxKeyLength(500);

        assertEquals(7200, config.getDefaultTtl());
        assertFalse(config.isEnableNullCache());
        assertEquals(600, config.getNullCacheTtl());
        assertFalse(config.isEnableStatistics());
        assertEquals(500, config.getMaxKeyLength());
    }

    @Test
    @DisplayName("缓存配置验证测试")
    void testCacheConfigValidation() {
        RedisCommonsProperties.CacheConfig config = properties.getCache();

        // TTL 不能小于1
        config.setDefaultTtl(0);
        Set<ConstraintViolation<RedisCommonsProperties>> violations = validator.validate(properties);
        assertFalse(violations.isEmpty());

        // 空值缓存TTL不能小于1
        config.setDefaultTtl(3600);
        config.setNullCacheTtl(0);
        violations = validator.validate(properties);
        assertFalse(violations.isEmpty());

        // 最大键长度不能小于1
        config.setNullCacheTtl(300);
        config.setMaxKeyLength(0);
        violations = validator.validate(properties);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("锁配置测试")
    void testLockConfig() {
        RedisCommonsProperties.LockConfig config = properties.getLock();

        // 测试默认值
        assertEquals(10, config.getDefaultWaitTime());
        assertEquals(30, config.getDefaultLeaseTime());
        assertTrue(config.isEnableWatchdog());
        assertEquals(30000, config.getWatchdogTimeout());
        assertEquals(100, config.getRetryInterval());

        // 测试设置值
        config.setDefaultWaitTime(20);
        config.setDefaultLeaseTime(60);
        config.setEnableWatchdog(false);
        config.setWatchdogTimeout(60000);
        config.setRetryInterval(200);

        assertEquals(20, config.getDefaultWaitTime());
        assertEquals(60, config.getDefaultLeaseTime());
        assertFalse(config.isEnableWatchdog());
        assertEquals(60000, config.getWatchdogTimeout());
        assertEquals(200, config.getRetryInterval());
    }

    @Test
    @DisplayName("锁配置验证测试")
    void testLockConfigValidation() {
        RedisCommonsProperties.LockConfig config = properties.getLock();

        // 等待时间不能小于0
        config.setDefaultWaitTime(-1);
        Set<ConstraintViolation<RedisCommonsProperties>> violations = validator.validate(properties);
        assertFalse(violations.isEmpty());

        // 持有时间不能小于1
        config.setDefaultWaitTime(10);
        config.setDefaultLeaseTime(0);
        violations = validator.validate(properties);
        assertFalse(violations.isEmpty());

        // 看门狗超时不能小于1000
        config.setDefaultLeaseTime(30);
        config.setWatchdogTimeout(999);
        violations = validator.validate(properties);
        assertFalse(violations.isEmpty());

        // 重试间隔不能小于10
        config.setWatchdogTimeout(30000);
        config.setRetryInterval(9);
        violations = validator.validate(properties);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("防护配置测试")
    void testProtectionConfig() {
        RedisCommonsProperties.ProtectionConfig config = properties.getProtection();

        // 测试默认值
        assertTrue(config.isEnableBloomFilter());
        assertEquals(1000000, config.getBloomFilterExpectedInsertions());
        assertEquals(0.01, config.getBloomFilterFpp());
        assertTrue(config.isEnableCircuitBreaker());
        assertEquals(5, config.getCircuitBreakerFailureThreshold());
        assertEquals(60000, config.getCircuitBreakerRecoveryTimeout());
        assertFalse(config.isEnableRateLimit());
        assertEquals(1000, config.getRateLimitPermitsPerSecond());

        // 测试设置值
        config.setEnableBloomFilter(false);
        config.setBloomFilterExpectedInsertions(2000000);
        config.setBloomFilterFpp(0.02);
        config.setEnableCircuitBreaker(false);
        config.setCircuitBreakerFailureThreshold(10);
        config.setCircuitBreakerRecoveryTimeout(120000);
        config.setEnableRateLimit(true);
        config.setRateLimitPermitsPerSecond(2000);

        assertFalse(config.isEnableBloomFilter());
        assertEquals(2000000, config.getBloomFilterExpectedInsertions());
        assertEquals(0.02, config.getBloomFilterFpp());
        assertFalse(config.isEnableCircuitBreaker());
        assertEquals(10, config.getCircuitBreakerFailureThreshold());
        assertEquals(120000, config.getCircuitBreakerRecoveryTimeout());
        assertTrue(config.isEnableRateLimit());
        assertEquals(2000, config.getRateLimitPermitsPerSecond());
    }

    @Test
    @DisplayName("防护配置验证测试")
    void testProtectionConfigValidation() {
        RedisCommonsProperties.ProtectionConfig config = properties.getProtection();

        // 布隆过滤器预期插入数不能小于1000
        config.setBloomFilterExpectedInsertions(999);
        Set<ConstraintViolation<RedisCommonsProperties>> violations = validator.validate(properties);
        assertFalse(violations.isEmpty());

        // 布隆过滤器误判率不能小于0
        config.setBloomFilterExpectedInsertions(1000000);
        config.setBloomFilterFpp(-0.01);
        violations = validator.validate(properties);
        assertFalse(violations.isEmpty());

        // 熔断器失败阈值不能小于1
        config.setBloomFilterFpp(0.01);
        config.setCircuitBreakerFailureThreshold(0);
        violations = validator.validate(properties);
        assertFalse(violations.isEmpty());

        // 熔断器恢复时间不能小于1000
        config.setCircuitBreakerFailureThreshold(5);
        config.setCircuitBreakerRecoveryTimeout(999);
        violations = validator.validate(properties);
        assertFalse(violations.isEmpty());

        // 限流每秒请求数不能小于1
        config.setCircuitBreakerRecoveryTimeout(60000);
        config.setRateLimitPermitsPerSecond(0);
        violations = validator.validate(properties);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("监控配置测试")
    void testMonitoringConfig() {
        RedisCommonsProperties.MonitoringConfig config = properties.getMonitoring();

        // 测试默认值
        assertTrue(config.isEnabled());
        assertTrue(config.isEnableHealthCheck());
        assertEquals(30000, config.getHealthCheckInterval());
        assertTrue(config.isEnableMetrics());
        assertEquals(10000, config.getMetricsCollectionInterval());
        assertTrue(config.isEnableSlowLog());
        assertEquals(1000, config.getSlowLogThreshold());

        // 测试设置值
        config.setEnabled(false);
        config.setEnableHealthCheck(false);
        config.setHealthCheckInterval(60000);
        config.setEnableMetrics(false);
        config.setMetricsCollectionInterval(20000);
        config.setEnableSlowLog(false);
        config.setSlowLogThreshold(2000);

        assertFalse(config.isEnabled());
        assertFalse(config.isEnableHealthCheck());
        assertEquals(60000, config.getHealthCheckInterval());
        assertFalse(config.isEnableMetrics());
        assertEquals(20000, config.getMetricsCollectionInterval());
        assertFalse(config.isEnableSlowLog());
        assertEquals(2000, config.getSlowLogThreshold());
    }

    @Test
    @DisplayName("监控配置验证测试")
    void testMonitoringConfigValidation() {
        RedisCommonsProperties.MonitoringConfig config = properties.getMonitoring();

        // 健康检查间隔不能小于1000
        config.setHealthCheckInterval(999);
        Set<ConstraintViolation<RedisCommonsProperties>> violations = validator.validate(properties);
        assertFalse(violations.isEmpty());

        // 指标收集间隔不能小于1000
        config.setHealthCheckInterval(30000);
        config.setMetricsCollectionInterval(999);
        violations = validator.validate(properties);
        assertFalse(violations.isEmpty());

        // 慢查询阈值不能小于1
        config.setMetricsCollectionInterval(10000);
        config.setSlowLogThreshold(0);
        violations = validator.validate(properties);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("嵌套配置对象不能为null")
    void testNestedConfigNotNull() {
        // 序列化配置不能为null
        properties.setSerialization(null);
        Set<ConstraintViolation<RedisCommonsProperties>> violations = validator.validate(properties);
        assertFalse(violations.isEmpty());

        // 重置
        properties.setSerialization(new RedisCommonsProperties.SerializationConfig());

        // 缓存配置不能为null
        properties.setCache(null);
        violations = validator.validate(properties);
        assertFalse(violations.isEmpty());

        // 重置
        properties.setCache(new RedisCommonsProperties.CacheConfig());

        // 锁配置不能为null
        properties.setLock(null);
        violations = validator.validate(properties);
        assertFalse(violations.isEmpty());

        // 重置
        properties.setLock(new RedisCommonsProperties.LockConfig());

        // 防护配置不能为null
        properties.setProtection(null);
        violations = validator.validate(properties);
        assertFalse(violations.isEmpty());

        // 重置
        properties.setProtection(new RedisCommonsProperties.ProtectionConfig());

        // 监控配置不能为null
        properties.setMonitoring(null);
        violations = validator.validate(properties);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("配置对象创建测试")
    void testConfigObjectCreation() {
        // 测试所有嵌套配置对象都能正确创建
        assertNotNull(properties.getSerialization());
        assertNotNull(properties.getCache());
        assertNotNull(properties.getLock());
        assertNotNull(properties.getProtection());
        assertNotNull(properties.getMonitoring());

        // 测试新创建的配置对象
        RedisCommonsProperties newProperties = new RedisCommonsProperties();
        assertNotNull(newProperties.getSerialization());
        assertNotNull(newProperties.getCache());
        assertNotNull(newProperties.getLock());
        assertNotNull(newProperties.getProtection());
        assertNotNull(newProperties.getMonitoring());
    }
}