package com.david.commons.redis.serialization;

import com.david.commons.redis.serialization.impl.JsonRedisSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JSON Redis 序列化器测试
 *
 * @author David
 */
class JsonRedisSerializerTest {

    private JsonRedisSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new JsonRedisSerializer();
    }

    @Test
    void testSerializeAndDeserializeString() {
        String original = "Hello, World!";

        byte[] serialized = serializer.serialize(original);
        assertNotNull(serialized);
        assertTrue(serialized.length > 0);

        String deserialized = serializer.deserialize(serialized, String.class);
        assertEquals(original, deserialized);
    }

    @Test
    void testSerializeAndDeserializeInteger() {
        Integer original = 12345;

        byte[] serialized = serializer.serialize(original);
        assertNotNull(serialized);

        Integer deserialized = serializer.deserialize(serialized, Integer.class);
        assertEquals(original, deserialized);
    }

    @Test
    void testSerializeAndDeserializeList() {
        List<String> original = Arrays.asList("apple", "banana", "cherry");

        byte[] serialized = serializer.serialize(original);
        assertNotNull(serialized);

        @SuppressWarnings("unchecked")
        List<String> deserialized = (List<String>) serializer.deserialize(serialized, List.class);
        assertEquals(original.size(), deserialized.size());
        assertEquals(original.get(0), deserialized.get(0));
    }

    @Test
    void testSerializeAndDeserializeMap() {
        Map<String, Object> original = new HashMap<>();
        original.put("name", "John");
        original.put("age", 30);
        original.put("active", true);

        byte[] serialized = serializer.serialize(original);
        assertNotNull(serialized);

        @SuppressWarnings("unchecked")
        Map<String, Object> deserialized = (Map<String, Object>) serializer.deserialize(serialized, Map.class);
        assertEquals(original.get("name"), deserialized.get("name"));
        assertEquals(original.get("age"), deserialized.get("age"));
        assertEquals(original.get("active"), deserialized.get("active"));
    }

    @Test
    void testSerializeAndDeserializeLocalDateTime() {
        LocalDateTime original = LocalDateTime.now();

        byte[] serialized = serializer.serialize(original);
        assertNotNull(serialized);

        LocalDateTime deserialized = serializer.deserialize(serialized, LocalDateTime.class);
        assertEquals(original, deserialized);
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
        assertEquals(SerializationType.JSON, serializer.getType());
    }

    @Test
    void testSupports() {
        assertTrue(serializer.supports(String.class));
        assertTrue(serializer.supports(Integer.class));
        assertTrue(serializer.supports(List.class));
        assertTrue(serializer.supports(Map.class));
        assertTrue(serializer.supports(LocalDateTime.class));
    }

    @Test
    void testSerializeComplexObject() {
        TestObject original = new TestObject("test", 123, true);

        byte[] serialized = serializer.serialize(original);
        assertNotNull(serialized);

        TestObject deserialized = serializer.deserialize(serialized, TestObject.class);
        assertEquals(original.getName(), deserialized.getName());
        assertEquals(original.getValue(), deserialized.getValue());
        assertEquals(original.isActive(), deserialized.isActive());
    }

    /**
     * 测试用的简单对象
     */
    public static class TestObject {
        private String name;
        private int value;
        private boolean active;

        public TestObject() {
        }

        public TestObject(String name, int value, boolean active) {
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
}