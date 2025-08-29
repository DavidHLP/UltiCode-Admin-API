package com.david.log.commons.core.operations.impl;

import com.david.log.commons.core.context.LogContext;
import com.david.log.commons.core.context.LogType;
import com.david.log.commons.core.executor.LogOperationExecutor;
import com.david.log.commons.core.operations.BusinessLogOperations;
import com.david.log.commons.core.operations.LogLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 业务日志操作实现
 * 
 * <p>
 * 实现业务相关的日志记录功能，包括用户操作审计、
 * 业务流程追踪、业务事件和指标记录。
 * 
 * @author David
 */
@Slf4j
@Component
public class BusinessLogOperationsImpl extends BaseLogOperationsImpl implements BusinessLogOperations {

    private static final String MODULE_NAME = "BUSINESS";

    public BusinessLogOperationsImpl(LogOperationExecutor executor) {
        super(executor, MODULE_NAME, LogType.BUSINESS);
    }

    @Override
    public void audit(String userId, String operation, String result, Object... details) {
        LogContext context = LogContext.builder()
                .module(MODULE_NAME)
                .logType(LogType.BUSINESS)
                .operation("AUDIT")
                .userId(userId)
                .level(LogLevel.INFO)
                .message("用户操作审计 - 操作:{}, 结果:{}")
                .args(new Object[] { operation, result })
                .build()
                .withMetadata("operation_type", operation)
                .withMetadata("result", result)
                .withMetadata("details", details);

        executeLog(context);
    }

    @Override
    public void trace(String processId, String step, String status, Object... data) {
        LogContext context = LogContext.builder()
                .module(MODULE_NAME)
                .logType(LogType.BUSINESS)
                .operation("TRACE")
                .level(LogLevel.DEBUG)
                .message("业务流程追踪 - 流程:{}, 步骤:{}, 状态:{}")
                .args(new Object[] { processId, step, status })
                .build()
                .withMetadata("process_id", processId)
                .withMetadata("step", step)
                .withMetadata("status", status)
                .withMetadata("data", data);

        executeLog(context);
    }

    @Override
    public void event(String eventType, Object... eventData) {
        LogContext context = LogContext.builder()
                .module(MODULE_NAME)
                .logType(LogType.BUSINESS)
                .operation("EVENT")
                .level(LogLevel.INFO)
                .message("业务事件记录 - 事件类型:{}")
                .args(new Object[] { eventType })
                .build()
                .withMetadata("event_type", eventType)
                .withMetadata("event_data", eventData);

        executeLog(context);
    }

    @Override
    public void metric(String metric, Object value, Object... tags) {
        LogContext context = LogContext.builder()
                .module(MODULE_NAME)
                .logType(LogType.BUSINESS)
                .operation("METRIC")
                .level(LogLevel.INFO)
                .message("业务指标记录 - 指标:{}, 值:{}")
                .args(new Object[] { metric, value })
                .build()
                .withMetadata("metric_name", metric)
                .withMetadata("metric_value", value)
                .withMetadata("tags", tags);

        executeLog(context);
    }
}
