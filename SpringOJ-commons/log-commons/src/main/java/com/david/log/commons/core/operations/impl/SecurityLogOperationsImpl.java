package com.david.log.commons.core.operations.impl;

import com.david.log.commons.core.context.LogContext;
import com.david.log.commons.core.context.LogType;
import com.david.log.commons.core.executor.LogOperationExecutor;
import com.david.log.commons.core.operations.LogLevel;
import com.david.log.commons.core.operations.SecurityLogOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 安全日志操作实现
 * 
 * <p>
 * 实现安全相关的日志记录功能，包括用户登录登出、
 * 权限检查、安全威胁和数据访问记录。
 * 
 * @author David
 */
@Slf4j
@Component
public class SecurityLogOperationsImpl extends BaseLogOperationsImpl implements SecurityLogOperations {

    private static final String MODULE_NAME = "SECURITY";

    public SecurityLogOperationsImpl(LogOperationExecutor executor) {
        super(executor, MODULE_NAME, LogType.SECURITY);
    }

    @Override
    public void login(String userId, String loginType, boolean success, String ipAddress, String userAgent) {
        LogLevel level = success ? LogLevel.INFO : LogLevel.WARN;
        String result = success ? "成功" : "失败";

        LogContext context = LogContext.builder()
                .module(MODULE_NAME)
                .logType(LogType.SECURITY)
                .operation("LOGIN")
                .userId(userId)
                .level(level)
                .message("用户登录 - 用户:{}, 类型:{}, 结果:{}, IP:{}")
                .args(new Object[] { userId, loginType, result, ipAddress })
                .build()
                .withMetadata("login_type", loginType)
                .withMetadata("success", success)
                .withMetadata("ip_address", ipAddress)
                .withMetadata("user_agent", userAgent);

        executeLog(context);
    }

    @Override
    public void logout(String userId, String sessionId, String reason) {
        LogContext context = LogContext.builder()
                .module(MODULE_NAME)
                .logType(LogType.SECURITY)
                .operation("LOGOUT")
                .userId(userId)
                .sessionId(sessionId)
                .level(LogLevel.INFO)
                .message("用户登出 - 用户:{}, 会话:{}, 原因:{}")
                .args(new Object[] { userId, sessionId, reason })
                .build()
                .withMetadata("logout_reason", reason);

        executeLog(context);
    }

    @Override
    public void permission(String userId, String resource, String operation, boolean granted) {
        LogLevel level = granted ? LogLevel.DEBUG : LogLevel.WARN;
        String result = granted ? "授权" : "拒绝";

        LogContext context = LogContext.builder()
                .module(MODULE_NAME)
                .logType(LogType.SECURITY)
                .operation("PERMISSION")
                .userId(userId)
                .level(level)
                .message("权限检查 - 用户:{}, 资源:{}, 操作:{}, 结果:{}")
                .args(new Object[] { userId, resource, operation, result })
                .build()
                .withMetadata("resource", resource)
                .withMetadata("operation_type", operation)
                .withMetadata("granted", granted);

        executeLog(context);
    }

    @Override
    public void threat(String threatType, String severity, String description, String source) {
        LogLevel level = "HIGH".equalsIgnoreCase(severity) ? LogLevel.ERROR : LogLevel.WARN;

        LogContext context = LogContext.builder()
                .module(MODULE_NAME)
                .logType(LogType.SECURITY)
                .operation("THREAT")
                .level(level)
                .message("安全威胁 - 类型:{}, 严重程度:{}, 描述:{}, 来源:{}")
                .args(new Object[] { threatType, severity, description, source })
                .build()
                .withMetadata("threat_type", threatType)
                .withMetadata("severity", severity)
                .withMetadata("description", description)
                .withMetadata("source", source);

        executeLog(context);
    }

    @Override
    public void dataAccess(String userId, String dataType, String action, int recordCount) {
        LogLevel level = "DELETE".equalsIgnoreCase(action) || recordCount > 1000 ? LogLevel.WARN : LogLevel.INFO;

        LogContext context = LogContext.builder()
                .module(MODULE_NAME)
                .logType(LogType.SECURITY)
                .operation("DATA_ACCESS")
                .userId(userId)
                .level(level)
                .message("数据访问 - 用户:{}, 数据类型:{}, 操作:{}, 记录数:{}")
                .args(new Object[] { userId, dataType, action, recordCount })
                .build()
                .withMetadata("data_type", dataType)
                .withMetadata("action", action)
                .withMetadata("record_count", recordCount);

        executeLog(context);
    }
}
