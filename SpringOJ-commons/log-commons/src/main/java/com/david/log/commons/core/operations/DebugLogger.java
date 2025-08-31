package com.david.log.commons.core.operations;

import com.david.log.commons.core.enums.LogLevel;
import com.david.log.commons.core.operations.support.BaseLogger;

/**
 * DEBUG级别日志实现类
 * 
 * <p>
 * 用于输出调试信息，通常在开发和测试阶段使用
 * </p>
 * 
 * @author David
 * @version 1.0
 * @since 2024-08-31
 */
public final class DebugLogger extends BaseLogger {

    /**
     * 单例实例
     */
    private static volatile DebugLogger instance;

    /**
     * 私有构造函数
     */
    private DebugLogger() {
        super("com.david.log.commons.DEBUG", LogLevel.DEBUG);
    }

    /**
     * 获取单例实例
     * 
     * @return DebugLogger实例
     */
    public static DebugLogger getInstance() {
        if (instance == null) {
            synchronized (DebugLogger.class) {
                if (instance == null) {
                    instance = new DebugLogger();
                }
            }
        }
        return instance;
    }

    /**
     * 静态便捷方法 - 记录DEBUG日志
     *
     * @param message 日志消息
     * @param args    参数数组
     */
    public static void debug(String message, Object... args) {
        getInstance().log(message, args);
    }

    /**
     * 记录DEBUG级别日志
     *
     * @param message 日志消息
     * @param args    参数数组
     */
    @Override
    public void log(String message, Object... args) {
        if (validateMessage(message)) {
            return;
        }

        // 直接使用SequentialLogger，自动保证顺序性
        if (sequentialLogger.isDebugEnabled()) {
            sequentialLogger.debug(message, args);
        }
    }

    /**
     * 检查DEBUG级别是否启用
     *
     * @return 如果DEBUG级别启用则返回true
     */
    public boolean isDebugEnabled() {
        return sequentialLogger.isDebugEnabled();
    }
}
