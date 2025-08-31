package com.david.commons.redis.operations.impl;

import com.david.commons.redis.exception.RedisCommonsException;
import com.david.commons.redis.serialization.SerializationStrategySelector;
import com.david.commons.redis.serialization.RedisSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RedisHashOperationsImpl 单元测试
 *
 * @author David
 */
@ExtendWith(MockitoExtension.class)
class RedisHashOperationsImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private SerializationStrategySelector strategySelector;

    @Mock
    private RedisSerializer<Object> redisSerializer;

    private RedisHashOperationsImpl<String> stringHashOperations;

    private static final String TEST_KEY = "test:hash";
    private static final String TEST_HASH_KEY = "field1";
    private static final String TEST_VALUE = "test_value";

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        stringHashOperations = new RedisHashOperationsImpl<>(redisTemplate, strategySelector, String.class);
    }

    @Test
    void testPut_Success() {
        // Given
        doNothing().when(hashOperations).put(TEST_KEY, TEST_HASH_KEY, TEST_VALUE);

        // When
        stringHashOperations.put(TEST_KEY, TEST_HASH_KEY, TEST_VALUE);

        // Then
        verify(hashOperations).put(TEST_KEY, TEST_HASH_KEY, TEST_VALUE);
    }

    @Test
    void testPut_Exception() {
        // Given
        doThrow(new RuntimeException("Redis error")).when(hashOperations).put(TEST_KEY, TEST_HASH_KEY, TEST_VALUE);

        // When & Then
        RedisCommonsException exception = assertThrows(RedisCommonsException.class,
                () -> stringHashOperations.put(TEST_KEY, TEST_HASH_KEY, TEST_VALUE));

        assertEquals("REDIS_HASH_PUT_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to set hash field for key: " + TEST_KEY));
    }

    @Test
    void testPutAll_Success() {
        // Given
        Map<String, String> testMap = new HashMap<>();
        testMap.put("field1", "value1");
        testMap.put("field2", "value2");

        Map<String, Object> expectedObjectMap = new HashMap<>();
        expectedObjectMap.put("field1", "value1");
        expectedObjectMap.put("field2", "value2");

        doNothing().when(hashOperations).putAll(eq(TEST_KEY), eq(expectedObjectMap));

        // When
        stringHashOperations.putAll(TEST_KEY, testMap);

        // Then
        verify(hashOperations).putAll(eq(TEST_KEY), eq(expectedObjectMap));
    }

    @Test
    void testPutAll_Exception() {
        // Given
        Map<String, String> testMap = new HashMap<>();
        testMap.put("field1", "value1");

        doThrow(new RuntimeException("Redis error")).when(hashOperations).putAll(eq(TEST_KEY), any(Map.class));

        // When & Then
        RedisCommonsException exception = assertThrows(RedisCommonsException.class,
                () -> stringHashOperations.putAll(TEST_KEY, testMap));

        assertEquals("REDIS_HASH_PUT_ALL_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to set multiple hash fields for key: " + TEST_KEY));
    }

    @Test
    void testPutIfAbsent_Success() {
        // Given
        when(hashOperations.putIfAbsent(TEST_KEY, TEST_HASH_KEY, TEST_VALUE)).thenReturn(true);

        // When
        Boolean result = stringHashOperations.putIfAbsent(TEST_KEY, TEST_HASH_KEY, TEST_VALUE);

        // Then
        assertTrue(result);
        verify(hashOperations).putIfAbsent(TEST_KEY, TEST_HASH_KEY, TEST_VALUE);
    }

    @Test
    void testPutIfAbsent_AlreadyExists() {
        // Given
        when(hashOperations.putIfAbsent(TEST_KEY, TEST_HASH_KEY, TEST_VALUE)).thenReturn(false);

        // When
        Boolean result = stringHashOperations.putIfAbsent(TEST_KEY, TEST_HASH_KEY, TEST_VALUE);

        // Then
        assertFalse(result);
        verify(hashOperations).putIfAbsent(TEST_KEY, TEST_HASH_KEY, TEST_VALUE);
    }

    @Test
    void testPutIfAbsent_NullResult() {
        // Given
        when(hashOperations.putIfAbsent(TEST_KEY, TEST_HASH_KEY, TEST_VALUE)).thenReturn(null);

        // When
        Boolean result = stringHashOperations.putIfAbsent(TEST_KEY, TEST_HASH_KEY, TEST_VALUE);

        // Then
        assertFalse(result);
        verify(hashOperations).putIfAbsent(TEST_KEY, TEST_HASH_KEY, TEST_VALUE);
    }

    @Test
    void testGet_Success() {
        // Given
        when(hashOperations.get(TEST_KEY, TEST_HASH_KEY)).thenReturn(TEST_VALUE);

        // When
        String result = stringHashOperations.get(TEST_KEY, TEST_HASH_KEY);

        // Then
        assertEquals(TEST_VALUE, result);
        verify(hashOperations).get(TEST_KEY, TEST_HASH_KEY);
    }

    @Test
    void testGet_NotFound() {
        // Given
        when(hashOperations.get(TEST_KEY, TEST_HASH_KEY)).thenReturn(null);

        // When
        String result = stringHashOperations.get(TEST_KEY, TEST_HASH_KEY);

        // Then
        assertNull(result);
        verify(hashOperations).get(TEST_KEY, TEST_HASH_KEY);
    }

    @Test
    void testGet_TypeConversion() {
        // Given
        Integer intValue = 123;
        when(hashOperations.get(TEST_KEY, TEST_HASH_KEY)).thenReturn(intValue);

        RedisHashOperationsImpl<Integer> intHashOperations = new RedisHashOperationsImpl<>(redisTemplate,
                strategySelector, Integer.class);

        // When
        Integer result = intHashOperations.get(TEST_KEY, TEST_HASH_KEY);

        // Then
        assertEquals(intValue, result);
        verify(hashOperations).get(TEST_KEY, TEST_HASH_KEY);
    }

    @Test
    void testGet_StringToNumberConversion() {
        // Given
        String numberString = "456";
        when(hashOperations.get(TEST_KEY, TEST_HASH_KEY)).thenReturn(numberString);

        RedisHashOperationsImpl<Integer> intHashOperations = new RedisHashOperationsImpl<>(redisTemplate,
                strategySelector, Integer.class);

        // When
        Integer result = intHashOperations.get(TEST_KEY, TEST_HASH_KEY);

        // Then
        assertEquals(456, result);
        verify(hashOperations).get(TEST_KEY, TEST_HASH_KEY);
    }

    @Test
    void testGet_Exception() {
        // Given
        when(hashOperations.get(TEST_KEY, TEST_HASH_KEY)).thenThrow(new RuntimeException("Redis error"));

        // When & Then
        RedisCommonsException exception = assertThrows(RedisCommonsException.class,
                () -> stringHashOperations.get(TEST_KEY, TEST_HASH_KEY));

        assertEquals("REDIS_HASH_GET_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to get hash field for key: " + TEST_KEY));
    }

    @Test
    void testMultiGet_Success() {
        // Given
        Collection<String> hashKeys = Arrays.asList("field1", "field2", "field3");
        Collection<Object> objectHashKeys = Arrays.asList("field1", "field2", "field3");
        List<Object> values = Arrays.asList("value1", "value2", null);
        when(hashOperations.multiGet(TEST_KEY, objectHashKeys)).thenReturn(values);

        // When
        List<String> result = stringHashOperations.multiGet(TEST_KEY, hashKeys);

        // Then
        assertEquals(3, result.size());
        assertEquals("value1", result.get(0));
        assertEquals("value2", result.get(1));
        assertNull(result.get(2));
        verify(hashOperations).multiGet(eq(TEST_KEY), any(Collection.class));
    }

    @Test
    void testMultiGet_Exception() {
        // Given
        Collection<String> hashKeys = Arrays.asList("field1", "field2");
        Collection<Object> objectHashKeys = Arrays.asList("field1", "field2");
        when(hashOperations.multiGet(eq(TEST_KEY), any(Collection.class)))
                .thenThrow(new RuntimeException("Redis error"));

        // When & Then
        RedisCommonsException exception = assertThrows(RedisCommonsException.class,
                () -> stringHashOperations.multiGet(TEST_KEY, hashKeys));

        assertEquals("REDIS_HASH_MULTI_GET_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to get multiple hash fields for key: " + TEST_KEY));
    }

    @Test
    void testEntries_Success() {
        // Given
        Map<Object, Object> entries = new HashMap<>();
        entries.put("field1", "value1");
        entries.put("field2", "value2");
        when(hashOperations.entries(TEST_KEY)).thenReturn(entries);

        // When
        Map<String, String> result = stringHashOperations.entries(TEST_KEY);

        // Then
        assertEquals(2, result.size());
        assertEquals("value1", result.get("field1"));
        assertEquals("value2", result.get("field2"));
        verify(hashOperations).entries(TEST_KEY);
    }

    @Test
    void testEntries_Exception() {
        // Given
        when(hashOperations.entries(TEST_KEY)).thenThrow(new RuntimeException("Redis error"));

        // When & Then
        RedisCommonsException exception = assertThrows(RedisCommonsException.class,
                () -> stringHashOperations.entries(TEST_KEY));

        assertEquals("REDIS_HASH_ENTRIES_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to get all hash entries for key: " + TEST_KEY));
    }

    @Test
    void testKeys_Success() {
        // Given
        Set<Object> keys = new HashSet<>(Arrays.asList("field1", "field2"));
        when(hashOperations.keys(TEST_KEY)).thenReturn(keys);

        // When
        Set<String> result = stringHashOperations.keys(TEST_KEY);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains("field1"));
        assertTrue(result.contains("field2"));
        verify(hashOperations).keys(TEST_KEY);
    }

    @Test
    void testKeys_Exception() {
        // Given
        when(hashOperations.keys(TEST_KEY)).thenThrow(new RuntimeException("Redis error"));

        // When & Then
        RedisCommonsException exception = assertThrows(RedisCommonsException.class,
                () -> stringHashOperations.keys(TEST_KEY));

        assertEquals("REDIS_HASH_KEYS_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to get all hash keys for key: " + TEST_KEY));
    }

    @Test
    void testValues_Success() {
        // Given
        List<Object> values = Arrays.asList("value1", "value2");
        when(hashOperations.values(TEST_KEY)).thenReturn(values);

        // When
        List<String> result = stringHashOperations.values(TEST_KEY);

        // Then
        assertEquals(2, result.size());
        assertEquals("value1", result.get(0));
        assertEquals("value2", result.get(1));
        verify(hashOperations).values(TEST_KEY);
    }

    @Test
    void testValues_Exception() {
        // Given
        when(hashOperations.values(TEST_KEY)).thenThrow(new RuntimeException("Redis error"));

        // When & Then
        RedisCommonsException exception = assertThrows(RedisCommonsException.class,
                () -> stringHashOperations.values(TEST_KEY));

        assertEquals("REDIS_HASH_VALUES_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to get all hash values for key: " + TEST_KEY));
    }

    @Test
    void testDelete_Success() {
        // Given
        String[] hashKeys = { "field1", "field2" };
        when(hashOperations.delete(eq(TEST_KEY), any(Object[].class))).thenReturn(2L);

        // When
        Long result = stringHashOperations.delete(TEST_KEY, hashKeys);

        // Then
        assertEquals(2L, result);
        verify(hashOperations).delete(eq(TEST_KEY), any(Object[].class));
    }

    @Test
    void testDelete_Exception() {
        // Given
        String[] hashKeys = { "field1" };
        when(hashOperations.delete(eq(TEST_KEY), any(Object[].class))).thenThrow(new RuntimeException("Redis error"));

        // When & Then
        RedisCommonsException exception = assertThrows(RedisCommonsException.class,
                () -> stringHashOperations.delete(TEST_KEY, hashKeys));

        assertEquals("REDIS_HASH_DELETE_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to delete hash fields for key: " + TEST_KEY));
    }

    @Test
    void testHasKey_Success() {
        // Given
        when(hashOperations.hasKey(TEST_KEY, TEST_HASH_KEY)).thenReturn(true);

        // When
        Boolean result = stringHashOperations.hasKey(TEST_KEY, TEST_HASH_KEY);

        // Then
        assertTrue(result);
        verify(hashOperations).hasKey(TEST_KEY, TEST_HASH_KEY);
    }

    @Test
    void testHasKey_NotExists() {
        // Given
        when(hashOperations.hasKey(TEST_KEY, TEST_HASH_KEY)).thenReturn(false);

        // When
        Boolean result = stringHashOperations.hasKey(TEST_KEY, TEST_HASH_KEY);

        // Then
        assertFalse(result);
        verify(hashOperations).hasKey(TEST_KEY, TEST_HASH_KEY);
    }

    @Test
    void testHasKey_Exception() {
        // Given
        when(hashOperations.hasKey(TEST_KEY, TEST_HASH_KEY)).thenThrow(new RuntimeException("Redis error"));

        // When & Then
        RedisCommonsException exception = assertThrows(RedisCommonsException.class,
                () -> stringHashOperations.hasKey(TEST_KEY, TEST_HASH_KEY));

        assertEquals("REDIS_HASH_HAS_KEY_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to check hash field existence for key: " + TEST_KEY));
    }

    @Test
    void testSize_Success() {
        // Given
        when(hashOperations.size(TEST_KEY)).thenReturn(5L);

        // When
        Long result = stringHashOperations.size(TEST_KEY);

        // Then
        assertEquals(5L, result);
        verify(hashOperations).size(TEST_KEY);
    }

    @Test
    void testSize_Exception() {
        // Given
        when(hashOperations.size(TEST_KEY)).thenThrow(new RuntimeException("Redis error"));

        // When & Then
        RedisCommonsException exception = assertThrows(RedisCommonsException.class,
                () -> stringHashOperations.size(TEST_KEY));

        assertEquals("REDIS_HASH_SIZE_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to get hash size for key: " + TEST_KEY));
    }

    @Test
    void testIncrementLong_Success() {
        // Given
        long delta = 5L;
        when(hashOperations.increment(TEST_KEY, TEST_HASH_KEY, delta)).thenReturn(10L);

        // When
        Long result = stringHashOperations.increment(TEST_KEY, TEST_HASH_KEY, delta);

        // Then
        assertEquals(10L, result);
        verify(hashOperations).increment(TEST_KEY, TEST_HASH_KEY, delta);
    }

    @Test
    void testIncrementLong_Exception() {
        // Given
        long delta = 5L;
        when(hashOperations.increment(TEST_KEY, TEST_HASH_KEY, delta)).thenThrow(new RuntimeException("Redis error"));

        // When & Then
        RedisCommonsException exception = assertThrows(RedisCommonsException.class,
                () -> stringHashOperations.increment(TEST_KEY, TEST_HASH_KEY, delta));

        assertEquals("REDIS_HASH_INCREMENT_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to increment hash field for key: " + TEST_KEY));
    }

    @Test
    void testIncrementDouble_Success() {
        // Given
        double delta = 2.5;
        when(hashOperations.increment(TEST_KEY, TEST_HASH_KEY, delta)).thenReturn(7.5);

        // When
        Double result = stringHashOperations.increment(TEST_KEY, TEST_HASH_KEY, delta);

        // Then
        assertEquals(7.5, result);
        verify(hashOperations).increment(TEST_KEY, TEST_HASH_KEY, delta);
    }

    @Test
    void testIncrementDouble_Exception() {
        // Given
        double delta = 2.5;
        when(hashOperations.increment(TEST_KEY, TEST_HASH_KEY, delta)).thenThrow(new RuntimeException("Redis error"));

        // When & Then
        RedisCommonsException exception = assertThrows(RedisCommonsException.class,
                () -> stringHashOperations.increment(TEST_KEY, TEST_HASH_KEY, delta));

        assertEquals("REDIS_HASH_INCREMENT_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to increment hash field (double) for key: " + TEST_KEY));
    }

    @Test
    void testTypeConversion_StringToBoolean() {
        // Given
        String booleanString = "true";
        when(hashOperations.get(TEST_KEY, TEST_HASH_KEY)).thenReturn(booleanString);

        RedisHashOperationsImpl<Boolean> booleanHashOperations = new RedisHashOperationsImpl<>(redisTemplate,
                strategySelector, Boolean.class);

        // When
        Boolean result = booleanHashOperations.get(TEST_KEY, TEST_HASH_KEY);

        // Then
        assertTrue(result);
    }

    @Test
    void testTypeConversion_NumberToString() {
        // Given
        Double doubleValue = 123.45;
        when(hashOperations.get(TEST_KEY, TEST_HASH_KEY)).thenReturn(doubleValue);

        // When
        String result = stringHashOperations.get(TEST_KEY, TEST_HASH_KEY);

        // Then
        assertEquals("123.45", result);
    }

    @Test
    void testBatchOperations() {
        // Given
        Map<String, String> testMap = new HashMap<>();
        testMap.put("field1", "value1");
        testMap.put("field2", "value2");

        Collection<String> hashKeys = Arrays.asList("field1", "field2");
        Collection<Object> objectHashKeys = Arrays.asList("field1", "field2");
        List<Object> values = Arrays.asList("value1", "value2");

        Map<String, Object> expectedObjectMap = new HashMap<>();
        expectedObjectMap.put("field1", "value1");
        expectedObjectMap.put("field2", "value2");

        doNothing().when(hashOperations).putAll(eq(TEST_KEY), eq(expectedObjectMap));
        when(hashOperations.multiGet(eq(TEST_KEY), any(Collection.class))).thenReturn(values);
        when(hashOperations.size(TEST_KEY)).thenReturn(2L);

        // When - simulate batch operations
        stringHashOperations.putAll(TEST_KEY, testMap);
        List<String> getResult = stringHashOperations.multiGet(TEST_KEY, hashKeys);
        Long sizeResult = stringHashOperations.size(TEST_KEY);

        // Then
        assertEquals(2, getResult.size());
        assertEquals("value1", getResult.get(0));
        assertEquals("value2", getResult.get(1));
        assertEquals(2L, sizeResult);

        verify(hashOperations).putAll(eq(TEST_KEY), eq(expectedObjectMap));
        verify(hashOperations).multiGet(eq(TEST_KEY), any(Collection.class));
        verify(hashOperations).size(TEST_KEY);
    }

    @Test
    void testComplexTypeConversion() {
        // Given
        when(hashOperations.get(TEST_KEY, "intField")).thenReturn("789");
        when(hashOperations.get(TEST_KEY, "doubleField")).thenReturn("123.456");
        when(hashOperations.get(TEST_KEY, "boolField")).thenReturn("false");

        RedisHashOperationsImpl<Integer> intHashOperations = new RedisHashOperationsImpl<>(redisTemplate,
                strategySelector, Integer.class);
        RedisHashOperationsImpl<Double> doubleHashOperations = new RedisHashOperationsImpl<>(redisTemplate,
                strategySelector, Double.class);
        RedisHashOperationsImpl<Boolean> booleanHashOperations = new RedisHashOperationsImpl<>(redisTemplate,
                strategySelector, Boolean.class);

        // When
        Integer intResult = intHashOperations.get(TEST_KEY, "intField");
        Double doubleResult = doubleHashOperations.get(TEST_KEY, "doubleField");
        Boolean boolResult = booleanHashOperations.get(TEST_KEY, "boolField");

        // Then
        assertEquals(789, intResult);
        assertEquals(123.456, doubleResult);
        assertFalse(boolResult);
    }
}