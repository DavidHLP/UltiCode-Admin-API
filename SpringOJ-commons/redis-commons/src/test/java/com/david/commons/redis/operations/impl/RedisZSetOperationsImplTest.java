package com.david.commons.redis.operations.impl;

import com.david.commons.redis.exception.RedisCommonsException;
import com.david.commons.redis.operations.RedisZSetOperations.ZSetTuple;
import com.david.commons.redis.serialization.SerializationStrategySelector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RedisZSetOperationsImpl 单元测试
 *
 * @author David
 */
@ExtendWith(MockitoExtension.class)
class RedisZSetOperationsImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ZSetOperations<String, Object> zSetOperations;

    @Mock
    private SerializationStrategySelector strategySelector;

    private RedisZSetOperationsImpl<String> stringZSetOperations;

    private static final String TEST_KEY = "test:zset";
    private static final String TEST_DEST_KEY = "test:dest:zset";
    private static final String TEST_VALUE = "test_value";
    private static final double TEST_SCORE = 1.0;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        stringZSetOperations = new RedisZSetOperationsImpl<>(redisTemplate, strategySelector, String.class);
    }

    @Test
    void testAdd_Success() {
        // Given
        when(zSetOperations.add(TEST_KEY, TEST_VALUE, TEST_SCORE)).thenReturn(true);

        // When
        Boolean result = stringZSetOperations.add(TEST_KEY, TEST_VALUE, TEST_SCORE);

        // Then
        assertTrue(result);
        verify(zSetOperations).add(TEST_KEY, TEST_VALUE, TEST_SCORE);
    }

    @Test
    void testAdd_AlreadyExists() {
        // Given
        when(zSetOperations.add(TEST_KEY, TEST_VALUE, TEST_SCORE)).thenReturn(false);

        // When
        Boolean result = stringZSetOperations.add(TEST_KEY, TEST_VALUE, TEST_SCORE);

        // Then
        assertFalse(result);
        verify(zSetOperations).add(TEST_KEY, TEST_VALUE, TEST_SCORE);
    }

    @Test
    void testAdd_Exception() {
        // Given
        when(zSetOperations.add(TEST_KEY, TEST_VALUE, TEST_SCORE)).thenThrow(new RuntimeException("Redis error"));

        // When & Then
        RedisCommonsException exception = assertThrows(RedisCommonsException.class,
                () -> stringZSetOperations.add(TEST_KEY, TEST_VALUE, TEST_SCORE));

        assertEquals("REDIS_ZSET_ADD_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to add value to zset for key: " + TEST_KEY));
    }

    @Test
    void testAdd_Tuples_Success() {
        // Given
        Set<ZSetTuple<String>> tuples = new HashSet<>();
        tuples.add(new ZSetTupleImpl<>("value1", 1.0));
        tuples.add(new ZSetTupleImpl<>("value2", 2.0));

        when(zSetOperations.add(eq(TEST_KEY), any(Set.class))).thenReturn(2L);

        // When
        Long result = stringZSetOperations.add(TEST_KEY, tuples);

        // Then
        assertEquals(2L, result);
        verify(zSetOperations).add(eq(TEST_KEY), any(Set.class));
    }

    @Test
    void testRemove_Success() {
        // Given
        String[] values = { "value1", "value2" };
        when(zSetOperations.remove(eq(TEST_KEY), any(Object[].class))).thenReturn(2L);

        // When
        Long result = stringZSetOperations.remove(TEST_KEY, values);

        // Then
        assertEquals(2L, result);
        verify(zSetOperations).remove(eq(TEST_KEY), any(Object[].class));
    }

    @Test
    void testRemove_Exception() {
        // Given
        String[] values = { "value1" };
        when(zSetOperations.remove(eq(TEST_KEY), any(Object[].class))).thenThrow(new RuntimeException("Redis error"));

        // When & Then
        RedisCommonsException exception = assertThrows(RedisCommonsException.class,
                () -> stringZSetOperations.remove(TEST_KEY, values));

        assertEquals("REDIS_ZSET_REMOVE_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to remove values from zset for key: " + TEST_KEY));
    }

    @Test
    void testRemoveRangeByScore_Success() {
        // Given
        when(zSetOperations.removeRangeByScore(TEST_KEY, 1.0, 5.0)).thenReturn(3L);

        // When
        Long result = stringZSetOperations.removeRangeByScore(TEST_KEY, 1.0, 5.0);

        // Then
        assertEquals(3L, result);
        verify(zSetOperations).removeRangeByScore(TEST_KEY, 1.0, 5.0);
    }

    @Test
    void testRemoveRange_Success() {
        // Given
        when(zSetOperations.removeRange(TEST_KEY, 0, 2)).thenReturn(3L);

        // When
        Long result = stringZSetOperations.removeRange(TEST_KEY, 0, 2);

        // Then
        assertEquals(3L, result);
        verify(zSetOperations).removeRange(TEST_KEY, 0, 2);
    }

    @Test
    void testIncrementScore_Success() {
        // Given
        when(zSetOperations.incrementScore(TEST_KEY, TEST_VALUE, 2.5)).thenReturn(3.5);

        // When
        Double result = stringZSetOperations.incrementScore(TEST_KEY, TEST_VALUE, 2.5);

        // Then
        assertEquals(3.5, result);
        verify(zSetOperations).incrementScore(TEST_KEY, TEST_VALUE, 2.5);
    }

    @Test
    void testScore_Success() {
        // Given
        when(zSetOperations.score(TEST_KEY, TEST_VALUE)).thenReturn(TEST_SCORE);

        // When
        Double result = stringZSetOperations.score(TEST_KEY, TEST_VALUE);

        // Then
        assertEquals(TEST_SCORE, result);
        verify(zSetOperations).score(TEST_KEY, TEST_VALUE);
    }

    @Test
    void testScore_NotFound() {
        // Given
        when(zSetOperations.score(TEST_KEY, TEST_VALUE)).thenReturn(null);

        // When
        Double result = stringZSetOperations.score(TEST_KEY, TEST_VALUE);

        // Then
        assertNull(result);
        verify(zSetOperations).score(TEST_KEY, TEST_VALUE);
    }

    @Test
    void testRank_Success() {
        // Given
        when(zSetOperations.rank(TEST_KEY, TEST_VALUE)).thenReturn(2L);

        // When
        Long result = stringZSetOperations.rank(TEST_KEY, TEST_VALUE);

        // Then
        assertEquals(2L, result);
        verify(zSetOperations).rank(TEST_KEY, TEST_VALUE);
    }

    @Test
    void testRank_NotFound() {
        // Given
        when(zSetOperations.rank(TEST_KEY, TEST_VALUE)).thenReturn(null);

        // When
        Long result = stringZSetOperations.rank(TEST_KEY, TEST_VALUE);

        // Then
        assertNull(result);
        verify(zSetOperations).rank(TEST_KEY, TEST_VALUE);
    }

    @Test
    void testReverseRank_Success() {
        // Given
        when(zSetOperations.reverseRank(TEST_KEY, TEST_VALUE)).thenReturn(1L);

        // When
        Long result = stringZSetOperations.reverseRank(TEST_KEY, TEST_VALUE);

        // Then
        assertEquals(1L, result);
        verify(zSetOperations).reverseRank(TEST_KEY, TEST_VALUE);
    }

    @Test
    void testRange_Success() {
        // Given
        Set<Object> values = new LinkedHashSet<>(Arrays.asList("value1", "value2", "value3"));
        when(zSetOperations.range(TEST_KEY, 0, 2)).thenReturn(values);

        // When
        Set<String> result = stringZSetOperations.range(TEST_KEY, 0, 2);

        // Then
        assertEquals(3, result.size());
        assertTrue(result.contains("value1"));
        assertTrue(result.contains("value2"));
        assertTrue(result.contains("value3"));
        verify(zSetOperations).range(TEST_KEY, 0, 2);
    }

    @Test
    void testRange_Null() {
        // Given
        when(zSetOperations.range(TEST_KEY, 0, 2)).thenReturn(null);

        // When
        Set<String> result = stringZSetOperations.range(TEST_KEY, 0, 2);

        // Then
        assertNull(result);
        verify(zSetOperations).range(TEST_KEY, 0, 2);
    }

    @Test
    void testRangeWithScores_Success() {
        // Given
        Set<ZSetOperations.TypedTuple<Object>> tuples = new LinkedHashSet<>();
        tuples.add(ZSetOperations.TypedTuple.of("value1", 1.0));
        tuples.add(ZSetOperations.TypedTuple.of("value2", 2.0));
        when(zSetOperations.rangeWithScores(TEST_KEY, 0, 1)).thenReturn(tuples);

        // When
        Set<ZSetTuple<String>> result = stringZSetOperations.rangeWithScores(TEST_KEY, 0, 1);

        // Then
        assertEquals(2, result.size());
        // 验证结果包含正确的值和分数
        boolean foundValue1 = false, foundValue2 = false;
        for (ZSetTuple<String> tuple : result) {
            if ("value1".equals(tuple.getValue()) && tuple.getScore().equals(1.0)) {
                foundValue1 = true;
            } else if ("value2".equals(tuple.getValue()) && tuple.getScore().equals(2.0)) {
                foundValue2 = true;
            }
        }
        assertTrue(foundValue1);
        assertTrue(foundValue2);
        verify(zSetOperations).rangeWithScores(TEST_KEY, 0, 1);
    }

    @Test
    void testReverseRange_Success() {
        // Given
        Set<Object> values = new LinkedHashSet<>(Arrays.asList("value3", "value2", "value1"));
        when(zSetOperations.reverseRange(TEST_KEY, 0, 2)).thenReturn(values);

        // When
        Set<String> result = stringZSetOperations.reverseRange(TEST_KEY, 0, 2);

        // Then
        assertEquals(3, result.size());
        assertTrue(result.contains("value1"));
        assertTrue(result.contains("value2"));
        assertTrue(result.contains("value3"));
        verify(zSetOperations).reverseRange(TEST_KEY, 0, 2);
    }

    @Test
    void testReverseRangeWithScores_Success() {
        // Given
        Set<ZSetOperations.TypedTuple<Object>> tuples = new LinkedHashSet<>();
        tuples.add(ZSetOperations.TypedTuple.of("value2", 2.0));
        tuples.add(ZSetOperations.TypedTuple.of("value1", 1.0));
        when(zSetOperations.reverseRangeWithScores(TEST_KEY, 0, 1)).thenReturn(tuples);

        // When
        Set<ZSetTuple<String>> result = stringZSetOperations.reverseRangeWithScores(TEST_KEY, 0, 1);

        // Then
        assertEquals(2, result.size());
        // 验证结果包含正确的值和分数
        boolean foundValue1 = false, foundValue2 = false;
        for (ZSetTuple<String> tuple : result) {
            if ("value1".equals(tuple.getValue()) && tuple.getScore().equals(1.0)) {
                foundValue1 = true;
            } else if ("value2".equals(tuple.getValue()) && tuple.getScore().equals(2.0)) {
                foundValue2 = true;
            }
        }
        assertTrue(foundValue1);
        assertTrue(foundValue2);
        verify(zSetOperations).reverseRangeWithScores(TEST_KEY, 0, 1);
    }

    @Test
    void testRangeByScore_Success() {
        // Given
        Set<Object> values = new LinkedHashSet<>(Arrays.asList("value1", "value2"));
        when(zSetOperations.rangeByScore(TEST_KEY, 1.0, 3.0)).thenReturn(values);

        // When
        Set<String> result = stringZSetOperations.rangeByScore(TEST_KEY, 1.0, 3.0);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains("value1"));
        assertTrue(result.contains("value2"));
        verify(zSetOperations).rangeByScore(TEST_KEY, 1.0, 3.0);
    }

    @Test
    void testRangeByScoreWithScores_Success() {
        // Given
        Set<ZSetOperations.TypedTuple<Object>> tuples = new LinkedHashSet<>();
        tuples.add(ZSetOperations.TypedTuple.of("value1", 1.5));
        tuples.add(ZSetOperations.TypedTuple.of("value2", 2.5));
        when(zSetOperations.rangeByScoreWithScores(TEST_KEY, 1.0, 3.0)).thenReturn(tuples);

        // When
        Set<ZSetTuple<String>> result = stringZSetOperations.rangeByScoreWithScores(TEST_KEY, 1.0, 3.0);

        // Then
        assertEquals(2, result.size());
        // 验证结果包含正确的值和分数
        boolean foundValue1 = false, foundValue2 = false;
        for (ZSetTuple<String> tuple : result) {
            if ("value1".equals(tuple.getValue()) && tuple.getScore().equals(1.5)) {
                foundValue1 = true;
            } else if ("value2".equals(tuple.getValue()) && tuple.getScore().equals(2.5)) {
                foundValue2 = true;
            }
        }
        assertTrue(foundValue1);
        assertTrue(foundValue2);
        verify(zSetOperations).rangeByScoreWithScores(TEST_KEY, 1.0, 3.0);
    }

    @Test
    void testRangeByScore_WithLimit_Success() {
        // Given
        Set<Object> values = new LinkedHashSet<>(Arrays.asList("value1", "value2"));
        when(zSetOperations.rangeByScore(TEST_KEY, 1.0, 5.0, 0, 2)).thenReturn(values);

        // When
        Set<String> result = stringZSetOperations.rangeByScore(TEST_KEY, 1.0, 5.0, 0, 2);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains("value1"));
        assertTrue(result.contains("value2"));
        verify(zSetOperations).rangeByScore(TEST_KEY, 1.0, 5.0, 0, 2);
    }

    @Test
    void testRangeByScoreWithScores_WithLimit_Success() {
        // Given
        Set<ZSetOperations.TypedTuple<Object>> tuples = new LinkedHashSet<>();
        tuples.add(ZSetOperations.TypedTuple.of("value1", 1.5));
        tuples.add(ZSetOperations.TypedTuple.of("value2", 2.5));
        when(zSetOperations.rangeByScoreWithScores(TEST_KEY, 1.0, 5.0, 0, 2)).thenReturn(tuples);

        // When
        Set<ZSetTuple<String>> result = stringZSetOperations.rangeByScoreWithScores(TEST_KEY, 1.0, 5.0, 0, 2);

        // Then
        assertEquals(2, result.size());
        // 验证结果包含正确的值和分数
        boolean foundValue1 = false, foundValue2 = false;
        for (ZSetTuple<String> tuple : result) {
            if ("value1".equals(tuple.getValue()) && tuple.getScore().equals(1.5)) {
                foundValue1 = true;
            } else if ("value2".equals(tuple.getValue()) && tuple.getScore().equals(2.5)) {
                foundValue2 = true;
            }
        }
        assertTrue(foundValue1);
        assertTrue(foundValue2);
        verify(zSetOperations).rangeByScoreWithScores(TEST_KEY, 1.0, 5.0, 0, 2);
    }

    @Test
    void testCount_Success() {
        // Given
        when(zSetOperations.count(TEST_KEY, 1.0, 5.0)).thenReturn(3L);

        // When
        Long result = stringZSetOperations.count(TEST_KEY, 1.0, 5.0);

        // Then
        assertEquals(3L, result);
        verify(zSetOperations).count(TEST_KEY, 1.0, 5.0);
    }

    @Test
    void testSize_Success() {
        // Given
        when(zSetOperations.size(TEST_KEY)).thenReturn(10L);

        // When
        Long result = stringZSetOperations.size(TEST_KEY);

        // Then
        assertEquals(10L, result);
        verify(zSetOperations).size(TEST_KEY);
    }

    @Test
    void testSize_Exception() {
        // Given
        when(zSetOperations.size(TEST_KEY)).thenThrow(new RuntimeException("Redis error"));

        // When & Then
        RedisCommonsException exception = assertThrows(RedisCommonsException.class,
                () -> stringZSetOperations.size(TEST_KEY));

        assertEquals("REDIS_ZSET_SIZE_ERROR", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Failed to get zset size for key: " + TEST_KEY));
    }

    @Test
    void testIntersectAndStore_VarArgs_Success() {
        // Given
        String[] otherKeys = { "key2", "key3" };
        when(zSetOperations.intersectAndStore(eq(TEST_KEY), any(Collection.class), eq(TEST_DEST_KEY))).thenReturn(2L);

        // When
        Long result = stringZSetOperations.intersectAndStore(TEST_KEY, TEST_DEST_KEY, otherKeys);

        // Then
        assertEquals(2L, result);
        verify(zSetOperations).intersectAndStore(eq(TEST_KEY), any(Collection.class), eq(TEST_DEST_KEY));
    }

    @Test
    void testIntersectAndStore_Collection_Success() {
        // Given
        Collection<String> otherKeys = Arrays.asList("key2", "key3");
        when(zSetOperations.intersectAndStore(TEST_KEY, otherKeys, TEST_DEST_KEY)).thenReturn(2L);

        // When
        Long result = stringZSetOperations.intersectAndStore(TEST_KEY, TEST_DEST_KEY, otherKeys);

        // Then
        assertEquals(2L, result);
        verify(zSetOperations).intersectAndStore(TEST_KEY, otherKeys, TEST_DEST_KEY);
    }

    @Test
    void testUnionAndStore_VarArgs_Success() {
        // Given
        String[] otherKeys = { "key2", "key3" };
        when(zSetOperations.unionAndStore(eq(TEST_KEY), any(Collection.class), eq(TEST_DEST_KEY))).thenReturn(5L);

        // When
        Long result = stringZSetOperations.unionAndStore(TEST_KEY, TEST_DEST_KEY, otherKeys);

        // Then
        assertEquals(5L, result);
        verify(zSetOperations).unionAndStore(eq(TEST_KEY), any(Collection.class), eq(TEST_DEST_KEY));
    }

    @Test
    void testUnionAndStore_Collection_Success() {
        // Given
        Collection<String> otherKeys = Arrays.asList("key2", "key3");
        when(zSetOperations.unionAndStore(TEST_KEY, otherKeys, TEST_DEST_KEY)).thenReturn(5L);

        // When
        Long result = stringZSetOperations.unionAndStore(TEST_KEY, TEST_DEST_KEY, otherKeys);

        // Then
        assertEquals(5L, result);
        verify(zSetOperations).unionAndStore(TEST_KEY, otherKeys, TEST_DEST_KEY);
    }

    @Test
    void testTypeConversion_IntegerZSet() {
        // Given
        Set<Object> values = new LinkedHashSet<>(Arrays.asList(1, 2, 3));
        when(zSetOperations.range(TEST_KEY, 0, 2)).thenReturn(values);

        RedisZSetOperationsImpl<Integer> intZSetOperations = new RedisZSetOperationsImpl<>(redisTemplate,
                strategySelector, Integer.class);

        // When
        Set<Integer> result = intZSetOperations.range(TEST_KEY, 0, 2);

        // Then
        assertEquals(3, result.size());
        assertTrue(result.contains(1));
        assertTrue(result.contains(2));
        assertTrue(result.contains(3));
    }

    @Test
    void testTypeConversion_StringToNumber() {
        // Given
        when(zSetOperations.score(TEST_KEY, 123)).thenReturn(456.0);

        RedisZSetOperationsImpl<Integer> intZSetOperations = new RedisZSetOperationsImpl<>(redisTemplate,
                strategySelector, Integer.class);

        // When
        Double result = intZSetOperations.score(TEST_KEY, 123);

        // Then
        assertEquals(456.0, result);
    }

    @Test
    void testBatchOperations() {
        // Given
        Set<ZSetTuple<String>> tuples = new HashSet<>();
        tuples.add(new ZSetTupleImpl<>("value1", 1.0));
        tuples.add(new ZSetTupleImpl<>("value2", 2.0));

        Set<Object> rangeValues = new LinkedHashSet<>(Arrays.asList("value1", "value2"));

        when(zSetOperations.add(eq(TEST_KEY), any(Set.class))).thenReturn(2L);
        when(zSetOperations.range(TEST_KEY, 0, -1)).thenReturn(rangeValues);
        when(zSetOperations.size(TEST_KEY)).thenReturn(2L);

        // When - simulate batch operations
        Long addResult = stringZSetOperations.add(TEST_KEY, tuples);
        Set<String> rangeResult = stringZSetOperations.range(TEST_KEY, 0, -1);
        Long sizeResult = stringZSetOperations.size(TEST_KEY);

        // Then
        assertEquals(2L, addResult);
        assertEquals(2, rangeResult.size());
        assertTrue(rangeResult.contains("value1"));
        assertTrue(rangeResult.contains("value2"));
        assertEquals(2L, sizeResult);

        verify(zSetOperations).add(eq(TEST_KEY), any(Set.class));
        verify(zSetOperations).range(TEST_KEY, 0, -1);
        verify(zSetOperations).size(TEST_KEY);
    }
}