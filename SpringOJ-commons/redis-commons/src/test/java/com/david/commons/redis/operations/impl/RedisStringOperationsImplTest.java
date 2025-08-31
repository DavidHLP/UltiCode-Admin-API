package com.david.commons.redis.operations.impl;

import com.david.commons.redis.exception.RedisCommonsException;
import com.david.commons.redis.serialization.SerializationStrategySelector;
import com.david.commons.redis.serialization.SerializationType;
import com.david.commons.redis.serialization.RedisSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RedisStringOperationsImpl 单元测试
 *
 * @author David
 */
@ExtendWith(MockitoExtension.class)
class RedisStringOperationsImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private SerializationStrategySelector strategySelector;

    @Mock
    private RedisSerializer<Object> redisSerializer;

    private RedisStringOperationsImpl<String> stringOperations;

    private static final String TEST_KEY = "test:key";
    private static final String TEST_VALUE = "test_value";

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        stringOperations = new RedisStringOperationsImpl<>(redisTemplate, strategySelector, String.class);
    }

    @Test
    void testSet_Success() {
        // Given
        doNothing().when(valueOperations).set(TEST_KEY, TEST_VALUE);

        // When
        Boolean result = stringOperations.set(TEST_KEY, TEST_VALUE);

        // Then
        assertTrue(result);
        verify(valueOperations).set(TEST_KEY, TEST_VALUE);
    }

    @Test
    void testSet_Exception() {
        // Given
        doThrow(new RuntimeException("Redis error")).when(valueOperations).set(TEST_KEY, TEST_VALUE);

        // When & Then
        RedisCommonsException exception = assertThrows(RedisCommonsException.class,
                () -> stringOperations.set(TEST_KEY, TEST_VALUE));

        assertEquals("REDIS_SET_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to set value for key: " + TEST_KEY));
    }

    @Test
    void testSetWithTimeout_Success() {
        // Given
        long timeout = 60;
        TimeUnit unit = TimeUnit.SECONDS;
        doNothing().when(valueOperations).set(TEST_KEY, TEST_VALUE, timeout, unit);

        // When
        Boolean result = stringOperations.set(TEST_KEY, TEST_VALUE, timeout, unit);

        // Then
        assertTrue(result);
        verify(valueOperations).set(TEST_KEY, TEST_VALUE, timeout, unit);
    }

    @Test
    void testSetWithDuration_Success() {
        // Given
        Duration duration = Duration.ofMinutes(5);
        doNothing().when(valueOperations).set(TEST_KEY, TEST_VALUE, duration);

        // When
        Boolean result = stringOperations.set(TEST_KEY, TEST_VALUE, duration);

        // Then
        assertTrue(result);
        verify(valueOperations).set(TEST_KEY, TEST_VALUE, duration);
    }

    @Test
    void testSetIfAbsent_Success() {
        // Given
        when(valueOperations.setIfAbsent(TEST_KEY, TEST_VALUE)).thenReturn(true);

        // When
        Boolean result = stringOperations.setIfAbsent(TEST_KEY, TEST_VALUE);

        // Then
        assertTrue(result);
        verify(valueOperations).setIfAbsent(TEST_KEY, TEST_VALUE);
    }

    @Test
    void testSetIfAbsent_AlreadyExists() {
        // Given
        when(valueOperations.setIfAbsent(TEST_KEY, TEST_VALUE)).thenReturn(false);

        // When
        Boolean result = stringOperations.setIfAbsent(TEST_KEY, TEST_VALUE);

        // Then
        assertFalse(result);
        verify(valueOperations).setIfAbsent(TEST_KEY, TEST_VALUE);
    }

    @Test
    void testSetIfAbsent_NullResult() {
        // Given
        when(valueOperations.setIfAbsent(TEST_KEY, TEST_VALUE)).thenReturn(null);

        // When
        Boolean result = stringOperations.setIfAbsent(TEST_KEY, TEST_VALUE);

        // Then
        assertFalse(result);
        verify(valueOperations).setIfAbsent(TEST_KEY, TEST_VALUE);
    }

    @Test
    void testSetIfAbsentWithTimeout_Success() {
        // Given
        long timeout = 30;
        TimeUnit unit = TimeUnit.SECONDS;
        when(valueOperations.setIfAbsent(TEST_KEY, TEST_VALUE, timeout, unit)).thenReturn(true);

        // When
        Boolean result = stringOperations.setIfAbsent(TEST_KEY, TEST_VALUE, timeout, unit);

        // Then
        assertTrue(result);
        verify(valueOperations).setIfAbsent(TEST_KEY, TEST_VALUE, timeout, unit);
    }

    @Test
    void testGet_Success() {
        // Given
        when(valueOperations.get(TEST_KEY)).thenReturn(TEST_VALUE);

        // When
        String result = stringOperations.get(TEST_KEY);

        // Then
        assertEquals(TEST_VALUE, result);
        verify(valueOperations).get(TEST_KEY);
    }

    @Test
    void testGet_NotFound() {
        // Given
        when(valueOperations.get(TEST_KEY)).thenReturn(null);

        // When
        String result = stringOperations.get(TEST_KEY);

        // Then
        assertNull(result);
        verify(valueOperations).get(TEST_KEY);
    }

    @Test
    void testGet_TypeConversion() {
        // Given
        Integer intValue = 123;
        when(valueOperations.get(TEST_KEY)).thenReturn(intValue);

        RedisStringOperationsImpl<Integer> intOperations = new RedisStringOperationsImpl<>(redisTemplate,
                strategySelector, Integer.class);

        // When
        Integer result = intOperations.get(TEST_KEY);

        // Then
        assertEquals(intValue, result);
        verify(valueOperations).get(TEST_KEY);
    }

    @Test
    void testGet_StringToNumberConversion() {
        // Given
        String numberString = "456";
        when(valueOperations.get(TEST_KEY)).thenReturn(numberString);

        RedisStringOperationsImpl<Integer> intOperations = new RedisStringOperationsImpl<>(redisTemplate,
                strategySelector, Integer.class);

        // When
        Integer result = intOperations.get(TEST_KEY);

        // Then
        assertEquals(456, result);
        verify(valueOperations).get(TEST_KEY);
    }

    @Test
    void testGet_Exception() {
        // Given
        when(valueOperations.get(TEST_KEY)).thenThrow(new RuntimeException("Redis error"));

        // When & Then
        RedisCommonsException exception = assertThrows(RedisCommonsException.class,
                () -> stringOperations.get(TEST_KEY));

        assertEquals("REDIS_GET_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to get value for key: " + TEST_KEY));
    }

    @Test
    void testGetAndSet_Success() {
        // Given
        String oldValue = "old_value";
        when(valueOperations.getAndSet(TEST_KEY, TEST_VALUE)).thenReturn(oldValue);

        // When
        String result = stringOperations.getAndSet(TEST_KEY, TEST_VALUE);

        // Then
        assertEquals(oldValue, result);
        verify(valueOperations).getAndSet(TEST_KEY, TEST_VALUE);
    }

    @Test
    void testGetAndSet_NullOldValue() {
        // Given
        when(valueOperations.getAndSet(TEST_KEY, TEST_VALUE)).thenReturn(null);

        // When
        String result = stringOperations.getAndSet(TEST_KEY, TEST_VALUE);

        // Then
        assertNull(result);
        verify(valueOperations).getAndSet(TEST_KEY, TEST_VALUE);
    }

    @Test
    void testIncrement_Success() {
        // Given
        Long expectedValue = 1L;
        when(valueOperations.increment(TEST_KEY)).thenReturn(expectedValue);

        // When
        Long result = stringOperations.increment(TEST_KEY);

        // Then
        assertEquals(expectedValue, result);
        verify(valueOperations).increment(TEST_KEY);
    }

    @Test
    void testIncrement_Exception() {
        // Given
        when(valueOperations.increment(TEST_KEY)).thenThrow(new RuntimeException("Redis error"));

        // When & Then
        RedisCommonsException exception = assertThrows(RedisCommonsException.class,
                () -> stringOperations.increment(TEST_KEY));

        assertEquals("REDIS_INCREMENT_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to increment value for key: " + TEST_KEY));
    }

    @Test
    void testIncrementWithDelta_Success() {
        // Given
        long delta = 5L;
        Long expectedValue = 10L;
        when(valueOperations.increment(TEST_KEY, delta)).thenReturn(expectedValue);

        // When
        Long result = stringOperations.increment(TEST_KEY, delta);

        // Then
        assertEquals(expectedValue, result);
        verify(valueOperations).increment(TEST_KEY, delta);
    }

    @Test
    void testDecrement_Success() {
        // Given
        Long expectedValue = -1L;
        when(valueOperations.decrement(TEST_KEY)).thenReturn(expectedValue);

        // When
        Long result = stringOperations.decrement(TEST_KEY);

        // Then
        assertEquals(expectedValue, result);
        verify(valueOperations).decrement(TEST_KEY);
    }

    @Test
    void testDecrement_Exception() {
        // Given
        when(valueOperations.decrement(TEST_KEY)).thenThrow(new RuntimeException("Redis error"));

        // When & Then
        RedisCommonsException exception = assertThrows(RedisCommonsException.class,
                () -> stringOperations.decrement(TEST_KEY));

        assertEquals("REDIS_DECREMENT_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to decrement value for key: " + TEST_KEY));
    }

    @Test
    void testDecrementWithDelta_Success() {
        // Given
        long delta = 3L;
        Long expectedValue = -5L;
        when(valueOperations.decrement(TEST_KEY, delta)).thenReturn(expectedValue);

        // When
        Long result = stringOperations.decrement(TEST_KEY, delta);

        // Then
        assertEquals(expectedValue, result);
        verify(valueOperations).decrement(TEST_KEY, delta);
    }

    @Test
    void testSize_Success() {
        // Given
        Long expectedSize = 10L;
        when(valueOperations.size(TEST_KEY)).thenReturn(expectedSize);

        // When
        Long result = stringOperations.size(TEST_KEY);

        // Then
        assertEquals(expectedSize, result);
        verify(valueOperations).size(TEST_KEY);
    }

    @Test
    void testSize_Exception() {
        // Given
        when(valueOperations.size(TEST_KEY)).thenThrow(new RuntimeException("Redis error"));

        // When & Then
        RedisCommonsException exception = assertThrows(RedisCommonsException.class,
                () -> stringOperations.size(TEST_KEY));

        assertEquals("REDIS_SIZE_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to get string length for key: " + TEST_KEY));
    }

    @Test
    void testTypeConversion_StringToBoolean() {
        // Given
        String booleanString = "true";
        when(valueOperations.get(TEST_KEY)).thenReturn(booleanString);

        RedisStringOperationsImpl<Boolean> booleanOperations = new RedisStringOperationsImpl<>(redisTemplate,
                strategySelector, Boolean.class);

        // When
        Boolean result = booleanOperations.get(TEST_KEY);

        // Then
        assertTrue(result);
    }

    @Test
    void testTypeConversion_NumberToString() {
        // Given
        Double doubleValue = 123.45;
        when(valueOperations.get(TEST_KEY)).thenReturn(doubleValue);

        // When
        String result = stringOperations.get(TEST_KEY);

        // Then
        assertEquals("123.45", result);
    }

    @Test
    void testChainedOperations() {
        // Given
        when(valueOperations.setIfAbsent(TEST_KEY, TEST_VALUE)).thenReturn(true);
        when(valueOperations.get(TEST_KEY)).thenReturn(TEST_VALUE);
        when(valueOperations.size(TEST_KEY)).thenReturn(10L);

        // When - simulate chained operations
        Boolean setResult = stringOperations.setIfAbsent(TEST_KEY, TEST_VALUE);
        String getValue = stringOperations.get(TEST_KEY);
        Long sizeResult = stringOperations.size(TEST_KEY);

        // Then
        assertTrue(setResult);
        assertEquals(TEST_VALUE, getValue);
        assertEquals(10L, sizeResult);

        verify(valueOperations).setIfAbsent(TEST_KEY, TEST_VALUE);
        verify(valueOperations).get(TEST_KEY);
        verify(valueOperations).size(TEST_KEY);
    }
}