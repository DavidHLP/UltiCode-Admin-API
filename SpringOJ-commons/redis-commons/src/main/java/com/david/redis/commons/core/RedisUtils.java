package com.david.redis.commons.core;

import com.david.redis.commons.core.lock.RedisCallback;
import com.david.redis.commons.core.operations.*;
import com.david.redis.commons.core.transaction.RedisTransactionManager;
import com.david.redis.commons.core.transaction.TransactionContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Redis核心工具类 - 门面模式
 *
 * <p>提供统一的Redis操作接口，支持String、Hash、List、Set、ZSet等数据类型操作， 以及事务支持。
 * 所有操作都委托给对应的专门操作类，保持向后兼容性。
 *
 * @author David
 */
@Slf4j
@Component
public class RedisUtils {

    @Getter 
    private final RedisTemplate<String, Object> redisTemplate;

    // 各个操作模块
    private final RedisStringOperations stringOperations;
    private final RedisHashOperations hashOperations;
    private final RedisListOperations listOperations;
    private final RedisSetOperations setOperations;
    private final RedisZSetOperations zSetOperations;
    private final RedisTransactionOperations transactionOperations;

    private RedisTransactionManager transactionManager;

    /**
     * 构造函数
     */
    public RedisUtils(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        
        // 初始化各个操作模块
        this.stringOperations = new RedisStringOperationsImpl(redisTemplate);
        this.hashOperations = new RedisHashOperationsImpl(redisTemplate);
        this.listOperations = new RedisListOperationsImpl(redisTemplate);
        this.setOperations = new RedisSetOperationsImpl(redisTemplate);
        this.zSetOperations = new RedisZSetOperationsImpl(redisTemplate);
        this.transactionOperations = new RedisTransactionOperationsImpl(redisTemplate, null);
    }

