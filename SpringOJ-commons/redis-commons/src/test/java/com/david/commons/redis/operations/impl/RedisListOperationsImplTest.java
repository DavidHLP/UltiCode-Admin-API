package com.david.commons.redis.operations.impl;

import com.david.commons.redis.RealRedisTestBase;
// 移除不再使用的导入
import com.david.commons.redis.serialization.SerializationStrategySelector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RedisListOperationsImpl 真实连接测试
 *
 * @author David
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RealRedisTestBase.RealRedisTestConfiguration.class)
class RedisListOperationsImplTest_New extends RealRedisTestBase {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private RedisListOperationsImpl<String> listOperations;

    private static final String TEST_VALUE = "test_value";

    @BeforeEach
    void setUp() {
        // 创建空的模拟选择器 - 只为了测试基本功能
        SerializationStrategySelector strategySelector = null;
        
        listOperations = new RedisListOperationsImpl<>(redisTemplate, strategySelector, String.class);
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
    void testLeftPush() {
        // Given
        String testKey = randomTestKey();

        // When
        Long result = listOperations.leftPush(testKey, TEST_VALUE);

        // Then
        assertEquals(1L, result);
        assertEquals(TEST_VALUE, redisTemplate.opsForList().index(testKey, 0));
    }

    @Test
    void testRightPush() {
        // Given
        String testKey = randomTestKey();

        // When
        Long result = listOperations.rightPush(testKey, TEST_VALUE);

        // Then
        assertEquals(1L, result);
        assertEquals(TEST_VALUE, redisTemplate.opsForList().index(testKey, 0));
    }

    @Test
    void testLeftPop() {
        // Given
        String testKey = randomTestKey();
        redisTemplate.opsForList().leftPush(testKey, TEST_VALUE);

        // When
        String result = listOperations.leftPop(testKey);

        // Then
        assertEquals(TEST_VALUE, result);
        assertEquals(0L, redisTemplate.opsForList().size(testKey));
    }

    @Test
    void testRightPop() {
        // Given
        String testKey = randomTestKey();
        redisTemplate.opsForList().rightPush(testKey, TEST_VALUE);

        // When
        String result = listOperations.rightPop(testKey);

        // Then
        assertEquals(TEST_VALUE, result);
        assertEquals(0L, redisTemplate.opsForList().size(testKey));
    }

    @Test
    void testSize() {
        // Given
        String testKey = randomTestKey();
        redisTemplate.opsForList().rightPush(testKey, "value1");
        redisTemplate.opsForList().rightPush(testKey, "value2");
        redisTemplate.opsForList().rightPush(testKey, "value3");

        // When
        Long result = listOperations.size(testKey);

        // Then
        assertEquals(3L, result);
    }

    @Test
    void testIndex() {
        // Given
        String testKey = randomTestKey();
        redisTemplate.opsForList().rightPush(testKey, "value1");
        redisTemplate.opsForList().rightPush(testKey, "value2");
        redisTemplate.opsForList().rightPush(testKey, "value3");

        // When
        String result = listOperations.index(testKey, 1);

        // Then
        assertEquals("value2", result);
    }

    @Test
    void testSet() {
        // Given
        String testKey = randomTestKey();
        String newValue = "new_value";
        redisTemplate.opsForList().rightPush(testKey, "value1");
        redisTemplate.opsForList().rightPush(testKey, "value2");

        // When
        listOperations.set(testKey, 1, newValue);

        // Then
        assertEquals(newValue, redisTemplate.opsForList().index(testKey, 1));
    }

    @Test
    void testRange() {
        // Given
        String testKey = randomTestKey();
        redisTemplate.opsForList().rightPush(testKey, "value1");
        redisTemplate.opsForList().rightPush(testKey, "value2");
        redisTemplate.opsForList().rightPush(testKey, "value3");
        redisTemplate.opsForList().rightPush(testKey, "value4");

        // When
        List<String> result = listOperations.range(testKey, 1, 2);

        // Then
        assertEquals(2, result.size());
        assertEquals("value2", result.get(0));
        assertEquals("value3", result.get(1));
    }

    @Test
    void testTrim() {
        // Given
        String testKey = randomTestKey();
        redisTemplate.opsForList().rightPush(testKey, "value1");
        redisTemplate.opsForList().rightPush(testKey, "value2");
        redisTemplate.opsForList().rightPush(testKey, "value3");
        redisTemplate.opsForList().rightPush(testKey, "value4");

        // When
        listOperations.trim(testKey, 1, 2);

        // Then
        assertEquals(2L, redisTemplate.opsForList().size(testKey));
        assertEquals("value2", redisTemplate.opsForList().index(testKey, 0));
        assertEquals("value3", redisTemplate.opsForList().index(testKey, 1));
    }

    @Test
    void testRemove() {
        // Given
        String testKey = randomTestKey();
        redisTemplate.opsForList().rightPush(testKey, "value1");
        redisTemplate.opsForList().rightPush(testKey, TEST_VALUE);
        redisTemplate.opsForList().rightPush(testKey, "value3");
        redisTemplate.opsForList().rightPush(testKey, TEST_VALUE);

        // When
        Long result = listOperations.remove(testKey, 0, TEST_VALUE);

        // Then
        assertEquals(2L, result);
        assertEquals(2L, redisTemplate.opsForList().size(testKey));
        assertFalse(redisTemplate.opsForList().range(testKey, 0, -1).contains(TEST_VALUE));
    }

    @Test
    void testLeftPushAll() {
        // Given
        String testKey = randomTestKey();
        List<String> values = Arrays.asList("value1", "value2", "value3");

        // When
        Long result = listOperations.leftPushAll(testKey, values);

        // Then
        assertEquals(3L, result);
        List<Object> allValues = redisTemplate.opsForList().range(testKey, 0, -1);
        assertEquals(3, allValues.size());
        // 注意：leftPushAll会反转顺序
        assertEquals("value3", allValues.get(0));
        assertEquals("value2", allValues.get(1));
        assertEquals("value1", allValues.get(2));
    }

    @Test
    void testRightPushAll() {
        // Given
        String testKey = randomTestKey();
        List<String> values = Arrays.asList("value1", "value2", "value3");

        // When
        Long result = listOperations.rightPushAll(testKey, values);

        // Then
        assertEquals(3L, result);
        List<Object> allValues = redisTemplate.opsForList().range(testKey, 0, -1);
        assertEquals(3, allValues.size());
        assertEquals("value1", allValues.get(0));
        assertEquals("value2", allValues.get(1));
        assertEquals("value3", allValues.get(2));
    }

    @Test
    void testLeftPushIfPresent() {
        // Given
        String testKey = randomTestKey();
        redisTemplate.opsForList().rightPush(testKey, "existing_value");

        // When
        Long result = listOperations.leftPushIfPresent(testKey, TEST_VALUE);

        // Then
        assertEquals(2L, result);
        assertEquals(TEST_VALUE, redisTemplate.opsForList().index(testKey, 0));
    }

    @Test
    void testRightPushIfPresent() {
        // Given
        String testKey = randomTestKey();
        redisTemplate.opsForList().leftPush(testKey, "existing_value");

        // When
        Long result = listOperations.rightPushIfPresent(testKey, TEST_VALUE);

        // Then
        assertEquals(2L, result);
        assertEquals(TEST_VALUE, redisTemplate.opsForList().index(testKey, 1));
    }
}
