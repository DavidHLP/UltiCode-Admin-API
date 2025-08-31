package com.david.log.commons.core.operations.support;

import com.david.log.commons.core.enums.LogLevel;
import com.david.log.commons.core.sequence.SequentialLogger;
import com.david.log.commons.core.sequence.SequentialLoggerFactory;

import lombok.Getter;

/**
 * 基础日志处理抽象类
 * 
 * <p>
 * 基于@Slf4j的简化版本，使用SequentialLogger确保日志顺序性
 * </p>
 * 
 * @author David
 * @version 1.0
 * @since 2024-08-31
 */
public abstract class BaseLogger {

    /**
     * 序列化日志器
     */
    protected final SequentialLogger sequentialLogger;

    /**
     * 当前日志级别
     * -- GETTER --
     * 获取当前日志级别
     */
    @Getter
    protected final LogLevel level;

    /**
     * 构造函数
     * 
     * @param loggerName 日志器名称
     * @param level      日志级别
     */
    protected BaseLogger(String loggerName, LogLevel level) {
        this.sequentialLogger = SequentialLoggerFactory.getLogger(loggerName);
        this.level = level;
    }

    /**
     * 记录日志的抽象方法
     *
     * @param message 日志消息
     * @param args    参数数组
     */
    public abstract void log(String message, Object... args);

	/**
     * 验证消息参数
     * 
     * @param message 消息内容
     * @return 验证通过返回true
     */
    protected boolean validateMessage(String message) {
        return message == null || message.trim().isEmpty();
    }
}
