package com.david.commons.redis.operations.impl;

import com.david.commons.redis.exception.RedisCommonsException;
import com.david.commons.redis.serialization.SerializationStrategySelector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RedisListOperationsImpl 单元测试
 *
 * @author David
 */
@ExtendWith(MockitoExtension.class)
class RedisListOperationsImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ListOperations<String, Object> listOperations;

    @Mock
    private SerializationStrategySelector strategySelector;

    private RedisListOperationsImpl<String> stringListOperations;

    private static final String TEST_KEY = "test:list";
    private static final String TEST_VALUE = "test_value";

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        stringListOperations = new RedisListOperationsImpl<>(redisTemplate, strategySelector, String.class);
    }

    @Test
    void testLeftPush_Success() {
        // Given
        when(listOperations.leftPush(TEST_KEY, TEST_VALUE)).thenReturn(1L);

        // When
        Long result = stringListOperations.leftPush(TEST_KEY, TEST_VALUE);

        // Then
        assertEquals(1L, result);
        verify(listOperations).leftPush(TEST_KEY, TEST_VALUE);
    }

    @Test
    void testLeftPush_Exception() {
        // Given
        when(listOperations.leftPush(TEST_KEY, TEST_VALUE)).thenThrow(new RuntimeException("Redis error"));

        // When & Then
        RedisCommonsException exception = assertThrows(RedisCommonsException.class,
                () -> stringListOperations.leftPush(TEST_KEY, TEST_VALUE));

        assertEquals("REDIS_LIST_LEFT_PUSH_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to left push value to list for key: " + TEST_KEY));
    }

    @Test
    void testLeftPushAll_VarArgs_Success() {
        // Given
        String[] values = { "value1", "value2", "value3" };
        when(listOperations.leftPushAll(eq(TEST_KEY), any(Object[].class))).thenReturn(3L);

        // When
        Long result = stringListOperations.leftPushAll(TEST_KEY, values);

        // Then
        assertEquals(3L, result);
        verify(listOperations).leftPushAll(eq(TEST_KEY), any(Object[].class));
    }

    @Test
    void testLeftPushAll_Collection_Success() {
        // Given
        List<String> values = Arrays.asList("value1", "value2", "value3");
        when(listOperations.leftPushAll(eq(TEST_KEY), any(Object[].class))).thenReturn(3L);

        // When
        Long result = stringListOperations.leftPushAll(TEST_KEY, values);

        // Then
        assertEquals(3L, result);
        verify(listOperations).leftPushAll(eq(TEST_KEY), any(Object[].class));
    }

    @Test
    void testLeftPushIfPresent_Success() {
        // Given
        when(listOperations.leftPushIfPresent(TEST_KEY, TEST_VALUE)).thenReturn(1L);

        // When
        Long result = stringListOperations.leftPushIfPresent(TEST_KEY, TEST_VALUE);

        // Then
        assertEquals(1L, result);
        verify(listOperations).leftPushIfPresent(TEST_KEY, TEST_VALUE);
    }

    @Test
    void testRightPush_Success() {
        // Given
        when(listOperations.rightPush(TEST_KEY, TEST_VALUE)).thenReturn(1L);

        // When
        Long result = stringListOperations.rightPush(TEST_KEY, TEST_VALUE);

        // Then
        assertEquals(1L, result);
        verify(listOperations).rightPush(TEST_KEY, TEST_VALUE);
    }

    @Test
    void testRightPushAll_VarArgs_Success() {
        // Given
        String[] values = { "value1", "value2", "value3" };
        when(listOperations.rightPushAll(eq(TEST_KEY), any(Object[].class))).thenReturn(3L);

        // When
        Long result = stringListOperations.rightPushAll(TEST_KEY, values);

        // Then
        assertEquals(3L, result);
        verify(listOperations).rightPushAll(eq(TEST_KEY), any(Object[].class));
    }

    @Test
    void testRightPushAll_Collection_Success() {
        // Given
        List<String> values = Arrays.asList("value1", "value2", "value3");
        when(listOperations.rightPushAll(eq(TEST_KEY), any(Object[].class))).thenReturn(3L);

        // When
        Long result = stringListOperations.rightPushAll(TEST_KEY, values);

        // Then
        assertEquals(3L, result);
        verify(listOperations).rightPushAll(eq(TEST_KEY), any(Object[].class));
    }

    @Test
    void testRightPushIfPresent_Success() {
        // Given
        when(listOperations.rightPushIfPresent(TEST_KEY, TEST_VALUE)).thenReturn(1L);

        // When
        Long result = stringListOperations.rightPushIfPresent(TEST_KEY, TEST_VALUE);

        // Then
        assertEquals(1L, result);
        verify(listOperations).rightPushIfPresent(TEST_KEY, TEST_VALUE);
    }

    @Test
    void testLeftPop_Success() {
        // Given
        when(listOperations.leftPop(TEST_KEY)).thenReturn(TEST_VALUE);

        // When
        String result = stringListOperations.leftPop(TEST_KEY);

        // Then
        assertEquals(TEST_VALUE, result);
        verify(listOperations).leftPop(TEST_KEY);
    }

    @Test
    void testLeftPop_Null() {
        // Given
        when(listOperations.leftPop(TEST_KEY)).thenReturn(null);

        // When
        String result = stringListOperations.leftPop(TEST_KEY);

        // Then
        assertNull(result);
        verify(listOperations).leftPop(TEST_KEY);
    }

    @Test
    void testLeftPop_WithTimeout_Success() {
        // Given
        when(listOperations.leftPop(TEST_KEY, 10, TimeUnit.SECONDS)).thenReturn(TEST_VALUE);

        // When
        String result = stringListOperations.leftPop(TEST_KEY, 10, TimeUnit.SECONDS);

        // Then
        assertEquals(TEST_VALUE, result);
        verify(listOperations).leftPop(TEST_KEY, 10, TimeUnit.SECONDS);
    }

    @Test
    void testRightPop_Success() {
        // Given
        when(listOperations.rightPop(TEST_KEY)).thenReturn(TEST_VALUE);

        // When
        String result = stringListOperations.rightPop(TEST_KEY);

        // Then
        assertEquals(TEST_VALUE, result);
        verify(listOperations).rightPop(TEST_KEY);
    }

    @Test
    void testRightPop_WithTimeout_Success() {
        // Given
        when(listOperations.rightPop(TEST_KEY, 10, TimeUnit.SECONDS)).thenReturn(TEST_VALUE);

        // When
        String result = stringListOperations.rightPop(TEST_KEY, 10, TimeUnit.SECONDS);

        // Then
        assertEquals(TEST_VALUE, result);
        verify(listOperations).rightPop(TEST_KEY, 10, TimeUnit.SECONDS);
    }

    @Test
    void testRange_Success() {
        // Given
        List<Object> values = Arrays.asList("value1", "value2", "value3");
        when(listOperations.range(TEST_KEY, 0, -1)).thenReturn(values);

        // When
        List<String> result = stringListOperations.range(TEST_KEY, 0, -1);

        // Then
        assertEquals(3, result.size());
        assertEquals("value1", result.get(0));
        assertEquals("value2", result.get(1));
        assertEquals("value3", result.get(2));
        verify(listOperations).range(TEST_KEY, 0, -1);
    }

    @Test
    void testRange_Null() {
        // Given
        when(listOperations.range(TEST_KEY, 0, -1)).thenReturn(null);

        // When
        List<String> result = stringListOperations.range(TEST_KEY, 0, -1);

        // Then
        assertNull(result);
        verify(listOperations).range(TEST_KEY, 0, -1);
    }

    @Test
    void testIndex_Success() {
        // Given
        when(listOperations.index(TEST_KEY, 0)).thenReturn(TEST_VALUE);

        // When
        String result = stringListOperations.index(TEST_KEY, 0);

        // Then
        assertEquals(TEST_VALUE, result);
        verify(listOperations).index(TEST_KEY, 0);
    }

    @Test
    void testIndex_Null() {
        // Given
        when(listOperations.index(TEST_KEY, 0)).thenReturn(null);

        // When
        String result = stringListOperations.index(TEST_KEY, 0);

        // Then
        assertNull(result);
        verify(listOperations).index(TEST_KEY, 0);
    }

    @Test
    void testSet_Success() {
        // Given
        doNothing().when(listOperations).set(TEST_KEY, 0, TEST_VALUE);

        // When
        stringListOperations.set(TEST_KEY, 0, TEST_VALUE);

        // Then
        verify(listOperations).set(TEST_KEY, 0, TEST_VALUE);
    }

    @Test
    void testSet_Exception() {
        // Given
        doThrow(new RuntimeException("Redis error")).when(listOperations).set(TEST_KEY, 0, TEST_VALUE);

        // When & Then
        RedisCommonsException exception = assertThrows(RedisCommonsException.class,
                () -> stringListOperations.set(TEST_KEY, 0, TEST_VALUE));

        assertEquals("REDIS_LIST_SET_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to set index in list for key: " + TEST_KEY));
    }

    @Test
    void testRemove_Success() {
        // Given
        when(listOperations.remove(TEST_KEY, 1, TEST_VALUE)).thenReturn(1L);

        // When
        Long result = stringListOperations.remove(TEST_KEY, 1, TEST_VALUE);

        // Then
        assertEquals(1L, result);
        verify(listOperations).remove(TEST_KEY, 1, TEST_VALUE);
    }

    @Test
    void testTrim_Success() {
        // Given
        doNothing().when(listOperations).trim(TEST_KEY, 0, 10);

        // When
        stringListOperations.trim(TEST_KEY, 0, 10);

        // Then
        verify(listOperations).trim(TEST_KEY, 0, 10);
    }

    @Test
    void testTrim_Exception() {
        // Given
        doThrow(new RuntimeException("Redis error")).when(listOperations).trim(TEST_KEY, 0, 10);

        // When & Then
        RedisCommonsException exception = assertThrows(RedisCommonsException.class,
                () -> stringListOperations.trim(TEST_KEY, 0, 10));

        assertEquals("REDIS_LIST_TRIM_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to trim list for key: " + TEST_KEY));
    }

    @Test
    void testSize_Success() {
        // Given
        when(listOperations.size(TEST_KEY)).thenReturn(5L);

        // When
        Long result = stringListOperations.size(TEST_KEY);

        // Then
        assertEquals(5L, result);
        verify(listOperations).size(TEST_KEY);
    }

    @Test
    void testSize_Exception() {
        // Given
        when(listOperations.size(TEST_KEY)).thenThrow(new RuntimeException("Redis error"));

        // When & Then
        RedisCommonsException exception = assertThrows(RedisCommonsException.class,
                () -> stringListOperations.size(TEST_KEY));

        assertEquals("REDIS_LIST_SIZE_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to get list size for key: " + TEST_KEY));
    }

    @Test
    void testTypeConversion_IntegerList() {
        // Given
        List<Object> values = Arrays.asList(1, 2, 3);
        when(listOperations.range(TEST_KEY, 0, -1)).thenReturn(values);

        RedisListOperationsImpl<Integer> intListOperations = new RedisListOperationsImpl<>(redisTemplate,
                strategySelector, Integer.class);

        // When
        List<Integer> result = intListOperations.range(TEST_KEY, 0, -1);

        // Then
        assertEquals(3, result.size());
        assertEquals(Integer.valueOf(1), result.get(0));
        assertEquals(Integer.valueOf(2), result.get(1));
        assertEquals(Integer.valueOf(3), result.get(2));
    }

    @Test
    void testTypeConversion_StringToNumber() {
        // Given
        when(listOperations.leftPop(TEST_KEY)).thenReturn("123");

        RedisListOperationsImpl<Integer> intListOperations = new RedisListOperationsImpl<>(redisTemplate,
                strategySelector, Integer.class);

        // When
        Integer result = intListOperations.leftPop(TEST_KEY);

        // Then
        assertEquals(Integer.valueOf(123), result);
    }

    @Test
    void testBatchOperations() {
        // Given
        String[] pushValues = { "value1", "value2", "value3" };
        List<Object> rangeValues = Arrays.asList("value1", "value2", "value3");

        when(listOperations.rightPushAll(eq(TEST_KEY), any(Object[].class))).thenReturn(3L);
        when(listOperations.range(TEST_KEY, 0, -1)).thenReturn(rangeValues);
        when(listOperations.size(TEST_KEY)).thenReturn(3L);

        // When - simulate batch operations
        Long pushResult = stringListOperations.rightPushAll(TEST_KEY, pushValues);
        List<String> getResult = stringListOperations.range(TEST_KEY, 0, -1);
        Long sizeResult = stringListOperations.size(TEST_KEY);

        // Then
        assertEquals(3L, pushResult);
        assertEquals(3, getResult.size());
        assertEquals("value1", getResult.get(0));
        assertEquals("value2", getResult.get(1));
        assertEquals("value3", getResult.get(2));
        assertEquals(3L, sizeResult);

        verify(listOperations).rightPushAll(eq(TEST_KEY), any(Object[].class));
        verify(listOperations).range(TEST_KEY, 0, -1);
        verify(listOperations).size(TEST_KEY);
    }
}