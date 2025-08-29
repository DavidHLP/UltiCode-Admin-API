package com.david.log.commons.core.operations;

/**
 * 异常日志操作接口
 * 
 * <p>
 * 专门用于异常信息的日志记录，包括异常堆栈记录、
 * 异常分类统计、错误恢复建议等功能。
 * 
 * @author David
 */
public interface ExceptionLogOperations extends LogOperations {

    /**
     * 记录业务异常日志
     * 
     * @param operation 操作名称
     * @param exception 异常信息
     * @param context   业务上下文
     */
    void business(String operation, Throwable exception, Object... context);

    /**
     * 记录系统异常日志
     * 
     * @param component 组件名称
     * @param exception 异常信息
     * @param severity  严重程度
     */
    void system(String component, Throwable exception, String severity);

    /**
     * 记录网络异常日志
     * 
     * @param endpoint   端点地址
     * @param exception  异常信息
     * @param retryCount 重试次数
     */
    void network(String endpoint, Throwable exception, int retryCount);

    /**
     * 记录数据库异常日志
     * 
     * @param sql        SQL语句
     * @param exception  异常信息
     * @param parameters SQL参数
     */
    void database(String sql, Throwable exception, Object... parameters);

    /**
     * 记录验证异常日志
     * 
     * @param field   字段名称
     * @param value   字段值
     * @param rule    验证规则
     * @param message 错误消息
     */
    void validation(String field, Object value, String rule, String message);
}
