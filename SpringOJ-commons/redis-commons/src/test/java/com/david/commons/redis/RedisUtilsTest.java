package com.david.commons.redis;

import com.david.commons.redis.config.RedisCommonsProperties;
import com.david.commons.redis.lock.DistributedLockManager;
import com.david.commons.redis.operations.RedisCommonOperations;
import com.david.commons.redis.operations.RedisStringOperations;
import com.david.commons.redis.serialization.SerializationStrategySelector;
import com.david.commons.redis.serialization.SerializationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * RedisUtils 门面类单元测试
 *
 * @author David
 */
@ExtendWith(MockitoExtension.class)
class RedisUtilsTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private SerializationStrategySelector strategySelector;

    @Mock
    private DistributedLockManager lockManager;

    private RedisCommonsProperties properties;
    private RedisUtilsImpl redisUtils;

    @BeforeEach
    void setUp() {
        properties = new RedisCommonsProperties();
        properties.setKeyPrefix("test:");
        properties.getSerialization().setDefaultType(SerializationType.JSON);
        properties.getSerialization().setEnablePerformanceMonitoring(true);

        redisUtils = new RedisUtilsImpl(redisTemplate, properties, strategySelector, lockManager);
    }

    @Test
    void testGetOperations() {
        // 测试获取各种操作接口
        assertThat(redisUtils.string()).isNotNull();
        assertThat(redisUtils.hash()).isNotNull();
        assertThat(redisUtils.list()).isNotNull();
        assertThat(redisUtils.set()).isNotNull();
        assertThat(redisUtils.zset()).isNotNull();
        assertThat(redisUtils.common()).isNotNull();
        assertThat(redisUtils.lock()).isSameAs(lockManager);
    }

    @Test
    void testGetOperationsWithSerializationType() {
        // 测试指定序列化类型的操作接口
        RedisStringOperations<String> jsonOps = redisUtils.string(SerializationType.JSON);
        RedisStringOperations<String> kryoOps = redisUtils.string(SerializationType.KRYO);
        RedisStringOperations<String> jdkOps = redisUtils.string(SerializationType.JDK);

        assertThat(jsonOps).isNotNull();
        assertThat(kryoOps).isNotNull();
        assertThat(jdkOps).isNotNull();

        // 验证缓存机制：相同类型应该返回相同实例
        RedisStringOperations<String> jsonOps2 = redisUtils.string(SerializationType.JSON);
        assertThat(jsonOps).isSameAs(jsonOps2);
    }

    @Test
    void testKeyPrefixManagement() {
        // 测试键前缀管理
        assertThat(redisUtils.getKeyPrefix()).isEqualTo("test:");

        // 测试键构建
        String key = "my-key";
        String fullKey = redisUtils.buildKey(key);
        assertThat(fullKey).isEqualTo("test:my-key");

        // 测试避免重复前缀
        String keyWithPrefix = "test:another-key";
        String fullKeyWithPrefix = redisUtils.buildKey(keyWithPrefix);
        assertThat(fullKeyWithPrefix).isEqualTo(keyWithPrefix);

        // 测试设置新前缀
        redisUtils.setKeyPrefix("new:");
        assertThat(redisUtils.getKeyPrefix()).isEqualTo("new:");
        assertThat(redisUtils.buildKey(key)).isEqualTo("new:my-key");

        // 测试空前缀
        redisUtils.setKeyPrefix("");
        assertThat(redisUtils.getKeyPrefix()).isEqualTo("");
        assertThat(redisUtils.buildKey(key)).isEqualTo(key);

        // 测试 null 前缀
        redisUtils.setKeyPrefix(null);
        assertThat(redisUtils.getKeyPrefix()).isEqualTo("");
    }

    @Test
    void testSerializationTypeManagement() {
        // 测试序列化类型管理
        assertThat(redisUtils.getDefaultSerializationType()).isEqualTo(SerializationType.JSON);

        // 测试切换序列化策略
        redisUtils.switchSerializationStrategy(SerializationType.KRYO);
        assertThat(redisUtils.getDefaultSerializationType()).isEqualTo(SerializationType.KRYO);

        // 测试设置默认序列化类型
        redisUtils.setDefaultSerializationType(SerializationType.JDK);
        assertThat(redisUtils.getDefaultSerializationType()).isEqualTo(SerializationType.JDK);
    }

    @Test
    void testPerformanceMetrics() {
        // 测试性能指标
        RedisUtilsImpl.SerializationMetrics metrics = redisUtils.getSerializationMetrics(SerializationType.JSON);
        assertThat(metrics).isNotNull();
        assertThat(metrics.getTotalOperations()).isEqualTo(0);

        // 测试记录指标
        redisUtils.recordSerializationMetrics(SerializationType.JSON,
                java.time.Duration.ofMillis(100), true, 1024);

        metrics = redisUtils.getSerializationMetrics(SerializationType.JSON);
        assertThat(metrics.getTotalOperations()).isEqualTo(1);
        assertThat(metrics.getSuccessfulOperations()).isEqualTo(1);
        assertThat(metrics.getFailedOperations()).isEqualTo(0);
        assertThat(metrics.getSuccessRate()).isEqualTo(1.0);

        // 测试获取所有指标
        var allMetrics = redisUtils.getAllSerializationMetrics();
        assertThat(allMetrics).containsKey(SerializationType.JSON);

        // 测试重置指标
        redisUtils.resetSerializationMetrics();
        metrics = redisUtils.getSerializationMetrics(SerializationType.JSON);
        assertThat(metrics.getTotalOperations()).isEqualTo(0);
    }

    @Test
    void testCacheStats() {
        // 测试缓存统计
        String stats = redisUtils.getCacheStats();
        assertThat(stats).isNotNull();
        assertThat(stats).contains("Operation cache size");
    }

    @Test
    void testErrorHandling() {
        // 测试错误处理
        assertThrows(Exception.class, () -> {
            redisUtils.setDefaultSerializationType(null);
        });

        assertThrows(Exception.class, () -> {
            redisUtils.switchSerializationStrategy(null);
        });

        assertThrows(Exception.class, () -> {
            redisUtils.buildKey(null);
        });

        assertThrows(Exception.class, () -> {
            redisUtils.buildKey("");
        });

        assertThrows(Exception.class, () -> {
            redisUtils.buildKey("   ");
        });
    }

    @Test
    void testOperationCaching() {
        // 测试操作缓存机制
        RedisStringOperations<String> ops1 = redisUtils.string();
        RedisStringOperations<String> ops2 = redisUtils.string();

        // 相同的默认序列化类型应该返回相同的实例
        assertThat(ops1).isSameAs(ops2);

        // 不同的序列化类型应该返回不同的实例
        RedisStringOperations<String> kryoOps = redisUtils.string(SerializationType.KRYO);
        assertThat(ops1).isNotSameAs(kryoOps);

        // 切换默认序列化类型后，缓存应该被清除
        redisUtils.setDefaultSerializationType(SerializationType.KRYO);
        RedisStringOperations<String> ops3 = redisUtils.string();
        assertThat(ops1).isNotSameAs(ops3);
    }

    @Test
    void testCommonOperations() {
        // 测试通用操作
        RedisCommonOperations commonOps = redisUtils.common();
        assertThat(commonOps).isNotNull();

        // 多次调用应该返回相同实例（缓存）
        RedisCommonOperations commonOps2 = redisUtils.common();
        assertThat(commonOps).isSameAs(commonOps2);
    }

    @Test
    void testExceptionHandling() {
        // 测试异常处理方法
        Exception testException = new RuntimeException("Test exception");
        RuntimeException result = redisUtils.handleException("test-operation", "test-key", testException);

        assertThat(result).isNotNull();
        assertThat(result.getMessage()).contains("Redis operation failed: test-operation");
    }

    @Test
    void testHealthCheck() {
        // 测试健康检查（需要 mock Redis 连接）
        // 由于需要复杂的 mock 设置，这里只测试方法存在
        assertDoesNotThrow(() -> {
            boolean healthy = redisUtils.isHealthy();
            // 在没有真实 Redis 连接的情况下，预期会返回 false
            assertThat(healthy).isFalse();
        });
    }
}