package com.david.log.commons.core.sequence;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.MDC;

/**
 * 序列化日志包装器
 * 
 * <p>
 * 基于@Slf4j实现的日志顺序性保证包装器，确保日志的严格时序关系
 * </p>
 * 
 * @author David
 * @version 1.0
 * @since 2024-08-31
 * @param wrappedLogger
被包装的SLF4J Logger
 */
@Slf4j
public record SequentialLogger(Logger wrappedLogger) {

    /**
     * MDC中序列号的键名
     */
    private static final String MDC_SEQUENCE_KEY = "logSequence";

    /**
     * MDC中扩展时间戳的键名
     */
    private static final String MDC_EXTENDED_TIMESTAMP_KEY = "extendedTimestamp";

    /**
     * MDC中线程信息的键名
     */
    private static final String MDC_THREAD_INFO_KEY = "threadInfo";

	/**
	 * 构造函数
	 *
	 * @param wrappedLogger 要包装的SLF4J Logger
	 */
	public SequentialLogger {
	}

    /**
     * 记录DEBUG级别日志
     */
    public void debug(String message, Object... args) {
        if (wrappedLogger.isDebugEnabled()) {
            logWithSequence(() -> wrappedLogger.debug(message, args));
        }
    }

    /**
     * 记录INFO级别日志
     */
    public void info(String message, Object... args) {
        if (wrappedLogger.isInfoEnabled()) {
            logWithSequence(() -> wrappedLogger.info(message, args));
        }
    }

    /**
     * 记录WARN级别日志
     */
    public void warn(String message, Object... args) {
        if (wrappedLogger.isWarnEnabled()) {
            logWithSequence(() -> wrappedLogger.warn(message, args));
        }
    }

    /**
     * 记录ERROR级别日志
     */
    public void error(String message, Object... args) {
        if (wrappedLogger.isErrorEnabled()) {
            logWithSequence(() -> wrappedLogger.error(message, args));
        }
    }

    /**
     * 记录ERROR级别日志（带异常）
     */
    public void error(String message, Throwable throwable) {
        if (wrappedLogger.isErrorEnabled()) {
            logWithSequence(() -> wrappedLogger.error(message, throwable));
        }
    }

    /**
     * 记录ERROR级别日志（带异常和参数）
     */
    public void error(String message, Throwable throwable, Object... args) {
        if (wrappedLogger.isErrorEnabled()) {
            // SLF4J的error方法不支持throwable + args的组合，需要格式化消息
            String formattedMessage = formatMessage(message, args);
            logWithSequence(() -> wrappedLogger.error(formattedMessage, throwable));
        }
    }

    /**
     * 带序列号的日志记录
     * 
     * @param logAction 日志记录动作
     */
    private void logWithSequence(Runnable logAction) {
        // 生成序列号和扩展时间戳
        String sequence = LogSequenceGenerator.nextSequence();
        String extendedTimestamp = ThreadSafeFormatter.formatExtendedTimestamp();
        String threadInfo = getThreadInfo();

        // 设置MDC上下文
        MDC.put(MDC_SEQUENCE_KEY, sequence);
        MDC.put(MDC_EXTENDED_TIMESTAMP_KEY, extendedTimestamp);
        MDC.put(MDC_THREAD_INFO_KEY, threadInfo);

        try {
            // 执行实际的日志记录
            logAction.run();
        } finally {
            // 清理MDC上下文
            MDC.remove(MDC_SEQUENCE_KEY);
            MDC.remove(MDC_EXTENDED_TIMESTAMP_KEY);
            MDC.remove(MDC_THREAD_INFO_KEY);
        }
    }

    /**
     * 获取线程信息
     */
    private String getThreadInfo() {
        Thread currentThread = Thread.currentThread();
        return String.format("Thread-%s-%d", currentThread.getName(), currentThread.getId());
    }

    /**
     * 格式化消息（简单的参数替换）
     */
    private String formatMessage(String message, Object... args) {
        if (message == null || args == null || args.length == 0) {
            return message;
        }

        String result = message;
        for (Object arg : args) {
            result = result.replaceFirst("\\{}", String.valueOf(arg));
        }
        return result;
    }

    /**
     * 检查DEBUG级别是否启用
     */
    public boolean isDebugEnabled() {
        return wrappedLogger.isDebugEnabled();
    }

    /**
     * 检查INFO级别是否启用
     */
    public boolean isInfoEnabled() {
        return wrappedLogger.isInfoEnabled();
    }

    /**
     * 检查WARN级别是否启用
     */
    public boolean isWarnEnabled() {
        return wrappedLogger.isWarnEnabled();
    }

    /**
     * 检查ERROR级别是否启用
     */
    public boolean isErrorEnabled() {
        return wrappedLogger.isErrorEnabled();
    }

    /**
     * 获取被包装的Logger名称
     */
    public String getName() {
        return wrappedLogger.getName();
    }
}
