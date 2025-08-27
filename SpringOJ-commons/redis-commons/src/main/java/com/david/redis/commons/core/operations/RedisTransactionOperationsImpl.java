package com.david.redis.commons.core.operations;

import com.david.redis.commons.core.lock.RedisCallback;
import com.david.redis.commons.core.operations.interfaces.RedisTransactionOperations;
import com.david.redis.commons.core.operations.support.AbstractRedisOperations;
import com.david.redis.commons.core.operations.support.RedisLoggerHelper;
import com.david.redis.commons.core.operations.support.RedisOperationExecutor;
import com.david.redis.commons.core.operations.support.RedisResultProcessor;
import com.david.redis.commons.core.transaction.RedisTransactionManager;
import com.david.redis.commons.core.transaction.TransactionContext;
import com.david.redis.commons.exception.RedisOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Redis事务操作实现类
 * 
 * <p>
 * 实现所有事务相关的Redis操作方法
 * 
 * @author David
 */
@Slf4j
public class RedisTransactionOperationsImpl extends AbstractRedisOperations implements RedisTransactionOperations {

    public RedisTransactionOperationsImpl(RedisTemplate<String, Object> redisTemplate,
            RedisTransactionManager transactionManager,
            RedisOperationExecutor executor,
            RedisResultProcessor resultProcessor,
            RedisLoggerHelper loggerHelper) {
        super(redisTemplate, transactionManager, executor, resultProcessor, loggerHelper);
    }

    @Override
    protected String getOperationType() {
        return "TRANSACTION";
    }

    @Override
    public <T> T executeInTransaction(RedisCallback<T> callback) {
        return executeOperation("MULTI", "transaction", () -> {
            return redisTemplate.execute(new SessionCallback<T>() {
                @Override
                @SuppressWarnings({ "unchecked", "rawtypes" })
                public T execute(RedisOperations operations)
                        throws org.springframework.dao.DataAccessException {

                    // 开始事务
                    operations.multi();

                    try {
                        // 执行回调中的操作
                        T result = callback.doInRedis(null); // 传入null，因为我们使用门面模式

                        // 提交事务
                        List<Object> execResults = operations.exec();

                        if (execResults == null) {
                            log.warn("Redis transaction was discarded (WATCH key was modified)");
                            throw new RedisOperationException(
                                    "Redis事务被丢弃，可能是监视的键被修改", (String) null, "EXEC");
                        }

                        log.debug("Redis transaction committed successfully with {} operations",
                                execResults.size());
                        return result;

                    } catch (Exception e) {
                        log.error("Error in Redis transaction, discarding", e);
                        // 发生异常时，事务会自动被丢弃
                        operations.discard();
                        throw new RedisOperationException("Redis事务执行失败", e, "TRANSACTION");
                    }
                }
            });
        });
    }

    @Override
    public void executeInTransaction(Consumer<Object> action) {
        executeInTransaction(ops -> {
            action.accept(ops);
            return null;
        });
    }

    @Override
    public void watch(String... keys) {
        executeOperation("WATCH", Arrays.toString(keys), new Object[] { keys }, () -> {
            redisTemplate.watch(Arrays.asList(keys));
        });
    }

    @Override
    public void unwatch() {
        executeOperation("UNWATCH", "all-keys", () -> {
            redisTemplate.unwatch();
        });
    }

    @Override
    public boolean isInTransaction() {
        return transactionManager != null && transactionManager.isInTransaction();
    }

    @Override
    public TransactionContext getCurrentTransactionContext() {
        return transactionManager != null ? transactionManager.getCurrentTransaction() : null;
    }
}
