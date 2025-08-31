package com.david.commons.redis.serialization;

import com.david.commons.redis.serialization.impl.JdkRedisSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JDK Redis 序列化器测试
 *
 * @author David
 */
class JdkRedisSerializerTest {

    private JdkRedisSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new JdkRedisSerializer();
    }

    @Test
    void testSerializeAndDeserializeString() {
        String original = "Hello, JDK Serialization!";

        byte[] serialized = serializer.serialize(original);
        assertNotNull(serialized);
        assertTrue(serialized.length > 0);

        String deserialized = serializer.deserialize(serialized, String.class);
        assertEquals(original, deserialized);
    }

    @Test
    void testSerializeAndDeserializeInteger() {
        Integer original = 98765;

        byte[] serialized = serializer.serialize(original);
        assertNotNull(serialized);

        Integer deserialized = serializer.deserialize(serialized, Integer.class);
        assertEquals(original, deserialized);
    }

    @Test
    void testSerializeAndDeserializeList() {
        List<String> original = new ArrayList<>();
        original.add("jdk");
        original.add("serialization");
        original.add("compatible");

        byte[] serialized = serializer.serialize(original);
        assertNotNull(serialized);

        @SuppressWarnings("unchecked")
        List<String> deserialized = (List<String>) serializer.deserialize(serialized, List.class);
        assertEquals(original.size(), deserialized.size());
        assertEquals(original, deserialized);
    }

    @Test
    void testSerializeAndDeserializeMap() {
        Map<String, Object> original = new HashMap<>();
        original.put("string", "value");
        original.put("number", 42);
        original.put("boolean", true);

        byte[] serialized = serializer.serialize(original);
        assertNotNull(serialized);

        @SuppressWarnings("unchecked")
        Map<String, Object> deserialized = (Map<String, Object>) serializer.deserialize(serialized, Map.class);
        assertEquals(original, deserialized);
    }

    @Test
    void testSerializeAndDeserializeSerializableObject() {
        JdkSerializableTestObject original = new JdkSerializableTestObject("jdk-test", 777, true);

        byte[] serialized = serializer.serialize(original);
        assertNotNull(serialized);

        JdkSerializableTestObject deserialized = serializer.deserialize(serialized, JdkSerializableTestObject.class);
        assertEquals(original.getName(), deserialized.getName());
        assertEquals(original.getValue(), deserialized.getValue());
        assertEquals(original.isActive(), deserialized.isActive());
    }

    @Test
    void testSerializeNonSerializableObjectThrowsException() {
        NonSerializableObject nonSerializable = new NonSerializableObject("test");

        RedisSerializationException exception = assertThrows(RedisSerializationException.class,
                () -> serializer.serialize(nonSerializable));

        assertTrue(exception.getMessage().contains("Serializable"));
    }

    @Test
    void testSerializeNull() {
        byte[] serialized = serializer.serialize(null);
        assertNotNull(serialized);
        assertEquals(0, serialized.length);
    }

    @Test
    void testDeserializeNull() {
        String deserialized = serializer.deserialize(null, String.class);
        assertNull(deserialized);

        String deserializedEmpty = serializer.deserialize(new byte[0], String.class);
        assertNull(deserializedEmpty);
    }

    @Test
    void testGetType() {
        assertEquals(SerializationType.JDK, serializer.getType());
    }

    @Test
    void testSupports() {
        assertTrue(serializer.supports(String.class));
        assertTrue(serializer.supports(Integer.class));
        assertTrue(serializer.supports(ArrayList.class)); // ArrayList implements Serializable
        assertTrue(serializer.supports(HashMap.class)); // HashMap implements Serializable
        assertTrue(serializer.supports(JdkSerializableTestObject.class));

        // 测试不支持的类型
        assertFalse(serializer.supports(NonSerializableObject.class));
    }

    @Test
    void testIsSerializable() {
        assertTrue(serializer.isSerializable("test string"));
        assertTrue(serializer.isSerializable(123));
        assertTrue(serializer.isSerializable(new ArrayList<>()));
        assertTrue(serializer.isSerializable(new JdkSerializableTestObject("test", 1, true)));

        assertFalse(serializer.isSerializable(new NonSerializableObject("test")));
    }

    @Test
    void testGetSerializedSize() {
        String testString = "Hello, World!";
        long size = serializer.getSerializedSize(testString);
        assertTrue(size > 0);

        // 测试不可序列化对象
        NonSerializableObject nonSerializable = new NonSerializableObject("test");
        long invalidSize = serializer.getSerializedSize(nonSerializable);
        assertEquals(-1, invalidSize);
    }

    @Test
    void testDeserializeTypeMismatch() {
        String original = "This is a string";
        byte[] serialized = serializer.serialize(original);

        // 尝试反序列化为错误的类型
        RedisSerializationException exception = assertThrows(RedisSerializationException.class,
                () -> serializer.deserialize(serialized, Integer.class));

        assertTrue(exception.getMessage().contains("does not match expected type"));
    }

    /**
     * 可序列化的测试对象
     */
    public static class JdkSerializableTestObject implements Serializable {
        private static final long serialVersionUID = 1L;

        private String name;
        private int value;
        private boolean active;

        public JdkSerializableTestObject() {
        }

        public JdkSerializableTestObject(String name, int value, boolean active) {
            this.name = name;
            this.value = value;
            this.active = active;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }

    /**
     * 不可序列化的测试对象
     */
    public static class NonSerializableObject {
        private String name;

        public NonSerializableObject(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}