package com.david.commons.redis.operations.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ZSetTupleImpl 单元测试
 *
 * @author David
 */
class ZSetTupleImplTest {

    @Test
    void testConstructorAndGetters() {
        // Given
        String value = "test_value";
        Double score = 1.5;

        // When
        ZSetTupleImpl<String> tuple = new ZSetTupleImpl<>(value, score);

        // Then
        assertEquals(value, tuple.getValue());
        assertEquals(score, tuple.getScore());
    }

    @Test
    void testConstructorWithNullValues() {
        // Given & When
        ZSetTupleImpl<String> tuple = new ZSetTupleImpl<>(null, null);

        // Then
        assertNull(tuple.getValue());
        assertNull(tuple.getScore());
    }

    @Test
    void testEquals_SameObject() {
        // Given
        ZSetTupleImpl<String> tuple = new ZSetTupleImpl<>("value", 1.0);

        // When & Then
        assertEquals(tuple, tuple);
    }

    @Test
    void testEquals_EqualObjects() {
        // Given
        ZSetTupleImpl<String> tuple1 = new ZSetTupleImpl<>("value", 1.0);
        ZSetTupleImpl<String> tuple2 = new ZSetTupleImpl<>("value", 1.0);

        // When & Then
        assertEquals(tuple1, tuple2);
        assertEquals(tuple2, tuple1);
    }

    @Test
    void testEquals_DifferentValues() {
        // Given
        ZSetTupleImpl<String> tuple1 = new ZSetTupleImpl<>("value1", 1.0);
        ZSetTupleImpl<String> tuple2 = new ZSetTupleImpl<>("value2", 1.0);

        // When & Then
        assertNotEquals(tuple1, tuple2);
    }

    @Test
    void testEquals_DifferentScores() {
        // Given
        ZSetTupleImpl<String> tuple1 = new ZSetTupleImpl<>("value", 1.0);
        ZSetTupleImpl<String> tuple2 = new ZSetTupleImpl<>("value", 2.0);

        // When & Then
        assertNotEquals(tuple1, tuple2);
    }

    @Test
    void testEquals_NullValue() {
        // Given
        ZSetTupleImpl<String> tuple1 = new ZSetTupleImpl<>(null, 1.0);
        ZSetTupleImpl<String> tuple2 = new ZSetTupleImpl<>(null, 1.0);
        ZSetTupleImpl<String> tuple3 = new ZSetTupleImpl<>("value", 1.0);

        // When & Then
        assertEquals(tuple1, tuple2);
        assertNotEquals(tuple1, tuple3);
        assertNotEquals(tuple3, tuple1);
    }

    @Test
    void testEquals_NullScore() {
        // Given
        ZSetTupleImpl<String> tuple1 = new ZSetTupleImpl<>("value", null);
        ZSetTupleImpl<String> tuple2 = new ZSetTupleImpl<>("value", null);
        ZSetTupleImpl<String> tuple3 = new ZSetTupleImpl<>("value", 1.0);

        // When & Then
        assertEquals(tuple1, tuple2);
        assertNotEquals(tuple1, tuple3);
        assertNotEquals(tuple3, tuple1);
    }

    @Test
    void testEquals_NullObject() {
        // Given
        ZSetTupleImpl<String> tuple = new ZSetTupleImpl<>("value", 1.0);

        // When & Then
        assertNotEquals(tuple, null);
    }

    @Test
    void testEquals_DifferentClass() {
        // Given
        ZSetTupleImpl<String> tuple = new ZSetTupleImpl<>("value", 1.0);
        String other = "not a tuple";

        // When & Then
        assertNotEquals(tuple, other);
    }

    @Test
    void testHashCode_EqualObjects() {
        // Given
        ZSetTupleImpl<String> tuple1 = new ZSetTupleImpl<>("value", 1.0);
        ZSetTupleImpl<String> tuple2 = new ZSetTupleImpl<>("value", 1.0);

        // When & Then
        assertEquals(tuple1.hashCode(), tuple2.hashCode());
    }

    @Test
    void testHashCode_DifferentObjects() {
        // Given
        ZSetTupleImpl<String> tuple1 = new ZSetTupleImpl<>("value1", 1.0);
        ZSetTupleImpl<String> tuple2 = new ZSetTupleImpl<>("value2", 1.0);

        // When & Then
        assertNotEquals(tuple1.hashCode(), tuple2.hashCode());
    }

    @Test
    void testHashCode_NullValues() {
        // Given
        ZSetTupleImpl<String> tuple1 = new ZSetTupleImpl<>(null, null);
        ZSetTupleImpl<String> tuple2 = new ZSetTupleImpl<>(null, null);

        // When & Then
        assertEquals(tuple1.hashCode(), tuple2.hashCode());
    }

    @Test
    void testToString() {
        // Given
        ZSetTupleImpl<String> tuple = new ZSetTupleImpl<>("test_value", 2.5);

        // When
        String result = tuple.toString();

        // Then
        assertTrue(result.contains("test_value"));
        assertTrue(result.contains("2.5"));
        assertTrue(result.contains("ZSetTupleImpl"));
    }

    @Test
    void testToString_NullValues() {
        // Given
        ZSetTupleImpl<String> tuple = new ZSetTupleImpl<>(null, null);

        // When
        String result = tuple.toString();

        // Then
        assertTrue(result.contains("null"));
        assertTrue(result.contains("ZSetTupleImpl"));
    }

    @Test
    void testGenericTypes() {
        // Given
        ZSetTupleImpl<Integer> intTuple = new ZSetTupleImpl<>(123, 4.5);
        ZSetTupleImpl<Double> doubleTuple = new ZSetTupleImpl<>(6.7, 8.9);

        // When & Then
        assertEquals(Integer.valueOf(123), intTuple.getValue());
        assertEquals(4.5, intTuple.getScore());
        assertEquals(Double.valueOf(6.7), doubleTuple.getValue());
        assertEquals(8.9, doubleTuple.getScore());
    }
}