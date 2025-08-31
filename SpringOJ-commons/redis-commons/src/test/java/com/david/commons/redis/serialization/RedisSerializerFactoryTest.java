package com.david.commons.redis.serialization;

import com.david.commons.redis.serialization.impl.JdkRedisSerializer;
import com.david.commons.redis.serialization.impl.JsonRedisSerializer;
import com.david.commons.redis.serialization.impl.KryoRedisSerializer;
import com.david.commons.redis.serialization.impl.ProtobufRedisSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis 序列化器工厂测试
 *
 * @author David
 */
class RedisSerializerFactoryTest {

    private RedisSerializerFactory factory;

    @BeforeEach
    void setUp() {
        // 使用真实的序列化器实现
        JsonRedisSerializer jsonSerializer = new JsonRedisSerializer();
        KryoRedisSerializer kryoSerializer = new KryoRedisSerializer();
        JdkRedisSerializer jdkSerializer = new JdkRedisSerializer();
        ProtobufRedisSerializer protobufSerializer = new ProtobufRedisSerializer();

        factory = new RedisSerializerFactory(jsonSerializer, kryoSerializer, jdkSerializer, protobufSerializer);
    }

    @Test
    void testGetSerializerByType() {
        RedisSerializer<Object> jsonSer = factory.getSerializer(SerializationType.JSON);
        assertNotNull(jsonSer);
        assertEquals(SerializationType.JSON, jsonSer.getType());

        RedisSerializer<Object> kryoSer = factory.getSerializer(SerializationType.KRYO);
        assertNotNull(kryoSer);
        assertEquals(SerializationType.KRYO, kryoSer.getType());

        RedisSerializer<Object> jdkSer = factory.getSerializer(SerializationType.JDK);
        assertNotNull(jdkSer);
        assertEquals(SerializationType.JDK, jdkSer.getType());

        RedisSerializer<Object> protobufSer = factory.getSerializer(SerializationType.PROTOBUF);
        assertNotNull(protobufSer);
        assertEquals(SerializationType.PROTOBUF, protobufSer.getType());
    }

    @Test
    void testGetDefaultSerializer() {
        RedisSerializer<Object> defaultSerializer = factory.getDefaultSerializer();
        assertNotNull(defaultSerializer);
        assertEquals(SerializationType.JSON, defaultSerializer.getType());
    }

    @Test
    void testGetOptimalSerializerForPrimitiveTypes() {
        // 基本类型应该使用 JSON
        RedisSerializer<String> stringSerializer = factory.getOptimalSerializer(String.class);
        assertEquals(SerializationType.JSON, stringSerializer.getType());

        RedisSerializer<Integer> intSerializer = factory.getOptimalSerializer(Integer.class);
        assertEquals(SerializationType.JSON, intSerializer.getType());

        RedisSerializer<Boolean> boolSerializer = factory.getOptimalSerializer(Boolean.class);
        assertEquals(SerializationType.JSON, boolSerializer.getType());
    }

    @Test
    void testGetOptimalSerializerForSerializableObjects() {
        // 实现了 Serializable 的复杂对象应该使用 Kryo
        RedisSerializer<SerializableTestClass> serializer = factory.getOptimalSerializer(SerializableTestClass.class);
        assertEquals(SerializationType.KRYO, serializer.getType());
    }

    @Test
    void testGetOptimalSerializerForNonSerializableObjects() {
        // 不实现 Serializable 的对象应该使用 JSON
        RedisSerializer<NonSerializableTestClass> serializer = factory
                .getOptimalSerializer(NonSerializableTestClass.class);
        assertEquals(SerializationType.JSON, serializer.getType());
    }

    @Test
    void testSupports() {
        assertTrue(factory.supports(SerializationType.JSON, String.class));
        assertTrue(factory.supports(SerializationType.KRYO, SerializableTestClass.class));
        assertTrue(factory.supports(SerializationType.JDK, SerializableTestClass.class));
        assertFalse(factory.supports(SerializationType.PROTOBUF, String.class)); // Protobuf 当前不支持
    }

    @Test
    void testGetSupportedTypes() {
        SerializationType[] supportedTypes = factory.getSupportedTypes();
        assertNotNull(supportedTypes);
        assertEquals(4, supportedTypes.length);

        // 验证包含所有类型
        boolean hasJson = false, hasKryo = false, hasJdk = false, hasProtobuf = false;
        for (SerializationType type : supportedTypes) {
            switch (type) {
                case JSON -> hasJson = true;
                case KRYO -> hasKryo = true;
                case JDK -> hasJdk = true;
                case PROTOBUF -> hasProtobuf = true;
            }
        }

        assertTrue(hasJson);
        assertTrue(hasKryo);
        assertTrue(hasJdk);
        assertTrue(hasProtobuf);
    }

    @Test
    void testGetSerializerWithNullType() {
        assertThrows(NullPointerException.class, () -> factory.getSerializer(null));
    }

    @Test
    void testFactoryIntegration() {
        // 测试工厂的完整功能
        String testData = "Hello, Factory!";

        // 获取 JSON 序列化器并测试
        RedisSerializer<Object> jsonSerializer = factory.getSerializer(SerializationType.JSON);
        byte[] serialized = jsonSerializer.serialize(testData);
        String deserialized = jsonSerializer.deserialize(serialized, String.class);
        assertEquals(testData, deserialized);

        // 测试最优选择
        RedisSerializer<String> optimalSerializer = factory.getOptimalSerializer(String.class);
        assertEquals(SerializationType.JSON, optimalSerializer.getType());
    }

    /**
     * 实现了 Serializable 的测试类
     */
    public static class SerializableTestClass implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;

        public SerializableTestClass(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * 没有实现 Serializable 的测试类
     */
    public static class NonSerializableTestClass {
        private String name;

        public NonSerializableTestClass(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}