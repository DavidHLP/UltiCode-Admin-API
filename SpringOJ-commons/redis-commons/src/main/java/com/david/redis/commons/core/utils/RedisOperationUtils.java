package com.david.redis.commons.core.utils;

import com.david.redis.commons.core.transaction.RedisTransactionManager;
import com.david.redis.commons.exception.RedisOperationException;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * Redis操作工具类
 * 
 * <p>提供统一的异常处理、日志记录和操作记录功能
 * 
 * @author David
 */
@Slf4j
public class RedisOperationUtils {

    /**
     * 执行Redis操作并处理异常
     * 
     * @param operation 操作名称
     * @param key 操作的键
     * @param action 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     * @throws RedisOperationException 操作失败时抛出
     */
    public static <T> T executeWithExceptionHandling(
            String operation, 
            String key, 
            Supplier<T> action) {
        try {
            log.debug("Executing operation: {} for key: {}", operation, key);
            return action.get();
        } catch (Exception e) {
            log.error("Failed to execute operation: {} for key: {}", operation, key, e);
            throw new RedisOperationException(
                "Redis操作失败: " + operation, e, operation, key);
        }
    }

    /**
     * 执行Redis操作并处理异常（无返回值版本）
     * 
     * @param operation 操作名称
     * @param key 操作的键
     * @param action 要执行的操作
     * @throws RedisOperationException 操作失败时抛出
     */
    public static void executeWithExceptionHandling(
            String operation, 
            String key, 
            Runnable action) {
        executeWithExceptionHandling(operation, key, () -> {
            action.run();
            return null;
        });
    }

    /**
     * 执行Redis操作并处理异常（带额外参数）
     * 
     * @param operation 操作名称
     * @param key 操作的键
     * @param extraParams 额外参数
     * @param action 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     * @throws RedisOperationException 操作失败时抛出
     */
    public static <T> T executeWithExceptionHandling(
            String operation, 
            String key, 
            Object[] extraParams,
            Supplier<T> action) {
        try {
            log.debug("Executing operation: {} for key: {} with params: {}", 
                operation, key, extraParams);
            return action.get();
        } catch (Exception e) {
            log.error("Failed to execute operation: {} for key: {} with params: {}", 
                operation, key, extraParams, e);
            throw new RedisOperationException(
                "Redis操作失败: " + operation, e, operation, key, extraParams);
        }
    }

    /**
     * 记录操作（如果在事务中）
     * 
     * @param transactionManager 事务管理器
     * @param operation 操作描述
     */
    public static void recordOperation(RedisTransactionManager transactionManager, String operation) {
        if (transactionManager != null && transactionManager.isInTransaction()) {
            transactionManager.addOperation(operation);
        }
    }

    /**
     * 记录调试日志
     * 
     * @param message 日志消息
     * @param args 参数
     */
    public static void logDebug(String message, Object... args) {
        if (log.isDebugEnabled()) {
            log.debug(message, args);
        }
    }

    /**
     * 记录错误日志
     * 
     * @param message 日志消息
     * @param throwable 异常
     * @param args 参数
     */
    public static void logError(String message, Throwable throwable, Object... args) {
        log.error(message, args, throwable);
    }
}
