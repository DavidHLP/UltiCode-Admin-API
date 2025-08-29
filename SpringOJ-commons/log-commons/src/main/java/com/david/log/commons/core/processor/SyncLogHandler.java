package com.david.log.commons.core.processor;

import com.david.log.commons.core.context.LogContext;
import com.david.log.commons.core.formatter.LogFormatter;
import com.david.log.commons.core.operations.LogLevel;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 同步日志处理器
 * 
 * <p>
 * 直接使用SLF4J Logger进行同步日志输出，性能开销最小，
 * 适用于对实时性要求高的日志记录场景。
 * 
 * @author David
 */
@Slf4j
@Component
public class SyncLogHandler implements LogProcessor {

    private final LogFormatter formatter;

    public SyncLogHandler(LogFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public Object process(LogContext context) {
        try {
            // 获取对应模块的Logger
            Logger logger = getLogger(context);

            // 格式化日志消息
            String formattedMessage = formatter.format(context);

            // 根据级别输出日志
            writeLog(logger, context.getLevel(), formattedMessage, context.getThrowable());

            return "SUCCESS";

        } catch (Exception e) {
            // 处理异常，不向上抛出
            log.error("同步日志处理失败 - 操作:{}, 消息:{}",
                    context.getOperation(), context.getMessage(), e);
            return "FAILED";
        }
    }

    @Override
    public boolean supportsAsync() {
        return false;
    }

    @Override
    public ProcessorType getType() {
        return ProcessorType.SYNC;
    }

    /**
     * 获取对应的Logger实例
     */
    private Logger getLogger(LogContext context) {
        String loggerName = "com.david.log.commons";

        // 如果有模块信息，使用模块作为Logger名称
        if (context.getModule() != null) {
            loggerName = loggerName + "." + context.getModule();
        }

        // 如果有日志类型，添加类型后缀
        if (context.getLogType() != null) {
            loggerName = loggerName + "." + context.getLogType().getCode().toLowerCase();
        }

        return LoggerFactory.getLogger(loggerName);
    }

    /**
     * 根据级别写入日志
     */
    private void writeLog(Logger logger, LogLevel level, String message, Throwable throwable) {
        switch (level) {
            case DEBUG:
                if (throwable != null) {
                    logger.debug(message, throwable);
                } else {
                    logger.debug(message);
                }
                break;
            case INFO:
                if (throwable != null) {
                    logger.info(message, throwable);
                } else {
                    logger.info(message);
                }
                break;
            case WARN:
                if (throwable != null) {
                    logger.warn(message, throwable);
                } else {
                    logger.warn(message);
                }
                break;
            case ERROR:
                if (throwable != null) {
                    logger.error(message, throwable);
                } else {
                    logger.error(message);
                }
                break;
            default:
                logger.info(message);
        }
    }
}
