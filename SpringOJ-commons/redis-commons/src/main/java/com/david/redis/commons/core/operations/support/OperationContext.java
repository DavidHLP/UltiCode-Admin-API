package com.david.redis.commons.core.operations.support;

import com.david.redis.commons.core.transaction.RedisTransactionManager;

import lombok.Builder;

/**
 * Redis操作上下文
 *
 * <p>封装Redis操作的上下文信息，包括操作类型、键名、参数等
 *
 * @author David
 * @param operation 操作类型（如 GET, SET, HGET 等）
 * @param key Redis键名
 * @param params 操作参数
 * @param transactionManager 事务管理器
 * @param description 操作描述（用于日志）
 */
@Builder
public record OperationContext(
        String operation,
        String key,
        Object[] params,
        RedisTransactionManager transactionManager,
        String description) {

    /**
     * 创建简单的操作上下文
     *
     * @param operation 操作类型
     * @param key Redis键名
     * @return 操作上下文
     */
    public static OperationContext of(String operation, String key) {
        return OperationContext.builder().operation(operation).key(key).build();
    }

    /**
     * 创建带参数的操作上下文
     *
     * @param operation 操作类型
     * @param key Redis键名
     * @param params 操作参数
     * @return 操作上下文
     */
    public static OperationContext of(String operation, String key, Object[] params) {
        return OperationContext.builder().operation(operation).key(key).params(params).build();
    }

    /**
     * 创建完整的操作上下文
     *
     * @param operation 操作类型
     * @param key Redis键名
     * @param params 操作参数
     * @param transactionManager 事务管理器
     * @return 操作上下文
     */
    public static OperationContext of(
            String operation,
            String key,
            Object[] params,
            RedisTransactionManager transactionManager) {
        return OperationContext.builder()
                .operation(operation)
                .key(key)
                .params(params)
                .transactionManager(transactionManager)
                .build();
    }

    /**
     * 获取参数数量
     *
     * @return 参数数量
     */
    public int getParamCount() {
        return params != null ? params.length : 0;
    }

    /**
     * 检查是否在事务中
     *
     * @return 是否在事务中
     */
    public boolean isInTransaction() {
        return transactionManager != null && transactionManager.isInTransaction();
    }

    /**
     * 获取格式化的操作描述
     *
     * @return 操作描述
     */
    public String getFormattedDescription() {
        if (description != null) {
            return description;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(operation).append(" ").append(key);

        if (params != null && params.length > 0) {
            sb.append(" with ").append(params.length).append(" params");
        }

        return sb.toString();
    }
}
