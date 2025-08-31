package com.david.commons.redis.serialization;

import com.david.commons.redis.serialization.impl.KryoRedisSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kryo Redis 序列化器测试
 *
 * @author David
 */
class KryoRedisSerializerTest {

    private KryoRedisSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new KryoRedisSerializer();
    }

    @Test
    void testSerializeAndDeserializeString() {
        String original = "Hello, Kryo!";

        byte[] serialized = serializer.serialize(original);
        assertNotNull(serialized);
        assertTrue(serialized.length > 0);

        String deserialized = serializer.deserialize(serialized, String.class);
        assertEquals(original, deserialized);
    }

    @Test
    void testSerializeAndDeserializeInteger() {
        Integer original = 54321;

        byte[] serialized = serializer.serialize(original);
        assertNotNull(serialized);

        Integer deserialized = serializer.deserialize(serialized, Integer.class);
        assertEquals(original, deserialized);
    }

    @Test
    void testSerializeAndDeserializeList() {
        List<String> original = new ArrayList<>();
        original.add("kryo");
        original.add("serialization");
        original.add("test");

        byte[] serialized = serializer.serialize(original);
        assertNotNull(serialized);

        @SuppressWarnings("unchecked")
        List<String> deserialized = (List<String>) serializer.deserialize(serialized, Object.class);
        assertEquals(original.size(), deserialized.size());
        assertEquals(original.get(0), deserialized.get(0));
        assertEquals(original.get(1), deserialized.get(1));
        assertEquals(original.get(2), deserialized.get(2));
    }

    @Test
    void testSerializeAndDeserializeMap() {
        Map<String, Integer> original = new HashMap<>();
        original.put("one", 1);
        original.put("two", 2);
        original.put("three", 3);

        byte[] serialized = serializer.serialize(original);
        assertNotNull(serialized);

        @SuppressWarnings("unchecked")
        Map<String, Integer> deserialized = (Map<String, Integer>) serializer.deserialize(serialized, Object.class);
        assertEquals(original.size(), deserialized.size());
        assertEquals(original.get("one"), deserialized.get("one"));
        assertEquals(original.get("two"), deserialized.get("two"));
        assertEquals(original.get("three"), deserialized.get("three"));
    }

    @Test
    void testSerializeAndDeserializeSerializableObject() {
        SerializableTestObject original = new SerializableTestObject("kryo-test", 999);

        byte[] serialized = serializer.serialize(original);
        assertNotNull(serialized);

        SerializableTestObject deserialized = serializer.deserialize(serialized, SerializableTestObject.class);
        assertEquals(original.getName(), deserialized.getName());
        assertEquals(original.getValue(), deserialized.getValue());
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
        assertEquals(SerializationType.KRYO, serializer.getType());
    }

    @Test
    void testSupports() {
        assertTrue(serializer.supports(String.class));
        assertTrue(serializer.supports(Integer.class));
        assertTrue(serializer.supports(List.class));
        assertTrue(serializer.supports(Map.class));
        assertTrue(serializer.supports(SerializableTestObject.class));

        // 测试不支持的类型
        assertFalse(serializer.supports(NonSerializableTestObject.class));
    }

    @Test
    void testPerformanceComparison() {
        // 创建一个较大的对象来测试性能
        List<SerializableTestObject> largeList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            largeList.add(new SerializableTestObject("object-" + i, i));
        }

        long startTime = System.nanoTime();
        byte[] serialized = serializer.serialize(largeList);
        long serializeTime = System.nanoTime() - startTime;

        assertNotNull(serialized);
        assertTrue(serialized.length > 0);

        startTime = System.nanoTime();
        @SuppressWarnings("unchecked")
        List<SerializableTestObject> deserialized = (List<SerializableTestObject>) serializer.deserialize(serialized,
                Object.class);
        long deserializeTime = System.nanoTime() - startTime;

        assertEquals(largeList.size(), deserialized.size());

        System.out.printf("Kryo - Serialize: %d ns, Deserialize: %d ns, Size: %d bytes%n",
                serializeTime, deserializeTime, serialized.length);
    }

    /**
     * 可序列化的测试对象
     */
    public static class SerializableTestObject implements Serializable {
        private static final long serialVersionUID = 1L;

        private String name;
        private int value;

        public SerializableTestObject() {
        }

        public SerializableTestObject(String name, int value) {
            this.name = name;
            this.value = value;
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
    }

    /**
     * 不可序列化的测试对象
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