    /**
     * 设置事务管理器
     */
    public void setTransactionManager(RedisTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    // ========== String Operations ==========
    
    public void set(String key, Object value) {
        stringOperations.set(key, value);
    }

    public void set(String key, Object value, Duration timeout) {
        stringOperations.set(key, value, timeout);
    }

    public <T> T get(String key, Class<T> clazz) {
        return stringOperations.get(key, clazz);
    }

    public String getString(String key) {
        return stringOperations.getString(key);
    }

    public Boolean delete(String key) {
        return stringOperations.delete(key);
    }

    public Long delete(String... keys) {
        return stringOperations.delete(keys);
    }

    public Set<String> keys(String pattern) {
        return stringOperations.keys(pattern);
    }

    public Set<String> scanKeys(String pattern) {
        return stringOperations.scanKeys(pattern);
    }

    // ========== Hash Operations ==========
    
    public void hSet(String key, String field, Object value) {
        hashOperations.hSet(key, field, value);
    }

    public <T> T hGet(String key, String field, Class<T> clazz) {
        return hashOperations.hGet(key, field, clazz);
    }

    public String hGetString(String key, String field) {
        return hashOperations.hGetString(key, field);
    }

    public Boolean hExists(String key, String field) {
        return hashOperations.hExists(key, field);
    }

    public Long hDelete(String key, String... fields) {
        return hashOperations.hDelete(key, fields);
    }

    public Long hSize(String key) {
        return hashOperations.hSize(key);
    }

    public Set<String> hKeys(String key) {
        return hashOperations.hKeys(key);
    }

    public List<Object> hValues(String key) {
        return hashOperations.hValues(key);
    }

    public Map<String, Object> hGetAll(String key) {
        return hashOperations.hGetAll(key);
    }

    public Long hIncrBy(String key, String field, long delta) {
        return hashOperations.hIncrBy(key, field, delta);
    }

    public Long hIncrBy(String key, String field, int delta) {
        return hashOperations.hIncrBy(key, field, (long) delta);
    }

    // ========== List Operations ==========
    
    public Long lPush(String key, Object... values) {
        return listOperations.lPush(key, values);
    }

    public Long rPush(String key, Object... values) {
        return listOperations.rPush(key, values);
    }

    public <T> T lPop(String key, Class<T> clazz) {
        return listOperations.lPop(key, clazz);
    }

    public <T> T rPop(String key, Class<T> clazz) {
        return listOperations.rPop(key, clazz);
    }

    public Long lSize(String key) {
        return listOperations.lSize(key);
    }

    public <T> List<T> lRange(String key, long start, long end, Class<T> clazz) {
        return listOperations.lRange(key, start, end, clazz);
    }

    public <T> T lIndex(String key, long index, Class<T> clazz) {
        return listOperations.lIndex(key, index, clazz);
    }

    public void lSet(String key, long index, Object value) {
        listOperations.lSet(key, index, value);
    }

    // ========== Set Operations ==========
    
    public Long sAdd(String key, Object... values) {
        return setOperations.sAdd(key, values);
    }

    public Long sRemove(String key, Object... values) {
        return setOperations.sRem(key, values);
    }

    public Boolean sIsMember(String key, Object value) {
        return setOperations.sIsMember(key, value);
    }

    public Long sSize(String key) {
        return setOperations.sSize(key);
    }

    public <T> Set<T> sMembers(String key, Class<T> clazz) {
        return setOperations.sMembers(key, clazz);
    }

    public <T> T sRandomMember(String key, Class<T> clazz) {
        return setOperations.sRandomMember(key, clazz);
    }

    public <T> List<T> sRandomMembers(String key, long count, Class<T> clazz) {
        return setOperations.sRandomMembers(key, count, clazz);
    }

    public <T> T sPop(String key, Class<T> clazz) {
        return setOperations.sPop(key, clazz);
    }

    // ========== ZSet Operations ==========
    
    public Boolean zAdd(String key, Object value, double score) {
        return zSetOperations.zAdd(key, value, score);
    }

    public Long zAdd(String key, Map<Object, Double> scoreMembers) {
        return zSetOperations.zAdd(key, scoreMembers);
    }

    public Long zRemove(String key, Object... values) {
        return zSetOperations.zRem(key, values);
    }

    public Long zRank(String key, Object value) {
        return zSetOperations.zRank(key, value);
    }

    public <T> Set<T> zRange(String key, long start, long end, Class<T> clazz) {
        return zSetOperations.zRange(key, start, end, clazz);
    }

    public <T> Set<ZSetOperations.TypedTuple<T>> zRangeWithScores(String key, long start, long end, Class<T> clazz) {
        return zSetOperations.zRangeWithScores(key, start, end, clazz);
    }

    public <T> Set<T> zRangeByScore(String key, double min, double max, Class<T> clazz) {
        return zSetOperations.zRangeByScore(key, min, max, clazz);
    }

    public Long zCount(String key, double min, double max) {
        return zSetOperations.zCount(key, min, max);
    }

    public Long zSize(String key) {
        return zSetOperations.zSize(key);
    }

    public Double zScore(String key, Object value) {
        return zSetOperations.zScore(key, value);
    }

    // ========== Transaction Operations ==========
    
    public <T> T executeInTransaction(RedisCallback<T> callback) {
        return transactionOperations.executeInTransaction(callback);
    }

    public void executeInTransaction(Consumer<TransactionContext> operations) {
        // 将 Consumer 包装为 RedisCallback
        transactionOperations.executeInTransaction(new RedisCallback<Void>() {
            @Override
            public Void doInRedis(RedisUtils redisUtils) {
                TransactionContext context = new TransactionContext(
                    "tx-" + System.currentTimeMillis(),
                    true,
                    false,
                    30000L,
                    "facade-transaction"
                );
                operations.accept(context);
                return null;
            }
        });
    }

    public boolean isInTransaction() {
        return transactionOperations.isInTransaction();
    }
}
