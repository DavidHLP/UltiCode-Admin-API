package com.david.redis.commons.core.operations.support;

import com.david.redis.commons.core.operations.records.OperationContext;
import com.david.redis.commons.exception.RedisOperationException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Redis操作执行器
 *
 * <p>
 * 统一的Redis操作执行和异常处理组件，支持类型安全的结果处理和增强的日志记录
 *
 * @author David
 */
@Component
@RequiredArgsConstructor
public class RedisOperationExecutor {

    private final RedisResultProcessor resultProcessor;

    /**
     * 执行有返回值的Redis操作（类型安全版本）
     *
     * @param context 操作上下文
     * @param action  要执行的操作
     * @param <R>     期望的返回值类型
     * @return 类型安全的操作结果
     * @throws RedisOperationException 操作失败时抛出
     */
    @SuppressWarnings("unchecked")
    public <R> R executeTypeSafe(OperationContext<?, R> context, Supplier<Object> action) {
        long startTime = System.currentTimeMillis();

        try {

            // 执行操作
            Object rawResult = action.get();

            // 类型安全的结果转换
            R result = convertResult(rawResult, context);

            return result;

        } catch (Exception e) {
            throw createRedisOperationException(context, e);
        }
    }

    /**
     * 执行有返回值的Redis操作
     *
     * @param context 操作上下文
     * @param action  要执行的操作
     * @param <T>     返回值类型
     * @return 操作结果
     * @throws RedisOperationException 操作失败时抛出
     */
    public <T> T execute(OperationContext<?, ?> context, Supplier<T> action) {
        long startTime = System.currentTimeMillis();

        try {

            T result = action.get();

            return result;

        } catch (Exception e) {
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
    public void execute(OperationContext<?, ?> context, Runnable action) {
        execute(
                context,
                () -> {
                    action.run();
                    return null;
                });
    }

    /**
     * 转换结果到期望的类型
     *
     * @param rawResult 原始结果
     * @param context   操作上下文
     * @param <R>       期望的返回值类型
     * @return 转换后的结果
     */
    @SuppressWarnings("unchecked")
    private <R> R convertResult(Object rawResult, OperationContext<?, R> context) {
        if (rawResult == null) {
            return null;
        }

        Class<R> returnType = context.returnType();
        if (returnType == null || returnType == Object.class) {
            return (R) rawResult;
        }

        try {
            return resultProcessor.convertSingle(rawResult, returnType);
        } catch (Exception e) {
            // 转换失败时，如果期望类型可以直接赋值，则返回原始结果
            if (returnType.isAssignableFrom(rawResult.getClass())) {
                return (R) rawResult;
            }

            throw new RedisOperationException(
                    String.format("结果类型转换失败，期望类型: %s，实际类型: %s",
                            returnType.getSimpleName(),
                            rawResult.getClass().getSimpleName()),
                    e, context.operation(), context.key());
        }
    }


    /**
     * 创建Redis操作异常
     *
     * @param context 操作上下文
     * @param cause   原始异常
     * @return Redis操作异常
     */
    private RedisOperationException createRedisOperationException(
            OperationContext<?, ?> context, Exception cause) {
        String message = String.format("Redis操作失败: %s", context.getFormattedDescription());

        if (context.hasParams()) {
            return new RedisOperationException(
                    message, cause, context.operation(), context.key(), context.params());
        } else {
            return new RedisOperationException(message, cause, context.operation(), context.key());
        }
    }
}
