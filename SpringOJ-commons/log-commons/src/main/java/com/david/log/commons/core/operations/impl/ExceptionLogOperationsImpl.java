package com.david.log.commons.core.operations.impl;

import com.david.log.commons.core.context.LogContext;
import com.david.log.commons.core.context.LogType;
import com.david.log.commons.core.executor.LogOperationExecutor;
import com.david.log.commons.core.operations.ExceptionLogOperations;
import com.david.log.commons.core.operations.LogLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 异常日志操作实现
 * 
 * <p>
 * 实现异常相关的日志记录功能，包括业务异常、系统异常、
 * 网络异常、数据库异常和验证异常记录。
 * 
 * @author David
 */
@Slf4j
@Component
public class ExceptionLogOperationsImpl extends BaseLogOperationsImpl implements ExceptionLogOperations {

    private static final String MODULE_NAME = "EXCEPTION";

    public ExceptionLogOperationsImpl(LogOperationExecutor executor) {
        super(executor, MODULE_NAME, LogType.EXCEPTION);
    }

    @Override
    public void business(String operation, Throwable exception, Object... context) {
        LogContext logContext = LogContext.builder()
                .module(MODULE_NAME)
                .logType(LogType.EXCEPTION)
                .operation("BUSINESS_ERROR")
                .level(LogLevel.ERROR)
                .message("业务异常 - 操作:{}, 异常:{}")
                .args(new Object[] { operation, exception.getMessage() })
                .throwable(exception)
                .build()
                .withMetadata("operation_name", operation)
                .withMetadata("exception_type", exception.getClass().getSimpleName())
                .withMetadata("business_context", context);

        executeLog(logContext);
    }

    @Override
    public void system(String component, Throwable exception, String severity) {
        LogLevel level = "CRITICAL".equalsIgnoreCase(severity) ? LogLevel.ERROR : LogLevel.WARN;

        LogContext context = LogContext.builder()
                .module(MODULE_NAME)
                .logType(LogType.EXCEPTION)
                .operation("SYSTEM_ERROR")
                .level(level)
                .message("系统异常 - 组件:{}, 严重程度:{}, 异常:{}")
                .args(new Object[] { component, severity, exception.getMessage() })
                .throwable(exception)
                .build()
                .withMetadata("component_name", component)
                .withMetadata("severity", severity)
                .withMetadata("exception_type", exception.getClass().getSimpleName());

        executeLog(context);
    }

    @Override
    public void network(String endpoint, Throwable exception, int retryCount) {
        LogLevel level = retryCount >= 3 ? LogLevel.ERROR : LogLevel.WARN;

        LogContext context = LogContext.builder()
                .module(MODULE_NAME)
                .logType(LogType.EXCEPTION)
                .operation("NETWORK_ERROR")
                .level(level)
                .message("网络异常 - 端点:{}, 重试次数:{}, 异常:{}")
                .args(new Object[] { endpoint, retryCount, exception.getMessage() })
                .throwable(exception)
                .build()
                .withMetadata("endpoint", endpoint)
                .withMetadata("retry_count", retryCount)
                .withMetadata("exception_type", exception.getClass().getSimpleName());

        executeLog(context);
    }

    @Override
    public void database(String sql, Throwable exception, Object... parameters) {
        LogContext context = LogContext.builder()
                .module(MODULE_NAME)
                .logType(LogType.EXCEPTION)
                .operation("DATABASE_ERROR")
                .level(LogLevel.ERROR)
                .message("数据库异常 - SQL:{}, 异常:{}")
                .args(new Object[] { sql, exception.getMessage() })
                .throwable(exception)
                .build()
                .withMetadata("sql_statement", sql)
                .withMetadata("sql_parameters", parameters)
                .withMetadata("exception_type", exception.getClass().getSimpleName());

        executeLog(context);
    }

    @Override
    public void validation(String field, Object value, String rule, String message) {
        LogContext context = LogContext.builder()
                .module(MODULE_NAME)
                .logType(LogType.EXCEPTION)
                .operation("VALIDATION_ERROR")
                .level(LogLevel.WARN)
                .message("验证异常 - 字段:{}, 规则:{}, 错误:{}")
                .args(new Object[] { field, rule, message })
                .build()
                .withMetadata("field_name", field)
                .withMetadata("field_value", value)
                .withMetadata("validation_rule", rule)
                .withMetadata("error_message", message);

        executeLog(context);
    }
}
