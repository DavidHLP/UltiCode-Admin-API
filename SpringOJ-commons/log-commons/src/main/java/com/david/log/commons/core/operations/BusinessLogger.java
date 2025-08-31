package com.david.log.commons.core.operations;

import com.david.log.commons.core.enums.LogLevel;
import com.david.log.commons.core.operations.support.BaseLogger;

/**
 * 业务日志实现类
 *
 * <p>专门用于处理业务级别的日志，支持自动追加类名和方法名
 *
 * @author David
 * @version 1.0
 * @since 2024-08-31
 */
public final class BusinessLogger extends BaseLogger {

    /** 单例实例 */
    private static volatile BusinessLogger instance;

    /** 私有构造函数 */
    private BusinessLogger() {
        super("com.david.log.commons.BUSINESS", LogLevel.BUSINESS);
    }

    /**
     * 获取单例实例
     *
     * @return BusinessLogger实例
     */
    public static BusinessLogger getInstance() {
        if (instance == null) {
            synchronized (BusinessLogger.class) {
                if (instance == null) {
                    instance = new BusinessLogger();
                }
            }
        }
        return instance;
    }

    /**
     * 静态便捷方法 - 记录业务日志
     *
     * @param message 日志消息
     * @param args 参数数组
     */
    public static void business(String message, Object... args) {
        getInstance().log(message, args);
    }

    /**
     * 静态便捷方法 - 记录业务日志（指定级别）
     *
     * @param message 日志消息
     * @param logMethod 输出方法
     * @param args 参数数组
     */
    public static void business(String message, LogMethod logMethod, Object... args) {
        getInstance().log(message, logMethod, args);
    }

    /**
     * 记录业务日志
     *
     * @param message 日志消息
     * @param args 参数数组
     */
    @Override
    public void log(String message, Object... args) {
        log(message, LogMethod.INFO, args);
    }

    /**
     * 记录业务日志（指定输出级别）
     *
     * @param message 日志消息
     * @param logMethod 输出方法
     * @param args 参数数组
     */
    public void log(String message, LogMethod logMethod, Object... args) {
        if (validateMessage(message)) {
            return;
        }

        // 直接使用SequentialLogger，自动保证顺序性
        switch (logMethod) {
            case DEBUG:
                if (sequentialLogger.isDebugEnabled()) {
                    sequentialLogger.debug("[BUSINESS] " + message, args);
                }
                break;
            case WARN:
                if (sequentialLogger.isWarnEnabled()) {
                    sequentialLogger.warn("[BUSINESS] " + message, args);
                }
                break;
            case ERROR:
                if (sequentialLogger.isErrorEnabled()) {
                    sequentialLogger.error("[BUSINESS] " + message, args);
                }
                break;
            default:
                if (sequentialLogger.isInfoEnabled()) {
                    sequentialLogger.info("[BUSINESS] " + message, args);
                }
                break;
        }
    }

    /** 日志输出方法枚举 */
    public enum LogMethod {
        DEBUG,
        INFO,
        WARN,
        ERROR
    }
}
