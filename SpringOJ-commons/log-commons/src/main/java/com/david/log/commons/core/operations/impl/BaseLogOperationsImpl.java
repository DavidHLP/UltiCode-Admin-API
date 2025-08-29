package com.david.log.commons.core.operations.impl;

import com.david.log.commons.core.context.LogContext;
import com.david.log.commons.core.context.LogType;
import com.david.log.commons.core.executor.LogOperationExecutor;
import com.david.log.commons.core.operations.LogLevel;
import com.david.log.commons.core.operations.LogOperations;
import lombok.extern.slf4j.Slf4j;

/**
 * 日志操作基础实现类
 * 
 * <p>
 * 提供日志操作的通用实现，包括基础的日志记录方法
 * 和日志级别检查功能。所有具体的日志操作实现类继承此类。
 * 
 * @author David
 */
@Slf4j
public abstract class BaseLogOperationsImpl implements LogOperations {

    protected final LogOperationExecutor executor;
    protected final String moduleName;
    protected final LogType logType;

    protected BaseLogOperationsImpl(LogOperationExecutor executor, String moduleName, LogType logType) {
        this.executor = executor;
        this.moduleName = moduleName;
        this.logType = logType;
    }

    @Override
    public void info(String message, Object... args) {
        LogContext context = createLogContext(LogLevel.INFO, "INFO", message, args, null);
        executeLog(context);
    }

    @Override
    public void debug(String message, Object... args) {
        LogContext context = createLogContext(LogLevel.DEBUG, "DEBUG", message, args, null);
        executeLog(context);
    }

    @Override
    public void warn(String message, Object... args) {
        LogContext context = createLogContext(LogLevel.WARN, "WARN", message, args, null);
        executeLog(context);
    }

    @Override
    public void error(String message, Throwable throwable, Object... args) {
        LogContext context = createLogContext(LogLevel.ERROR, "ERROR", message, args, throwable);
        executeLog(context);
    }

    @Override
    public void error(String message, Object... args) {
        LogContext context = createLogContext(LogLevel.ERROR, "ERROR", message, args, null);
        executeLog(context);
    }

    @Override
    public boolean isEnabled(LogLevel level) {
        // 简单实现，实际可以根据配置动态调整
        return level.getLevel() >= LogLevel.DEBUG.getLevel();
    }

    /**
     * 创建日志上下文
     */
    protected LogContext createLogContext(LogLevel level, String operation,
            String message, Object[] args, Throwable throwable) {
        return LogContext.builder()
                .module(moduleName)
                .logType(logType)
                .operation(operation)
                .level(level)
                .message(message)
                .args(args)
                .throwable(throwable)
                .async(shouldUseAsync(level))
                .build();
    }

    /**
     * 执行日志记录
     */
    protected void executeLog(LogContext context) {
        try {
            executor.execute(context);
        } catch (Exception e) {
            // 记录执行失败，但不向上抛出异常
            log.error("执行日志记录失败 - 模块:{}, 操作:{}", moduleName, context.getOperation(), e);
        }
    }

    /**
     * 判断是否应该使用异步处理
     */
    protected boolean shouldUseAsync(LogLevel level) {
        // ERROR级别使用同步处理，确保及时输出
        // 其他级别使用异步处理，提升性能
        return level != LogLevel.ERROR;
    }
}
