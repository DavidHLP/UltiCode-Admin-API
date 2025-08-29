package com.david.redis.commons.core.operations.support;

import com.david.redis.commons.exception.RedisOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Redis操作执行器
 * 
 * <p>
 * 统一的Redis操作执行和异常处理组件
 * 
 * @author David
 */
@Component
@RequiredArgsConstructor
public class RedisOperationExecutor {

    private final RedisLoggerHelper loggerHelper;

    /**
     * 执行有返回值的Redis操作
     * 
     * @param context 操作上下文
     * @param action  要执行的操作
     * @param <T>     返回值类型
     * @return 操作结果
     * @throws RedisOperationException 操作失败时抛出
     */
    public <T> T execute(OperationContext context, Supplier<T> action) {
        try {
            loggerHelper.logOperationStart(context);
            recordOperationIfInTransaction(context);

            T result = action.get();

            loggerHelper.logOperationResult(context, result);
            return result;

        } catch (Exception e) {
            loggerHelper.logOperationError(context, e);
            throw createRedisOperationException(context, e);
        }
    }

    /**
     * 执行无返回值的Redis操作
     * 
     * @param context 操作上下文
     * @param action  要执行的操作
     * @throws RedisOperationException 操作失败时抛出
     */
    public void execute(OperationContext context, Runnable action) {
        execute(context, () -> {
            action.run();
            return null;
        });
    }

    /**
     * 记录事务操作（如果在事务中）
     * 
     * @param context 操作上下文
     */
    private void recordOperationIfInTransaction(OperationContext context) {
        if (context.isInTransaction()) {
            String operationRecord = context.operation() + " " + context.key();
            context.transactionManager().addOperation(operationRecord);
        }
    }

    /**
     * 创建Redis操作异常
     * 
     * @param context 操作上下文
     * @param cause   原始异常
     * @return Redis操作异常
     */
    private RedisOperationException createRedisOperationException(OperationContext context, Exception cause) {
        String message = String.format("Redis操作失败: %s", context.operation());

        if (context.params() != null && context.params().length > 0) {
            return new RedisOperationException(message, cause,
                    context.operation(), context.key(), context.params());
        } else {
            return new RedisOperationException(message, cause,
                    context.operation(), context.key());
        }
    }
}
