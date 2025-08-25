package com.david.redis.commons.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Redis事务异常类
 *
 * 用于处理Redis事务操作过程中发生的异常，包括事务提交失败、
 * 回滚失败、嵌套事务处理异常等场景。
 *
 * @author David
 */
public class RedisTransactionException extends RedisOperationException {

    /**
     * 失败的操作列表
     */
    private final List<String> failedOperations;

    /**
     * 事务ID（用于嵌套事务跟踪）
     */
    private final String transactionId;

    /**
     * 是否为回滚异常
     */
    private final boolean isRollback;

    /**
     * 构造函数
     *
     * @param message 异常消息
     */
    public RedisTransactionException(String message) {
        super(message, "REDIS_TRANSACTION");
        this.failedOperations = new ArrayList<>();
        this.transactionId = null;
        this.isRollback = false;
    }

    /**
     * 构造函数，包含失败操作列表
     *
     * @param message          异常消息
     * @param failedOperations 失败的操作列表
     */
    public RedisTransactionException(String message, List<String> failedOperations) {
        super(buildTransactionMessage(message, failedOperations, null, false), "REDIS_TRANSACTION", failedOperations);
        this.failedOperations = failedOperations != null ? new ArrayList<>(failedOperations) : new ArrayList<>();
        this.transactionId = null;
        this.isRollback = false;
    }

    /**
     * 构造函数，包含事务ID
     *
     * @param message          异常消息
     * @param failedOperations 失败的操作列表
     * @param transactionId    事务ID
     */
    public RedisTransactionException(String message, List<String> failedOperations, String transactionId) {
        super(buildTransactionMessage(message, failedOperations, transactionId, false), "REDIS_TRANSACTION",
                failedOperations, transactionId);
        this.failedOperations = failedOperations != null ? new ArrayList<>(failedOperations) : new ArrayList<>();
        this.transactionId = transactionId;
        this.isRollback = false;
    }

    /**
     * 构造函数，包含回滚标识
     *
     * @param message          异常消息
     * @param failedOperations 失败的操作列表
     * @param transactionId    事务ID
     * @param isRollback       是否为回滚异常
     */
    public RedisTransactionException(String message, List<String> failedOperations, String transactionId,
            boolean isRollback) {
        super(buildTransactionMessage(message, failedOperations, transactionId, isRollback), "REDIS_TRANSACTION",
                failedOperations, transactionId, isRollback);
        this.failedOperations = failedOperations != null ? new ArrayList<>(failedOperations) : new ArrayList<>();
        this.transactionId = transactionId;
        this.isRollback = isRollback;
    }

    /**
     * 构造函数，包含原始异常
     *
     * @param message          异常消息
     * @param cause            原始异常
     * @param failedOperations 失败的操作列表
     */
    public RedisTransactionException(String message, Throwable cause, List<String> failedOperations) {
        super(buildTransactionMessage(message, failedOperations, null, false), cause, "REDIS_TRANSACTION",
                failedOperations);
        this.failedOperations = failedOperations != null ? new ArrayList<>(failedOperations) : new ArrayList<>();
        this.transactionId = null;
        this.isRollback = false;
    }

    /**
     * 构造函数，包含原始异常和完整参数
     *
     * @param message          异常消息
     * @param cause            原始异常
     * @param failedOperations 失败的操作列表
     * @param transactionId    事务ID
     * @param isRollback       是否为回滚异常
     */
    public RedisTransactionException(String message, Throwable cause, List<String> failedOperations,
            String transactionId, boolean isRollback) {
        super(buildTransactionMessage(message, failedOperations, transactionId, isRollback), cause, "REDIS_TRANSACTION",
                failedOperations, transactionId, isRollback);
        this.failedOperations = failedOperations != null ? new ArrayList<>(failedOperations) : new ArrayList<>();
        this.transactionId = transactionId;
        this.isRollback = isRollback;
    }

    /**
     * 获取失败操作列表的不可变副本
     *
     * @return 失败操作列表
     */
    public List<String> getFailedOperations() {
        return Collections.unmodifiableList(failedOperations);
    }

    /**
     * 获取事务ID
     *
     * @return 事务ID，可能为null
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * 是否为回滚异常
     *
     * @return true如果是回滚异常
     */
    public boolean isRollback() {
        return isRollback;
    }

    /**
     * 添加失败操作
     *
     * @param operation 失败的操作
     */
    public void addFailedOperation(String operation) {
        if (operation != null) {
            this.failedOperations.add(operation);
        }
    }

    /**
     * 创建事务提交失败异常
     *
     * @param transactionId    事务ID
     * @param failedOperations 失败的操作列表
     * @return 事务提交失败异常
     */
    public static RedisTransactionException commitFailed(String transactionId, List<String> failedOperations) {
        return new RedisTransactionException(
                "Redis事务提交失败",
                failedOperations,
                transactionId,
                false);
    }

    /**
     * 创建事务回滚失败异常
     *
     * @param transactionId 事务ID
     * @param cause         原始异常
     * @return 事务回滚失败异常
     */
    public static RedisTransactionException rollbackFailed(String transactionId, Throwable cause) {
        return new RedisTransactionException(
                "Redis事务回滚失败",
                cause,
                new ArrayList<>(),
                transactionId,
                true);
    }

    /**
     * 创建嵌套事务异常
     *
     * @param parentTransactionId 父事务ID
     * @param childTransactionId  子事务ID
     * @return 嵌套事务异常
     */
    public static RedisTransactionException nestedTransactionError(String parentTransactionId,
            String childTransactionId) {
        return new RedisTransactionException(
                String.format("嵌套事务处理异常，父事务: %s，子事务: %s", parentTransactionId, childTransactionId),
                new ArrayList<>(),
                childTransactionId,
                false);
    }

    /**
     * 创建事务超时异常
     *
     * @param transactionId 事务ID
     * @param timeoutMillis 超时时间（毫秒）
     * @return 事务超时异常
     */
    public static RedisTransactionException transactionTimeout(String transactionId, long timeoutMillis) {
        return new RedisTransactionException(
                String.format("Redis事务执行超时: %d ms", timeoutMillis),
                new ArrayList<>(),
                transactionId,
                false);
    }

    /**
     * 构建包含事务信息的详细消息
     *
     * @param message          基础消息
     * @param failedOperations 失败操作列表
     * @param transactionId    事务ID
     * @param isRollback       是否为回滚
     * @return 详细消息
     */
    private static String buildTransactionMessage(String message, List<String> failedOperations, String transactionId,
            boolean isRollback) {
        StringBuilder sb = new StringBuilder(message);

        if (transactionId != null) {
            sb.append(" [事务ID: ").append(transactionId).append("]");
        }

        if (isRollback) {
            sb.append(" [回滚操作]");
        }

        if (failedOperations != null && !failedOperations.isEmpty()) {
            sb.append(" [失败操作: ");
            for (int i = 0; i < failedOperations.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(failedOperations.get(i));
            }
            sb.append("]");
        }

        return sb.toString();
    }
}