package com.david.redis.commons.core;

import com.david.redis.commons.core.operations.*;
import com.david.redis.commons.core.operations.interfaces.*;
import com.david.redis.commons.core.operations.support.RedisLoggerHelper;
import com.david.redis.commons.core.operations.support.RedisOperationExecutor;
import com.david.redis.commons.core.operations.support.RedisResultProcessor;
import com.david.redis.commons.core.transaction.RedisTransactionManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis核心工具类 - 门面模式
 *
 * <p>
 * 提供统一的Redis操作接口，支持String、Hash、List、Set、ZSet等数据类型操作， 以及事务支持。
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
    // 分布式锁管理器
    private final RedisLockOperations lockOperations;

    /** 构造函数 */
    public RedisUtils(
            RedisTemplate<String, Object> redisTemplate,
            RedisLockOperations lockOperations,
            RedisOperationExecutor executor,
            RedisResultProcessor resultProcessor,
            RedisLoggerHelper loggerHelper,
            RedisTransactionManager transactionManager) {
        this.redisTemplate = redisTemplate;
        this.lockOperations = lockOperations;

        // 初始化各个操作模块
        this.stringOperations = new RedisStringOperationsImpl(redisTemplate, transactionManager, executor,
                resultProcessor, loggerHelper);
        this.hashOperations = new RedisHashOperationsImpl(redisTemplate, transactionManager, executor, resultProcessor,
                loggerHelper);
        this.listOperations = new RedisListOperationsImpl(redisTemplate, transactionManager, executor, resultProcessor,
                loggerHelper);
        this.setOperations = new RedisSetOperationsImpl(redisTemplate, transactionManager, executor, resultProcessor,
                loggerHelper);
        this.zSetOperations = new RedisZSetOperationsImpl(redisTemplate, transactionManager, executor, resultProcessor,
                loggerHelper);
        this.transactionOperations = new RedisTransactionOperationsImpl(redisTemplate, transactionManager, executor,
                resultProcessor, loggerHelper);
    }

    /** 获取 String 类型操作入口 */
    public RedisStringOperations strings() {
        return this.stringOperations;
    }

    /** 获取 Hash 类型操作入口 */
    public RedisHashOperations hashes() {
        return this.hashOperations;
    }

    /** 获取 List 类型操作入口 */
    public RedisListOperations lists() {
        return this.listOperations;
    }

    /** 获取 Set 类型操作入口 */
    public RedisSetOperations sets() {
        return this.setOperations;
    }

    /** 获取 ZSet 类型操作入口 */
    public RedisZSetOperations zsets() {
        return this.zSetOperations;
    }

    /** 获取事务操作入口 */
    public RedisTransactionOperations tx() {
        return this.transactionOperations;
    }

    /** 获取分布式锁入口 */
    public RedisLockOperations locks() {
        return this.lockOperations;
    }

}
