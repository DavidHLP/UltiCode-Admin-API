package com.david.redis.commons.core.operations.support;

import com.david.log.commons.core.LogUtils;
import com.david.redis.commons.core.transaction.RedisTransactionManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.function.Supplier;

/**
 * Redis操作抽象基类
 *
 * <p>提供统一的Redis操作模板方法和通用功能
 *
 * @author David
 */
@Getter
@RequiredArgsConstructor
public abstract class AbstractRedisOperations {

    protected final RedisTemplate<String, Object> redisTemplate;
    protected final RedisTransactionManager transactionManager;
    protected final RedisOperationExecutor executor;
    protected final RedisResultProcessor resultProcessor;
    protected final RedisLoggerHelper loggerHelper;
    protected final LogUtils logUtils;

    /**
     * 执行Redis操作的模板方法
     *
     * @param operation 操作类型
     * @param key Redis键名
     * @param action 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     */
    protected final <T> T executeOperation(String operation, String key, Supplier<T> action) {
        OperationContext context = OperationContext.of(operation, key, null, transactionManager);
        return executor.execute(context, action);
    }

    /**
     * 执行带参数的Redis操作的模板方法
     *
     * @param operation 操作类型
     * @param key Redis键名
     * @param params 操作参数
     * @param action 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     */
    protected final <T> T executeOperation(
            String operation, String key, Object[] params, Supplier<T> action) {
        OperationContext context = OperationContext.of(operation, key, params, transactionManager);
        return executor.execute(context, action);
    }

    /**
     * 执行无返回值的Redis操作的模板方法
     *
     * @param operation 操作类型
     * @param key Redis键名
     * @param action 要执行的操作
     */
    protected final void executeOperation(String operation, String key, Runnable action) {
        OperationContext context = OperationContext.of(operation, key, null, transactionManager);
        executor.execute(context, action);
    }

    /**
     * 执行带参数且无返回值的Redis操作的模板方法
     *
     * @param operation 操作类型
     * @param key Redis键名
     * @param params 操作参数
     * @param action 要执行的操作
     */
    protected final void executeOperation(
            String operation, String key, Object[] params, Runnable action) {
        OperationContext context = OperationContext.of(operation, key, params, transactionManager);
        executor.execute(context, action);
    }

    /**
     * 获取操作类型标识
     *
     * @return 操作类型标识
     */
    protected abstract String getOperationType();
}
