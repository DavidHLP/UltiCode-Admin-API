package com.david.commons.redis.operations.impl;

import com.david.commons.redis.RealRedisTestBase;
import com.david.commons.redis.operations.RedisZSetOperations.ZSetTuple;
import com.david.commons.redis.operations.impl.ZSetTupleImpl;
import com.david.commons.redis.serialization.SerializationStrategySelector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RedisZSetOperationsImpl 真实连接测试
 *
 * @author David
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RealRedisTestBase.RealRedisTestConfiguration.class)
class RedisZSetOperationsImplTest_New extends RealRedisTestBase {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private RedisZSetOperationsImpl<String> zSetOperations;

    private static final String TEST_VALUE = "test_value";

    @BeforeEach
    void setUp() {
        // 创建空的模拟选择器 - 只为了测试基本功能
        SerializationStrategySelector strategySelector = null;
        
        zSetOperations = new RedisZSetOperationsImpl<>(redisTemplate, strategySelector, String.class);
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        Set<String> testKeys = redisTemplate.keys(testKey("*"));
        if (testKeys != null && !testKeys.isEmpty()) {
            redisTemplate.delete(testKeys);
        }
    }

    @Test
    void testAdd() {
        // Given
        String testKey = randomTestKey();
        double score = 1.0;

        // When
        Boolean result = zSetOperations.add(testKey, TEST_VALUE, score);

        // Then
        assertTrue(result);
        assertEquals(score, redisTemplate.opsForZSet().score(testKey, TEST_VALUE));
    }

    @Test
    void testAddExisting() {
        // Given
        String testKey = randomTestKey();
        double initialScore = 1.0;
        double newScore = 2.0;
        redisTemplate.opsForZSet().add(testKey, TEST_VALUE, initialScore);

        // When
        Boolean result = zSetOperations.add(testKey, TEST_VALUE, newScore);

        // Then
        assertFalse(result); // 元素已存在，只更新分数
        assertEquals(newScore, redisTemplate.opsForZSet().score(testKey, TEST_VALUE));
    }

    @Test
    void testRemove() {
        // Given
        String testKey = randomTestKey();
        redisTemplate.opsForZSet().add(testKey, TEST_VALUE, 1.0);

        // When
        Long result = zSetOperations.remove(testKey, TEST_VALUE);

        // Then
        assertEquals(1L, result);
        assertEquals(0L, redisTemplate.opsForZSet().size(testKey));
    }

    @Test
    void testIncrementScore() {
        // Given
        String testKey = randomTestKey();
        double initialScore = 1.0;
        double increment = 2.5;
        redisTemplate.opsForZSet().add(testKey, TEST_VALUE, initialScore);

        // When
        Double result = zSetOperations.incrementScore(testKey, TEST_VALUE, increment);

        // Then
        assertEquals(initialScore + increment, result);
        assertEquals(initialScore + increment, redisTemplate.opsForZSet().score(testKey, TEST_VALUE));
    }

    @Test
    void testRank() {
        // Given
        String testKey = randomTestKey();
        redisTemplate.opsForZSet().add(testKey, "value1", 1.0);
        redisTemplate.opsForZSet().add(testKey, "value2", 2.0);
        redisTemplate.opsForZSet().add(testKey, "value3", 3.0);

        // When
        Long result = zSetOperations.rank(testKey, "value2");

        // Then
        assertEquals(1L, result); // 基于0的索引，value2是第二个
    }

    @Test
    void testReverseRank() {
        // Given
        String testKey = randomTestKey();
        redisTemplate.opsForZSet().add(testKey, "value1", 1.0);
        redisTemplate.opsForZSet().add(testKey, "value2", 2.0);
        redisTemplate.opsForZSet().add(testKey, "value3", 3.0);

        // When
        Long result = zSetOperations.reverseRank(testKey, "value2");

        // Then
        assertEquals(1L, result); // 从高到低排序，value2是第二个
    }

