package com.david.log.commons.core.operations;

import com.david.log.commons.core.enums.LogLevel;
import com.david.log.commons.core.operations.support.BaseLogger;

/**
 * WARN级别日志实现类
 * 
 * <p>
 * 用于输出警告信息，表示潜在的问题或需要注意的情况
 * </p>
 * 
 * @author David
 * @version 1.0
 * @since 2024-08-31
 */
public final class WarnLogger extends BaseLogger {

    /**
     * 单例实例
     */
    private static volatile WarnLogger instance;

    /**
     * 私有构造函数
     */
    private WarnLogger() {
        super("com.david.log.commons.WARN", LogLevel.WARN);
    }

    /**
     * 获取单例实例
     * 
     * @return WarnLogger实例
     */
    public static WarnLogger getInstance() {
        if (instance == null) {
            synchronized (WarnLogger.class) {
                if (instance == null) {
                    instance = new WarnLogger();
                }
            }
        }
        return instance;
    }

    /**
     * 静态便捷方法 - 记录WARN日志
     *
     * @param message 日志消息
     * @param args    参数数组
     */
    public static void warn(String message, Object... args) {
        getInstance().log(message, args);
    }

    /**
     * 记录WARN级别日志
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
        if (sequentialLogger.isWarnEnabled()) {
            sequentialLogger.warn(message, args);
        }
    }

    /**
     * 检查WARN级别是否启用
     *
     * @return 如果WARN级别启用则返回true
     */
    public boolean isWarnEnabled() {
        return sequentialLogger.isWarnEnabled();
    }
}
