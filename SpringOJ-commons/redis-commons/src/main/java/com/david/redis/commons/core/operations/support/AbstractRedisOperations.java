package com.david.redis.commons.core.operations.support;

import com.david.log.commons.LogUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.function.Supplier;

/**
 * Redis操作抽象基类
 *
 * <p>提供统一的Redis操作模板方法和通用功能，支持类型安全和增强的日志记录
 *
 * @author David
 */
@Getter
@RequiredArgsConstructor
public abstract class AbstractRedisOperations {

    protected final RedisTemplate<String, Object> redisTemplate;
    protected final RedisOperationExecutor executor;
    protected final RedisResultProcessor resultProcessor;
    protected final LogUtils logUtils;

    // ===== 类型安全的操作方法 =====

    /**
     * 执行类型安全的Redis操作（使用操作类型枚举）
     *
     * @param operationType 操作类型枚举
     * @param key Redis键名
     * @param returnType 期望的返回类型
     * @param action 要执行的操作
     * @param <R> 返回值类型
     * @return 类型安全的操作结果
     */
    protected final <R> R executeOperation(
            RedisOperationType operationType, String key, Class<R> returnType, Supplier<Object> action) {
        OperationContext<Void, R> context = OperationContext.of(operationType, key, returnType);
        return executor.executeTypeSafe(context, action);
    }

    /**
     * 执行带参数的类型安全Redis操作（使用操作类型枚举）
     *
     * @param operationType 操作类型枚举
     * @param key Redis键名
     * @param params 操作参数
     * @param returnType 期望的返回类型
     * @param action 要执行的操作
     * @param <T> 参数类型
     * @param <R> 返回值类型
     * @return 类型安全的操作结果
     */
    protected final <T, R> R executeOperation(
            RedisOperationType operationType, String key, T params, Class<R> returnType, Supplier<Object> action) {
        OperationContext<T, R> context = OperationContext.of(operationType, key, params, returnType);
        return executor.executeTypeSafe(context, action);
    }

    /**
     * 执行类型安全的Redis操作（使用字符串操作类型）
     *
     * @param operation 操作类型
     * @param key Redis键名
     * @param returnType 期望的返回类型
     * @param action 要执行的操作
     * @param <R> 返回值类型
     * @return 类型安全的操作结果
     */
    protected final <R> R executeOperation(
            String operation, String key, Class<R> returnType, Supplier<Object> action) {
        OperationContext<Void, R> context = OperationContext.ofNoParams(operation, key, returnType);
        return executor.executeTypeSafe(context, action);
    }

    /**
     * 执行带参数的类型安全Redis操作（使用字符串操作类型）
     *
     * @param operation 操作类型
     * @param key Redis键名
     * @param params 操作参数
     * @param returnType 期望的返回类型
     * @param action 要执行的操作
     * @param <T> 参数类型
     * @param <R> 返回值类型
     * @return 类型安全的操作结果
     */
    protected final <T, R> R executeOperation(
            String operation, String key, T params, Class<R> returnType, Supplier<Object> action) {
        OperationContext<T, R> context = OperationContext.of(operation, key, params, returnType);
        return executor.executeTypeSafe(context, action);
    }

    // ===== 向后兼容的操作方法 =====

    /**
     * 执行Redis操作的模板方法（向后兼容）
     *
     * @param operation 操作类型
     * @param key Redis键名
     * @param action 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     */
    protected final <T> T executeOperation(String operation, String key, Supplier<T> action) {
        OperationContext<Void, Object> context = OperationContext.ofNoParams(operation, key, Object.class);
        return executor.execute(context, action);
    }

    /**
     * 执行带参数的Redis操作的模板方法（向后兼容）
     *
     * @param operation 操作类型
     * @param key Redis键名
     * @param params 操作参数
     * @param action 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     */
    protected final <T> T executeOperation(
            String operation, String key, Object params, Supplier<T> action) {
        OperationContext<Object, Object> context = OperationContext.of(operation, key, params, Object.class);
        return executor.execute(context, action);
    }

    /**
     * 执行无返回值的Redis操作的模板方法（向后兼容）
     *
     * @param operation 操作类型
     * @param key Redis键名
     * @param action 要执行的操作
     */
    protected final void executeOperation(String operation, String key, Runnable action) {
        OperationContext<Void, Object> context = OperationContext.ofNoParams(operation, key, Object.class);
        executor.execute(context, action);
    }

	// ===== 便捷方法 =====

    /**
     * 执行String类型返回值的操作
     *
     * @param key    Redis键名
     * @param action 要执行的操作
     * @return String类型结果
     */
    protected final String executeStringOperation(String key, Supplier<Object> action) {
        return executeOperation(RedisOperationType.GET, key, String.class, action);
    }

    /**
     * 执行Boolean类型返回值的操作
     *
     * @param operationType 操作类型
     * @param key Redis键名
     * @param action 要执行的操作
     * @return Boolean类型结果
     */
    protected final Boolean executeBooleanOperation(RedisOperationType operationType, String key, Supplier<Object> action) {
        return executeOperation(operationType, key, Boolean.class, action);
    }

    /**
     * 执行Long类型返回值的操作
     *
     * @param operationType 操作类型
     * @param key Redis键名
     * @param action 要执行的操作
     * @return Long类型结果
     */
    protected final Long executeLongOperation(RedisOperationType operationType, String key, Supplier<Object> action) {
        return executeOperation(operationType, key, Long.class, action);
    }

    /**
     * 执行带参数的String类型返回值的操作
     *
     * @param operationType 操作类型
     * @param key Redis键名
     * @param params 操作参数
     * @param action 要执行的操作
     * @param <T> 参数类型
     * @return String类型结果
     */
    protected final <T> String executeStringOperation(
            RedisOperationType operationType, String key, T params, Supplier<Object> action) {
        return executeOperation(operationType, key, params, String.class, action);
    }

    /**
     * 执行带参数的Boolean类型返回值的操作
     *
     * @param <T>    参数类型
     * @param key    Redis键名
     * @param params 操作参数
     * @param action 要执行的操作
     * @return Boolean类型结果
     */
    protected final <T> Boolean executeBooleanOperation(
            String key, T params, Supplier<Object> action) {
        return executeOperation(RedisOperationType.EXPIRE, key, params, Boolean.class, action);
    }

    /**
     * 执行带参数的Long类型返回值的操作
     *
     * @param <T>    参数类型
     * @param key    Redis键名
     * @param params 操作参数
     * @param action 要执行的操作
     * @return Long类型结果
     */
    protected final <T> Long executeLongOperation(
            String key, T params, Supplier<Object> action) {
        return executeOperation(RedisOperationType.DEL, key, params, Long.class, action);
    }

    /**
     * 执行无返回值的操作（使用操作类型枚举）
     *
     * @param operationType 操作类型
     * @param key Redis键名
     * @param action 要执行的操作
     */
    protected final void executeVoidOperation(RedisOperationType operationType, String key, Runnable action) {
        OperationContext<Void, Void> context = OperationContext.of(operationType, key, Void.class);
        executor.execute(context, action);
    }

    /**
     * 执行带参数的无返回值操作（使用操作类型枚举）
     *
     * @param operationType 操作类型
     * @param key Redis键名
     * @param params 操作参数
     * @param action 要执行的操作
     * @param <T> 参数类型
     */
    protected final <T> void executeVoidOperation(
            RedisOperationType operationType, String key, T params, Runnable action) {
        OperationContext<T, Void> context = OperationContext.of(operationType, key, params, Void.class);
        executor.execute(context, action);
    }
}
