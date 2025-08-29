package com.david.log.commons.core.operations.impl;

import com.david.log.commons.core.context.LogContext;
import com.david.log.commons.core.context.LogType;
import com.david.log.commons.core.executor.LogOperationExecutor;
import com.david.log.commons.core.operations.LogLevel;
import com.david.log.commons.core.operations.PerformanceLogOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 性能日志操作实现
 * 
 * <p>
 * 实现性能监控相关的日志记录功能，包括方法执行时间、
 * SQL性能、HTTP请求性能、内存使用和QPS统计。
 * 
 * @author David
 */
@Slf4j
@Component
public class PerformanceLogOperationsImpl extends BaseLogOperationsImpl implements PerformanceLogOperations {

    private static final String MODULE_NAME = "PERFORMANCE";

    public PerformanceLogOperationsImpl(LogOperationExecutor executor) {
        super(executor, MODULE_NAME, LogType.PERFORMANCE);
    }

    @Override
    public void timing(String methodName, long duration, Object... args) {
        LogLevel level = duration > 1000 ? LogLevel.WARN : LogLevel.DEBUG;

        LogContext context = LogContext.builder()
                .module(MODULE_NAME)
                .logType(LogType.PERFORMANCE)
                .operation("TIMING")
                .level(level)
                .message("方法执行时间 - 方法:{}, 耗时:{}ms")
                .args(new Object[] { methodName, duration })
                .build()
                .withMetadata("method_name", methodName)
                .withMetadata("duration_ms", duration)
                .withMetadata("method_args", args);

        executeLog(context);
    }

    @Override
    public void sql(String sql, long duration, int rowCount) {
        LogLevel level = duration > 5000 ? LogLevel.WARN : LogLevel.DEBUG;

        LogContext context = LogContext.builder()
                .module(MODULE_NAME)
                .logType(LogType.PERFORMANCE)
                .operation("SQL")
                .level(level)
                .message("SQL执行性能 - 耗时:{}ms, 影响行数:{}")
                .args(new Object[] { duration, rowCount })
                .build()
                .withMetadata("sql", sql)
                .withMetadata("duration_ms", duration)
                .withMetadata("row_count", rowCount);

        executeLog(context);
    }

    @Override
    public void http(String method, String url, int status, long duration) {
        LogLevel level = status >= 400 || duration > 3000 ? LogLevel.WARN : LogLevel.DEBUG;

        LogContext context = LogContext.builder()
                .module(MODULE_NAME)
                .logType(LogType.PERFORMANCE)
                .operation("HTTP")
                .level(level)
                .message("HTTP请求性能 - {} {}, 状态:{}, 耗时:{}ms")
                .args(new Object[] { method, url, status, duration })
                .build()
                .withMetadata("http_method", method)
                .withMetadata("url", url)
                .withMetadata("status_code", status)
                .withMetadata("duration_ms", duration);

        executeLog(context);
    }

    @Override
    public void memory(String component, long usedMemory, long totalMemory) {
        double usageRate = (double) usedMemory / totalMemory;
        LogLevel level = usageRate > 0.8 ? LogLevel.WARN : LogLevel.DEBUG;

        LogContext context = LogContext.builder()
                .module(MODULE_NAME)
                .logType(LogType.PERFORMANCE)
                .operation("MEMORY")
                .level(level)
                .message("内存使用统计 - 组件:{}, 已用:{}MB, 总计:{}MB, 使用率:{}")
                .args(new Object[] { component, usedMemory, totalMemory, String.format("%.2f%%", usageRate * 100) })
                .build()
                .withMetadata("component", component)
                .withMetadata("used_memory_mb", usedMemory)
                .withMetadata("total_memory_mb", totalMemory)
                .withMetadata("usage_rate", usageRate);

        executeLog(context);
    }

    @Override
    public void qps(String endpoint, double qps, double avgResponseTime) {
        LogLevel level = avgResponseTime > 1000 ? LogLevel.WARN : LogLevel.INFO;

        LogContext context = LogContext.builder()
                .module(MODULE_NAME)
                .logType(LogType.PERFORMANCE)
                .operation("QPS")
                .level(level)
                .message("QPS统计 - 端点:{}, QPS:{}, 平均响应时间:{}ms")
                .args(new Object[] { endpoint, String.format("%.2f", qps), String.format("%.2f", avgResponseTime) })
                .build()
                .withMetadata("endpoint", endpoint)
                .withMetadata("qps", qps)
                .withMetadata("avg_response_time_ms", avgResponseTime);

        executeLog(context);
    }
}