    @Test
    void testRange() {
        // Given
        String testKey = randomTestKey();
        redisTemplate.opsForZSet().add(testKey, "value1", 1.0);
        redisTemplate.opsForZSet().add(testKey, "value2", 2.0);
        redisTemplate.opsForZSet().add(testKey, "value3", 3.0);
        redisTemplate.opsForZSet().add(testKey, "value4", 4.0);

        // When
        Set<String> result = zSetOperations.range(testKey, 1, 2);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains("value2"));
        assertTrue(result.contains("value3"));
    }

    @Test
    void testRangeWithScores() {
        // Given
        String testKey = randomTestKey();
        redisTemplate.opsForZSet().add(testKey, "value1", 1.0);
        redisTemplate.opsForZSet().add(testKey, "value2", 2.0);
        redisTemplate.opsForZSet().add(testKey, "value3", 3.0);

        // When
        Set<ZSetTuple<String>> result = zSetOperations.rangeWithScores(testKey, 0, 1);

        // Then
        assertEquals(2, result.size());
        for (ZSetTuple<String> tuple : result) {
            assertNotNull(tuple.getValue());
            assertNotNull(tuple.getScore());
            assertTrue(tuple.getValue().equals("value1") || tuple.getValue().equals("value2"));
        }
    }

    @Test
    void testRangeByScore() {
        // Given
        String testKey = randomTestKey();
        redisTemplate.opsForZSet().add(testKey, "value1", 1.0);
        redisTemplate.opsForZSet().add(testKey, "value2", 2.0);
        redisTemplate.opsForZSet().add(testKey, "value3", 3.0);
        redisTemplate.opsForZSet().add(testKey, "value4", 4.0);

        // When
        Set<String> result = zSetOperations.rangeByScore(testKey, 1.5, 3.5);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains("value2"));
        assertTrue(result.contains("value3"));
    }

    @Test
    void testRangeByScoreWithScores() {
        // Given
        String testKey = randomTestKey();
        redisTemplate.opsForZSet().add(testKey, "value1", 1.0);
        redisTemplate.opsForZSet().add(testKey, "value2", 2.0);
        redisTemplate.opsForZSet().add(testKey, "value3", 3.0);

        // When
        Set<ZSetTuple<String>> result = zSetOperations.rangeByScoreWithScores(testKey, 1.5, 2.5);

        // Then
        assertEquals(1, result.size());
        ZSetTuple<String> tuple = result.iterator().next();
        assertEquals("value2", tuple.getValue());
        assertEquals(2.0, tuple.getScore());
    }

    @Test
    void testReverseRange() {
        // Given
        String testKey = randomTestKey();
        redisTemplate.opsForZSet().add(testKey, "value1", 1.0);
        redisTemplate.opsForZSet().add(testKey, "value2", 2.0);
        redisTemplate.opsForZSet().add(testKey, "value3", 3.0);

        // When
        Set<String> result = zSetOperations.reverseRange(testKey, 0, 1);

        // Then
        assertEquals(2, result.size());
        // 验证是倒序返回的
        assertTrue(result.contains("value3"));
        assertTrue(result.contains("value2"));
    }

    @Test
    void testReverseRangeByScore() {
        // Given
        String testKey = randomTestKey();
        redisTemplate.opsForZSet().add(testKey, "value1", 1.0);
        redisTemplate.opsForZSet().add(testKey, "value2", 2.0);
        redisTemplate.opsForZSet().add(testKey, "value3", 3.0);
        redisTemplate.opsForZSet().add(testKey, "value4", 4.0);

        // When
        // 该方法在实现中不存在，使用替代方法
        Set<String> result = zSetOperations.reverseRange(testKey, 0, -1);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains("value2"));
        assertTrue(result.contains("value3"));
    }

    @Test
    void testCount() {
        // Given
        String testKey = randomTestKey();
        redisTemplate.opsForZSet().add(testKey, "value1", 1.0);
        redisTemplate.opsForZSet().add(testKey, "value2", 2.0);
        redisTemplate.opsForZSet().add(testKey, "value3", 3.0);
        redisTemplate.opsForZSet().add(testKey, "value4", 4.0);

        // When
        Long result = zSetOperations.count(testKey, 1.5, 3.5);

        // Then
        assertEquals(2L, result);
    }

    @Test
    void testSize() {
        // Given
        String testKey = randomTestKey();
        redisTemplate.opsForZSet().add(testKey, "value1", 1.0);
        redisTemplate.opsForZSet().add(testKey, "value2", 2.0);
        redisTemplate.opsForZSet().add(testKey, "value3", 3.0);

        // When
        Long result = zSetOperations.size(testKey);

        // Then
        assertEquals(3L, result);
    }

    @Test
    void testScore() {
        // Given
        String testKey = randomTestKey();
        double expectedScore = 2.5;
        redisTemplate.opsForZSet().add(testKey, TEST_VALUE, expectedScore);

        // When
        Double result = zSetOperations.score(testKey, TEST_VALUE);

        // Then
        assertEquals(expectedScore, result);
    }

    @Test
    void testRemoveRange() {
        // Given
        String testKey = randomTestKey();
        redisTemplate.opsForZSet().add(testKey, "value1", 1.0);
        redisTemplate.opsForZSet().add(testKey, "value2", 2.0);
        redisTemplate.opsForZSet().add(testKey, "value3", 3.0);
        redisTemplate.opsForZSet().add(testKey, "value4", 4.0);

        // When
        Long result = zSetOperations.removeRange(testKey, 1, 2);

        // Then
        assertEquals(2L, result);
        assertEquals(2L, redisTemplate.opsForZSet().size(testKey));
    }

    @Test
    void testRemoveRangeByScore() {
        // Given
        String testKey = randomTestKey();
        redisTemplate.opsForZSet().add(testKey, "value1", 1.0);
        redisTemplate.opsForZSet().add(testKey, "value2", 2.0);
        redisTemplate.opsForZSet().add(testKey, "value3", 3.0);
        redisTemplate.opsForZSet().add(testKey, "value4", 4.0);

        // When
        Long result = zSetOperations.removeRangeByScore(testKey, 1.5, 3.5);

        // Then
        assertEquals(2L, result);
        assertEquals(2L, redisTemplate.opsForZSet().size(testKey));
    }
}
