package com.david.redis.commons.exception;

import lombok.Getter;

/**
 * Redis操作异常基类
 * <p>
 * 提供Redis操作过程中发生异常时的统一异常处理机制，
 * 包含操作上下文信息以便于问题诊断和调试。
 *
 * @author David
 */
@Getter
public class RedisOperationException extends RuntimeException {

    /**
     * 执行的Redis操作名称
     * -- GETTER --
     *  获取操作名称
     *
     */
    private final String operation;

    /**
     * 操作参数
     */
    private final Object[] parameters;

	/**
     * 构造函数，包含操作上下文
     *
     * @param message    异常消息
     * @param operation  执行的Redis操作
     * @param parameters 操作参数
     */
    public RedisOperationException(String message, String operation, Object... parameters) {
        super(buildDetailedMessage(message, operation, parameters));
        this.operation = operation;
        this.parameters = parameters != null ? parameters.clone() : null;
    }

    /**
     * 构造函数，包含操作上下文和原始异常
     *
     * @param message    异常消息
     * @param cause      原始异常
     * @param operation  执行的Redis操作
     * @param parameters 操作参数
     */
    public RedisOperationException(String message, Throwable cause, String operation, Object... parameters) {
        super(buildDetailedMessage(message, operation, parameters), cause);
        this.operation = operation;
        this.parameters = parameters != null ? parameters.clone() : null;
    }

    /**
     * 构建包含操作上下文的详细错误消息
     *
     * @param message    基础消息
     * @param operation  操作名称
     * @param parameters 操作参数
     * @return 详细的错误消息
     */
    private static String buildDetailedMessage(String message, String operation, Object[] parameters) {
        StringBuilder sb = new StringBuilder();
        sb.append(message);

        if (operation != null) {
            sb.append(" [操作: ").append(operation).append("]");
        }

        if (parameters != null && parameters.length > 0) {
            sb.append(" [参数: ");
            for (int i = 0; i < parameters.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(parameters[i]);
            }
            sb.append("]");
        }

        return sb.toString();
    }

}