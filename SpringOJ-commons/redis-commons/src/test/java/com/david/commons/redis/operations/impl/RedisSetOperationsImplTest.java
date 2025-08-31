package com.david.commons.redis.operations.impl;

import com.david.commons.redis.exception.RedisCommonsException;
import com.david.commons.redis.serialization.SerializationStrategySelector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RedisSetOperationsImpl 单元测试
 *
 * @author David
 */
@ExtendWith(MockitoExtension.class)
class RedisSetOperationsImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private SetOperations<String, Object> setOperations;

    @Mock
    private SerializationStrategySelector strategySelector;

    private RedisSetOperationsImpl<String> stringSetOperations;

    private static final String TEST_KEY = "test:set";
    private static final String TEST_DEST_KEY = "test:dest:set";
    private static final String TEST_VALUE = "test_value";

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        stringSetOperations = new RedisSetOperationsImpl<>(redisTemplate, strategySelector, String.class);
    }

    @Test
    void testAdd_Success() {
        // Given
        String[] values = { "value1", "value2", "value3" };
        when(setOperations.add(eq(TEST_KEY), any(Object[].class))).thenReturn(3L);

        // When
        Long result = stringSetOperations.add(TEST_KEY, values);

        // Then
        assertEquals(3L, result);
        verify(setOperations).add(eq(TEST_KEY), any(Object[].class));
    }

    @Test
    void testAdd_Exception() {
        // Given
        String[] values = { "value1" };
        when(setOperations.add(eq(TEST_KEY), any(Object[].class))).thenThrow(new RuntimeException("Redis error"));

        // When & Then
        RedisCommonsException exception = assertThrows(RedisCommonsException.class,
                () -> stringSetOperations.add(TEST_KEY, values));

        assertEquals("REDIS_SET_ADD_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to add values to set for key: " + TEST_KEY));
    }

    @Test
    void testRemove_Success() {
        // Given
        String[] values = { "value1", "value2" };
        when(setOperations.remove(eq(TEST_KEY), any(Object[].class))).thenReturn(2L);

        // When
        Long result = stringSetOperations.remove(TEST_KEY, values);

        // Then
        assertEquals(2L, result);
        verify(setOperations).remove(eq(TEST_KEY), any(Object[].class));
    }

    @Test
    void testRemove_Exception() {
        // Given
        String[] values = { "value1" };
        when(setOperations.remove(eq(TEST_KEY), any(Object[].class))).thenThrow(new RuntimeException("Redis error"));

        // When & Then
        RedisCommonsException exception = assertThrows(RedisCommonsException.class,
                () -> stringSetOperations.remove(TEST_KEY, values));

        assertEquals("REDIS_SET_REMOVE_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to remove values from set for key: " + TEST_KEY));
    }

    @Test
    void testPop_Success() {
        // Given
        when(setOperations.pop(TEST_KEY)).thenReturn(TEST_VALUE);

        // When
        String result = stringSetOperations.pop(TEST_KEY);

        // Then
        assertEquals(TEST_VALUE, result);
        verify(setOperations).pop(TEST_KEY);
    }

    @Test
    void testPop_Null() {
        // Given
        when(setOperations.pop(TEST_KEY)).thenReturn(null);

        // When
        String result = stringSetOperations.pop(TEST_KEY);

        // Then
        assertNull(result);
        verify(setOperations).pop(TEST_KEY);
    }

    @Test
    void testPop_WithCount_Success() {
        // Given
        List<Object> values = Arrays.asList("value1", "value2", "value3");
        when(setOperations.pop(TEST_KEY, 3)).thenReturn(values);

        // When
        List<String> result = stringSetOperations.pop(TEST_KEY, 3);

        // Then
        assertEquals(3, result.size());
        assertEquals("value1", result.get(0));
        assertEquals("value2", result.get(1));
        assertEquals("value3", result.get(2));
        verify(setOperations).pop(TEST_KEY, 3);
    }

    @Test
    void testPop_WithCount_Null() {
        // Given
        when(setOperations.pop(TEST_KEY, 3)).thenReturn(null);

        // When
        List<String> result = stringSetOperations.pop(TEST_KEY, 3);

        // Then
        assertNull(result);
        verify(setOperations).pop(TEST_KEY, 3);
    }

    @Test
    void testMove_Success() {
        // Given
        when(setOperations.move(TEST_KEY, TEST_VALUE, TEST_DEST_KEY)).thenReturn(true);

        // When
        Boolean result = stringSetOperations.move(TEST_KEY, TEST_VALUE, TEST_DEST_KEY);

        // Then
        assertTrue(result);
        verify(setOperations).move(TEST_KEY, TEST_VALUE, TEST_DEST_KEY);
    }

    @Test
    void testMove_Failed() {
        // Given
        when(setOperations.move(TEST_KEY, TEST_VALUE, TEST_DEST_KEY)).thenReturn(false);

        // When
        Boolean result = stringSetOperations.move(TEST_KEY, TEST_VALUE, TEST_DEST_KEY);

        // Then
        assertFalse(result);
        verify(setOperations).move(TEST_KEY, TEST_VALUE, TEST_DEST_KEY);
    }

    @Test
    void testSize_Success() {
        // Given
        when(setOperations.size(TEST_KEY)).thenReturn(5L);

        // When
        Long result = stringSetOperations.size(TEST_KEY);

        // Then
        assertEquals(5L, result);
        verify(setOperations).size(TEST_KEY);
    }

    @Test
    void testSize_Exception() {
        // Given
        when(setOperations.size(TEST_KEY)).thenThrow(new RuntimeException("Redis error"));

        // When & Then
        RedisCommonsException exception = assertThrows(RedisCommonsException.class,
                () -> stringSetOperations.size(TEST_KEY));

        assertEquals("REDIS_SET_SIZE_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to get set size for key: " + TEST_KEY));
    }

    @Test
    void testIsMember_Success() {
        // Given
        when(setOperations.isMember(TEST_KEY, TEST_VALUE)).thenReturn(true);

        // When
        Boolean result = stringSetOperations.isMember(TEST_KEY, TEST_VALUE);

        // Then
        assertTrue(result);
        verify(setOperations).isMember(TEST_KEY, TEST_VALUE);
    }

    @Test
    void testIsMember_NotMember() {
        // Given
        when(setOperations.isMember(TEST_KEY, TEST_VALUE)).thenReturn(false);

        // When
        Boolean result = stringSetOperations.isMember(TEST_KEY, TEST_VALUE);

        // Then
        assertFalse(result);
        verify(setOperations).isMember(TEST_KEY, TEST_VALUE);
    }

    @Test
    void testIsMember_Multiple_Success() {
        // Given
        Object[] values = { "value1", "value2", "value3" };
        Map<Object, Boolean> membershipMap = new HashMap<>();
        membershipMap.put("value1", true);
        membershipMap.put("value2", false);
        membershipMap.put("value3", true);
        when(setOperations.isMember(TEST_KEY, values)).thenReturn(membershipMap);

        // When
        Map<Object, Boolean> result = stringSetOperations.isMember(TEST_KEY, values);

        // Then
        assertEquals(3, result.size());
        assertTrue(result.get("value1"));
        assertFalse(result.get("value2"));
        assertTrue(result.get("value3"));
        verify(setOperations).isMember(TEST_KEY, values);
    }

    @Test
    void testMembers_Success() {
        // Given
        Set<Object> values = new HashSet<>(Arrays.asList("value1", "value2", "value3"));
        when(setOperations.members(TEST_KEY)).thenReturn(values);

        // When
        Set<String> result = stringSetOperations.members(TEST_KEY);

        // Then
        assertEquals(3, result.size());
        assertTrue(result.contains("value1"));
        assertTrue(result.contains("value2"));
        assertTrue(result.contains("value3"));
        verify(setOperations).members(TEST_KEY);
    }

    @Test
    void testMembers_Null() {
        // Given
        when(setOperations.members(TEST_KEY)).thenReturn(null);

        // When
        Set<String> result = stringSetOperations.members(TEST_KEY);

        // Then
        assertNull(result);
        verify(setOperations).members(TEST_KEY);
    }

    @Test
    void testRandomMember_Success() {
        // Given
        when(setOperations.randomMember(TEST_KEY)).thenReturn(TEST_VALUE);

        // When
        String result = stringSetOperations.randomMember(TEST_KEY);

        // Then
        assertEquals(TEST_VALUE, result);
        verify(setOperations).randomMember(TEST_KEY);
    }

    @Test
    void testRandomMember_Null() {
        // Given
        when(setOperations.randomMember(TEST_KEY)).thenReturn(null);

        // When
        String result = stringSetOperations.randomMember(TEST_KEY);

        // Then
        assertNull(result);
        verify(setOperations).randomMember(TEST_KEY);
    }

    @Test
    void testRandomMembers_Success() {
        // Given
        List<Object> values = Arrays.asList("value1", "value2", "value1"); // 可能有重复
        when(setOperations.randomMembers(TEST_KEY, 3)).thenReturn(values);

        // When
        List<String> result = stringSetOperations.randomMembers(TEST_KEY, 3);

        // Then
        assertEquals(3, result.size());
        assertEquals("value1", result.get(0));
        assertEquals("value2", result.get(1));
        assertEquals("value1", result.get(2));
        verify(setOperations).randomMembers(TEST_KEY, 3);
    }

    @Test
    void testDistinctRandomMembers_Success() {
        // Given
        Set<Object> values = new HashSet<>(Arrays.asList("value1", "value2", "value3"));
        when(setOperations.distinctRandomMembers(TEST_KEY, 3)).thenReturn(values);

        // When
        Set<String> result = stringSetOperations.distinctRandomMembers(TEST_KEY, 3);

        // Then
        assertEquals(3, result.size());
        assertTrue(result.contains("value1"));
        assertTrue(result.contains("value2"));
        assertTrue(result.contains("value3"));
        verify(setOperations).distinctRandomMembers(TEST_KEY, 3);
    }

    @Test
    void testIntersect_VarArgs_Success() {
        // Given
        String[] otherKeys = { "key2", "key3" };
        Set<Object> values = new HashSet<>(Arrays.asList("value1", "value2"));
        when(setOperations.intersect(eq(TEST_KEY), any(Collection.class))).thenReturn(values);

        // When
        Set<String> result = stringSetOperations.intersect(TEST_KEY, otherKeys);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains("value1"));
        assertTrue(result.contains("value2"));
        verify(setOperations).intersect(eq(TEST_KEY), any(Collection.class));
    }

    @Test
    void testIntersect_Collection_Success() {
        // Given
        Collection<String> otherKeys = Arrays.asList("key2", "key3");
        Set<Object> values = new HashSet<>(Arrays.asList("value1", "value2"));
        when(setOperations.intersect(TEST_KEY, otherKeys)).thenReturn(values);

        // When
        Set<String> result = stringSetOperations.intersect(TEST_KEY, otherKeys);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains("value1"));
        assertTrue(result.contains("value2"));
        verify(setOperations).intersect(TEST_KEY, otherKeys);
    }

    @Test
    void testUnion_VarArgs_Success() {
        // Given
        String[] otherKeys = { "key2", "key3" };
        Set<Object> values = new HashSet<>(Arrays.asList("value1", "value2", "value3", "value4"));
        when(setOperations.union(eq(TEST_KEY), any(Collection.class))).thenReturn(values);

        // When
        Set<String> result = stringSetOperations.union(TEST_KEY, otherKeys);

        // Then
        assertEquals(4, result.size());
        assertTrue(result.contains("value1"));
        assertTrue(result.contains("value2"));
        assertTrue(result.contains("value3"));
        assertTrue(result.contains("value4"));
        verify(setOperations).union(eq(TEST_KEY), any(Collection.class));
    }

    @Test
    void testUnion_Collection_Success() {
        // Given
        Collection<String> otherKeys = Arrays.asList("key2", "key3");
        Set<Object> values = new HashSet<>(Arrays.asList("value1", "value2", "value3", "value4"));
        when(setOperations.union(TEST_KEY, otherKeys)).thenReturn(values);

        // When
        Set<String> result = stringSetOperations.union(TEST_KEY, otherKeys);

        // Then
        assertEquals(4, result.size());
        assertTrue(result.contains("value1"));
        assertTrue(result.contains("value2"));
        assertTrue(result.contains("value3"));
        assertTrue(result.contains("value4"));
        verify(setOperations).union(TEST_KEY, otherKeys);
    }

    @Test
    void testDifference_VarArgs_Success() {
        // Given
        String[] otherKeys = { "key2", "key3" };
        Set<Object> values = new HashSet<>(Arrays.asList("value1", "value2"));
        when(setOperations.difference(eq(TEST_KEY), any(Collection.class))).thenReturn(values);

        // When
        Set<String> result = stringSetOperations.difference(TEST_KEY, otherKeys);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains("value1"));
        assertTrue(result.contains("value2"));
        verify(setOperations).difference(eq(TEST_KEY), any(Collection.class));
    }

    @Test
    void testDifference_Collection_Success() {
        // Given
        Collection<String> otherKeys = Arrays.asList("key2", "key3");
        Set<Object> values = new HashSet<>(Arrays.asList("value1", "value2"));
        when(setOperations.difference(TEST_KEY, otherKeys)).thenReturn(values);

        // When
        Set<String> result = stringSetOperations.difference(TEST_KEY, otherKeys);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains("value1"));
        assertTrue(result.contains("value2"));
        verify(setOperations).difference(TEST_KEY, otherKeys);
    }

    @Test
    void testTypeConversion_IntegerSet() {
        // Given
        Set<Object> values = new HashSet<>(Arrays.asList(1, 2, 3));
        when(setOperations.members(TEST_KEY)).thenReturn(values);

        RedisSetOperationsImpl<Integer> intSetOperations = new RedisSetOperationsImpl<>(redisTemplate,
                strategySelector, Integer.class);

        // When
        Set<Integer> result = intSetOperations.members(TEST_KEY);

        // Then
        assertEquals(3, result.size());
        assertTrue(result.contains(1));
        assertTrue(result.contains(2));
        assertTrue(result.contains(3));
    }

    @Test
    void testTypeConversion_StringToNumber() {
        // Given
        when(setOperations.pop(TEST_KEY)).thenReturn("123");

        RedisSetOperationsImpl<Integer> intSetOperations = new RedisSetOperationsImpl<>(redisTemplate,
                strategySelector, Integer.class);

        // When
        Integer result = intSetOperations.pop(TEST_KEY);

        // Then
        assertEquals(Integer.valueOf(123), result);
    }

    @Test
    void testBatchOperations() {
        // Given
        String[] addValues = { "value1", "value2", "value3" };
        Set<Object> membersValues = new HashSet<>(Arrays.asList("value1", "value2", "value3"));

        when(setOperations.add(eq(TEST_KEY), any(Object[].class))).thenReturn(3L);
        when(setOperations.members(TEST_KEY)).thenReturn(membersValues);
        when(setOperations.size(TEST_KEY)).thenReturn(3L);

        // When - simulate batch operations
        Long addResult = stringSetOperations.add(TEST_KEY, addValues);
        Set<String> membersResult = stringSetOperations.members(TEST_KEY);
        Long sizeResult = stringSetOperations.size(TEST_KEY);

        // Then
        assertEquals(3L, addResult);
        assertEquals(3, membersResult.size());
        assertTrue(membersResult.contains("value1"));
        assertTrue(membersResult.contains("value2"));
        assertTrue(membersResult.contains("value3"));
        assertEquals(3L, sizeResult);

        verify(setOperations).add(eq(TEST_KEY), any(Object[].class));
        verify(setOperations).members(TEST_KEY);
        verify(setOperations).size(TEST_KEY);
    }
}