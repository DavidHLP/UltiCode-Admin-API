package com.david.log.commons.core.operations;

import com.david.log.commons.core.enums.LogLevel;
import com.david.log.commons.core.operations.support.BaseLogger;

/**
 * INFO级别日志实现类
 * 
 * <p>
 * 用于输出一般性信息，是最常用的日志级别
 * </p>
 * 
 * @author David
 * @version 1.0
 * @since 2024-08-31
 */
public final class InfoLogger extends BaseLogger {

    /** 单例实例 */
    private static volatile InfoLogger instance;

    /** 私有构造函数 */
    private InfoLogger() {
        super("com.david.log.commons.INFO", LogLevel.INFO);
    }

    /**
     * 获取单例实例
     *
     * @return InfoLogger实例
     */
    public static InfoLogger getInstance() {
        if (instance == null) {
            synchronized (InfoLogger.class) {
                if (instance == null) {
                    instance = new InfoLogger();
                }
            }
        }
        return instance;
    }

    /**
     * 静态便捷方法 - 记录INFO级别日志
     *
     * @param message 日志消息
     * @param args 参数数组
     */
    public static void info(String message, Object... args) {
        getInstance().log(message, args);
    }

    /**
     * 记录INFO级别日志
     *
     * @param message 日志消息
     * @param args 参数数组
     */
    @Override
    public void log(String message, Object... args) {
        if (validateMessage(message)) {
            return;
        }

        // 直接使用SequentialLogger，自动保证顺序性
        if (sequentialLogger.isInfoEnabled()) {
            sequentialLogger.info(message, args);
        }
    }

    /**
     * 检查INFO级别是否启用
     *
     * @return 如果INFO级别启用则返回true
     */
    public boolean isInfoEnabled() {
        return sequentialLogger.isInfoEnabled();
    }
}
