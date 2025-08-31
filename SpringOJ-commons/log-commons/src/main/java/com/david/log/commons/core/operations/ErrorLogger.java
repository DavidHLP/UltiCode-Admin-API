package com.david.log.commons.core.operations;

import com.david.log.commons.core.enums.LogLevel;
import com.david.log.commons.core.operations.support.BaseLogger;

import lombok.extern.slf4j.Slf4j;

/**
 * ERROR级别日志实现类
 *
 * <p>用于输出错误信息，表示系统发生了错误或异常</p>
 *
 * @author David
 * @version 1.0
 * @since 2024-08-31
 */
@Slf4j
public final class ErrorLogger extends BaseLogger {

    /**
     * 单例实例
     */
    private static volatile ErrorLogger instance;

    /**
     * 私有构造函数
     */
    private ErrorLogger() {
        super("com.david.log.commons.ERROR", LogLevel.ERROR);
    }

    /**
     * 获取单例实例
     *
     * @return ErrorLogger实例
     */
    public static ErrorLogger getInstance() {
        if (instance == null) {
            synchronized (ErrorLogger.class) {
                if (instance == null) {
                    instance = new ErrorLogger();
                }
            }
        }
        return instance;
    }

    /**
     * 静态便捷方法 - 记录ERROR日志
     *
     * @param message 日志消息
     * @param args 参数数组
     */
    public static void error(String message, Object... args) {
        getInstance().log(message, args);
    }

    /**
     * 静态便捷方法 - 记录ERROR日志（带异常信息）
     *
     * @param message 日志消息
     * @param throwable 异常对象
     * @param args 参数数组
     */
    public static void error(String message, Throwable throwable, Object... args) {
        getInstance().log(message, throwable, args);
    }

    /**
     * 记录ERROR级别日志
     *
     * @param message 日志消息
     * @param args 参数数组
     */
    public void log(String message, Object... args) {
        if (validateMessage(message)) {
            return;
        }

        // 直接使用SequentialLogger，自动保证顺序性
        if (sequentialLogger.isErrorEnabled()) {
            sequentialLogger.error(message, args);
        }
    }

    /**
     * 记录ERROR级别日志（带异常信息）
     *
     * @param message 日志消息
     * @param throwable 异常对象
     */
    public void log(String message, Throwable throwable) {
        if (validateMessage(message)) {
            return;
        }

        // 使用SequentialLogger记录带异常的ERROR日志
        if (sequentialLogger.isErrorEnabled()) {
            sequentialLogger.error(message, throwable);
        }
    }

    /**
     * 检查ERROR级别是否启用
     *
     * @return 如果ERROR级别启用则返回true
     */
    public boolean isErrorEnabled() {
        return sequentialLogger.isErrorEnabled();
    }
}
