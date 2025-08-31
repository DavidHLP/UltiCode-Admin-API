package com.david.commons.redis;

import com.david.commons.redis.cache.CacheEvictionPolicy;
import com.david.commons.redis.cache.CacheSyncPolicy;
import com.david.commons.redis.config.RedisCommonsProperties;
import com.david.commons.redis.exception.*;
import com.david.commons.redis.lock.DistributedLockManager;
import com.david.commons.redis.lock.LockType;
import com.david.commons.redis.operations.*;
import com.david.commons.redis.serialization.RedisSerializer;
import com.david.commons.redis.serialization.SerializationType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis Commons 结构测试
 *
 * @author David
 */
class RedisCommonsStructureTest {

    @Test
    void testSerializationTypeEnum() {
        // 测试序列化类型枚举
        assertEquals("json", SerializationType.JSON.getCode());
        assertEquals("kryo", SerializationType.KRYO.getCode());
        assertEquals("jdk", SerializationType.JDK.getCode());
        assertEquals("protobuf", SerializationType.PROTOBUF.getCode());

        // 测试根据代码获取类型
        assertEquals(SerializationType.JSON, SerializationType.fromCode("json"));
        assertEquals(SerializationType.KRYO, SerializationType.fromCode("kryo"));

        // 测试无效代码
        assertThrows(IllegalArgumentException.class, () -> SerializationType.fromCode("invalid"));
    }

    @Test
    void testLockTypeEnum() {
        // 测试锁类型枚举
        assertEquals("reentrant", LockType.REENTRANT.getCode());
        assertEquals("fair", LockType.FAIR.getCode());
        assertEquals("read", LockType.READ.getCode());
        assertEquals("write", LockType.WRITE.getCode());

        // 测试根据代码获取类型
        assertEquals(LockType.REENTRANT, LockType.fromCode("reentrant"));
        assertEquals(LockType.FAIR, LockType.fromCode("fair"));

        // 测试无效代码
        assertThrows(IllegalArgumentException.class, () -> LockType.fromCode("invalid"));
    }

    @Test
    void testCacheEvictionPolicyEnum() {
        // 测试缓存淘汰策略枚举
        assertEquals("allEntries", CacheEvictionPolicy.ALL_ENTRIES.getCode());
        assertEquals("singleKey", CacheEvictionPolicy.SINGLE_KEY.getCode());
        assertEquals("patternMatch", CacheEvictionPolicy.PATTERN_MATCH.getCode());
        assertEquals("conditional", CacheEvictionPolicy.CONDITIONAL.getCode());

        // 测试根据代码获取策略
        assertEquals(CacheEvictionPolicy.ALL_ENTRIES, CacheEvictionPolicy.fromCode("allEntries"));
        assertEquals(CacheEvictionPolicy.SINGLE_KEY, CacheEvictionPolicy.fromCode("singleKey"));

        // 测试无效代码
        assertThrows(IllegalArgumentException.class, () -> CacheEvictionPolicy.fromCode("invalid"));
    }

    @Test
    void testCacheSyncPolicyEnum() {
        // 测试缓存同步策略枚举
        assertEquals("sync", CacheSyncPolicy.SYNC.getCode());
        assertEquals("async", CacheSyncPolicy.ASYNC.getCode());
        assertEquals("bestEffort", CacheSyncPolicy.BEST_EFFORT.getCode());

        // 测试根据代码获取策略
        assertEquals(CacheSyncPolicy.SYNC, CacheSyncPolicy.fromCode("sync"));
        assertEquals(CacheSyncPolicy.ASYNC, CacheSyncPolicy.fromCode("async"));

        // 测试无效代码
        assertThrows(IllegalArgumentException.class, () -> CacheSyncPolicy.fromCode("invalid"));
    }

    @Test
    void testDataTypeEnum() {
        // 测试数据类型枚举
        assertEquals("string", RedisCommonOperations.DataType.STRING.getCode());
        assertEquals("hash", RedisCommonOperations.DataType.HASH.getCode());
        assertEquals("list", RedisCommonOperations.DataType.LIST.getCode());
        assertEquals("set", RedisCommonOperations.DataType.SET.getCode());
        assertEquals("zset", RedisCommonOperations.DataType.ZSET.getCode());

        // 测试根据代码获取类型
        assertEquals(RedisCommonOperations.DataType.STRING, RedisCommonOperations.DataType.fromCode("string"));
        assertEquals(RedisCommonOperations.DataType.HASH, RedisCommonOperations.DataType.fromCode("hash"));

        // 测试无效代码
        assertEquals(RedisCommonOperations.DataType.NONE, RedisCommonOperations.DataType.fromCode("invalid"));
    }

    @Test
    void testExceptionHierarchy() {
        // 测试异常层次结构
        RedisCommonsException baseException = new RedisCommonsException("Base exception");
        assertNotNull(baseException);
        assertEquals("Base exception", baseException.getMessage());

        RedisConnectionException connectionException = new RedisConnectionException("Connection failed");
        assertTrue(connectionException instanceof RedisCommonsException);

        RedisLockException lockException = new RedisLockException("Lock failed");
        assertTrue(lockException instanceof RedisCommonsException);

        RedisCacheException cacheException = new RedisCacheException("Cache failed");
        assertTrue(cacheException instanceof RedisCommonsException);
    }

    @Test
    void testErrorCodes() {
        // 测试错误码常量
        assertEquals("REDIS_CONNECTION_FAILED", RedisErrorCodes.CONNECTION_FAILED);
        assertEquals("REDIS_SERIALIZATION_FAILED", RedisErrorCodes.SERIALIZATION_FAILED);
        assertEquals("REDIS_LOCK_ACQUISITION_FAILED", RedisErrorCodes.LOCK_ACQUISITION_FAILED);
        assertEquals("REDIS_CACHE_OPERATION_FAILED", RedisErrorCodes.CACHE_OPERATION_FAILED);
    }

    @Test
    void testConfigurationProperties() {
        // 测试配置属性
        RedisCommonsProperties properties = new RedisCommonsProperties();

        // 测试默认值
        assertTrue(properties.isEnabled());
        assertEquals("springoj:", properties.getKeyPrefix());
        assertEquals(SerializationType.JSON, properties.getSerialization().getDefaultType());
        assertEquals(3600, properties.getCache().getDefaultTtl());
        assertEquals(10, properties.getLock().getDefaultWaitTime());
        assertTrue(properties.getProtection().isEnableBloomFilter());
        assertTrue(properties.getMonitoring().isEnabled());

        // 测试设置值
        properties.setEnabled(false);
        properties.setKeyPrefix("test:");
        assertFalse(properties.isEnabled());
        assertEquals("test:", properties.getKeyPrefix());
    }

    @Test
    void testInterfaceStructure() {
        // 测试接口结构完整性
        assertNotNull(RedisUtils.class);
        assertNotNull(RedisStringOperations.class);
        assertNotNull(RedisHashOperations.class);
        assertNotNull(RedisListOperations.class);
        assertNotNull(RedisSetOperations.class);
        assertNotNull(RedisZSetOperations.class);
        assertNotNull(RedisCommonOperations.class);
        assertNotNull(DistributedLockManager.class);
        assertNotNull(RedisSerializer.class);
    }
}