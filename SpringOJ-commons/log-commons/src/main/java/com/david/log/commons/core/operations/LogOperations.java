package com.david.log.commons.core.operations;

/**
 * 日志操作基础接口
 * 
 * <p>
 * 定义统一的日志记录操作，支持不同级别的日志输出，
 * 提供结构化日志和上下文信息支持。
 * 
 * @author David
 */
public interface LogOperations {

    /**
     * 记录信息日志
     * 
     * @param message 日志消息
     * @param args    格式化参数
     */
    void info(String message, Object... args);

    /**
     * 记录调试日志
     * 
     * @param message 日志消息
     * @param args    格式化参数
     */
    void debug(String message, Object... args);

    /**
     * 记录警告日志
     * 
     * @param message 日志消息
     * @param args    格式化参数
     */
    void warn(String message, Object... args);

    /**
     * 记录错误日志
     * 
     * @param message   日志消息
     * @param throwable 异常信息
     * @param args      格式化参数
     */
    void error(String message, Throwable throwable, Object... args);

    /**
     * 记录错误日志（无异常信息）
     * 
     * @param message 日志消息
     * @param args    格式化参数
     */
    void error(String message, Object... args);

    /**
     * 判断是否启用指定级别的日志
     * 
     * @param level 日志级别
     * @return 是否启用
     */
    boolean isEnabled(LogLevel level);
}
