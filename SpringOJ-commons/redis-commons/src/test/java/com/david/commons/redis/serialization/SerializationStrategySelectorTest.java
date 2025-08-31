package com.david.commons.redis.serialization;

import com.david.commons.redis.config.RedisCommonsProperties;
import com.david.commons.redis.serialization.impl.JdkRedisSerializer;
import com.david.commons.redis.serialization.impl.JsonRedisSerializer;
import com.david.commons.redis.serialization.impl.KryoRedisSerializer;
import com.david.commons.redis.serialization.impl.ProtobufRedisSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 序列化策略选择器测试
 *
 * @author David
 */
class SerializationStrategySelectorTest {

    private SerializationStrategySelector selector;
    private RedisSerializerFactory factory;
    private RedisCommonsProperties properties;

    @BeforeEach
    void setUp() {
        // 创建真实的序列化器
        JsonRedisSerializer jsonSerializer = new JsonRedisSerializer();
        KryoRedisSerializer kryoSerializer = new KryoRedisSerializer();
        JdkRedisSerializer jdkSerializer = new JdkRedisSerializer();
        ProtobufRedisSerializer protobufSerializer = new ProtobufRedisSerializer();

        factory = new RedisSerializerFactory(jsonSerializer, kryoSerializer, jdkSerializer, protobufSerializer);

        // 创建配置
        properties = new RedisCommonsProperties();
        properties.getSerialization().setDefaultType(SerializationType.JSON);

        selector = new SerializationStrategySelector(factory, properties);
    }

    @Test
    void testSelectStrategyForPrimitiveTypes() {
        // 基本类型应该选择 JSON
        assertEquals(SerializationType.JSON, selector.selectStrategy(String.class));
        assertEquals(SerializationType.JSON, selector.selectStrategy(Integer.class));
        assertEquals(SerializationType.JSON, selector.selectStrategy(Boolean.class));
        assertEquals(SerializationType.JSON, selector.selectStrategy(Double.class));
        assertEquals(SerializationType.JSON, selector.selectStrategy(int.class));
    }

    @Test
    void testSelectStrategyForCollectionTypes() {
        // 集合类型默认使用 JSON
        assertEquals(SerializationType.JSON, selector.selectStrategy(ArrayList.class));
        assertEquals(SerializationType.JSON, selector.selectStrategy(HashMap.class));
        assertEquals(SerializationType.JSON, selector.selectStrategy(List.class));
        assertEquals(SerializationType.JSON, selector.selectStrategy(Map.class));
    }

    @Test
    void testSelectStrategyForSerializableObjects() {
        // 实现了 Serializable 的对象应该使用 Kryo
        assertEquals(SerializationType.KRYO, selector.selectStrategy(SerializableTestObject.class));
    }

    @Test
    void testSelectStrategyForNonSerializableObjects() {
        // 不实现 Serializable 的对象应该使用 JSON
        assertEquals(SerializationType.JSON, selector.selectStrategy(NonSerializableTestObject.class));
    }

    @Test
    void testSelectStrategyWithObject() {
        // 测试基于对象实例的策略选择
        assertEquals(SerializationType.JSON, selector.selectStrategy("test string"));
        assertEquals(SerializationType.JSON, selector.selectStrategy(123));
        assertEquals(SerializationType.KRYO, selector.selectStrategy(new SerializableTestObject("test")));
        assertEquals(SerializationType.JSON, selector.selectStrategy(new NonSerializableTestObject("test")));
    }

    @Test
    void testSelectStrategyWithNull() {
        // null 对象应该返回默认策略
        assertEquals(SerializationType.JSON, selector.selectStrategy((Object) null));
        assertEquals(SerializationType.JSON, selector.selectStrategy((Class<?>) null));
    }

    @Test
    void testGetSerializerWithFallback() {
        // 测试获取序列化器并处理降级
        RedisSerializer<String> serializer = selector.getSerializerWithFallback(SerializationType.JSON, String.class);
        assertNotNull(serializer);
        assertEquals(SerializationType.JSON, serializer.getType());

        // 测试不支持的策略会降级
        RedisSerializer<String> fallbackSerializer = selector.getSerializerWithFallback(SerializationType.PROTOBUF,
                String.class);
        assertNotNull(fallbackSerializer);
        // Protobuf 不支持 String，应该降级到 JSON
        assertEquals(SerializationType.JSON, fallbackSerializer.getType());
    }

    @Test
    void testStrategyCache() {
        // 第一次调用会计算策略
        SerializationType strategy1 = selector.selectStrategy(String.class);
        assertEquals(SerializationType.JSON, strategy1);

        // 第二次调用应该使用缓存
        SerializationType strategy2 = selector.selectStrategy(String.class);
        assertEquals(strategy1, strategy2);

        // 验证缓存统计
        String stats = selector.getCacheStats();
        assertTrue(stats.contains("1 entries"));
    }

    @Test
    void testClearCache() {
        // 先添加一些缓存
        selector.selectStrategy(String.class);
        selector.selectStrategy(Integer.class);

        String statsBefore = selector.getCacheStats();
        assertTrue(statsBefore.contains("2 entries"));

        // 清除缓存
        selector.clearCache();

        String statsAfter = selector.getCacheStats();
        assertTrue(statsAfter.contains("0 entries"));
    }

    @Test
    void testManualStrategyOverride() {
        // 手动设置策略
        selector.setStrategyForType(String.class, SerializationType.JDK);
        assertEquals(SerializationType.JDK, selector.selectStrategy(String.class));

        // 移除手动设置的策略
        selector.removeStrategyForType(String.class);
        // 应该回到默认的策略选择逻辑
        assertEquals(SerializationType.JSON, selector.selectStrategy(String.class));
    }

    @Test
    void testManualStrategyOverrideWithUnsupportedType() {
        // 尝试为不支持的类型设置策略应该抛出异常
        assertThrows(IllegalArgumentException.class,
                () -> selector.setStrategyForType(String.class, SerializationType.PROTOBUF));
    }

    @Test
    void testConfigurationInfluence() {
        // 测试配置对策略选择的影响
        properties.getSerialization().setDefaultType(SerializationType.KRYO);

        // 重新创建选择器以使用新配置
        SerializationStrategySelector newSelector = new SerializationStrategySelector(factory, properties);

        // 集合类型在高性能配置下应该使用 Kryo
        assertEquals(SerializationType.KRYO, newSelector.selectStrategy(ArrayList.class));
    }

    @Test
    void testIntegrationWithRealSerialization() {
        // 端到端测试：选择策略并实际执行序列化
        String testData = "Hello, Strategy Selector!";

        SerializationType strategy = selector.selectStrategy(testData);
        assertEquals(SerializationType.JSON, strategy);

        RedisSerializer<String> serializer = selector.getSerializerWithFallback(strategy, String.class);

        // 执行序列化和反序列化
        byte[] serialized = serializer.serialize(testData);
        String deserialized = serializer.deserialize(serialized, String.class);

        assertEquals(testData, deserialized);
    }

    /**
     * 实现了 Serializable 的测试类
     */
    public static class SerializableTestObject implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;

        public SerializableTestObject(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * 没有实现 Serializable 的测试类
     */
    public static class NonSerializableTestObject {
        private String name;

        public NonSerializableTestObject(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}