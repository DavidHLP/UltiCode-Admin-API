package com.david.redis.commons.core.operations.support;

import com.david.log.commons.core.LogUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collection;

/**
 * Redis日志助手
 *
 * <p>统一的Redis操作日志记录组件
 *
 * @author David
 */
@Component
@RequiredArgsConstructor
public class RedisLoggerHelper {

    private static final int MAX_PARAM_LENGTH = 100;
    private static final String SENSITIVE_MASK = "***";
    private final LogUtils logUtils;

    /**
     * 记录操作开始日志
     *
     * @param context 操作上下文
     */
    public void logOperationStart(OperationContext context) {
        String operation = context.operation();
        String key = context.key();
        Object[] params = context.params();

        if (params != null && params.length > 0) {
            String paramsStr = formatParams(params);
            logUtils.business()
                    .trace(
                            "redis_operation",
                            operation,
                            "start",
                            "key: " + key,
                            "params: " + paramsStr);
        } else {
            logUtils.business().trace("redis_operation", operation, "start", "key: " + key);
        }
    }

    /**
     * 记录操作结果日志
     *
     * @param context 操作上下文
     * @param result 操作结果
     */
    public void logOperationResult(OperationContext context, Object result) {
        String operation = context.operation();
        String key = context.key();
        String resultStr = formatResult(result);

        logUtils.business()
                .trace(
                        "redis_operation",
                        operation,
                        "completed",
                        "key: " + key,
                        "result: " + resultStr);
    }

    /**
     * 记录操作异常日志
     *
     * @param context 操作上下文
     * @param exception 异常信息
     */
    public void logOperationError(OperationContext context, Exception exception) {
        String operation = context.operation();
        String key = context.key();
        Object[] params = context.params();

        if (params != null && params.length > 0) {
            String paramsStr = formatParams(params);
            logUtils.exception()
                    .business(
                            "redis_operation_failed",
                            exception,
                            "operation: " + operation,
                            "key: " + key,
                            "params: " + paramsStr);
        } else {
            logUtils.exception()
                    .business(
                            "redis_operation_failed",
                            exception,
                            "operation: " + operation,
                            "key: " + key);
        }
    }

    /**
     * 格式化参数用于日志输出
     *
     * @param params 参数数组
     * @return 格式化后的参数字符串
     */
    private String formatParams(Object[] params) {
        if (params == null || params.length == 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(formatSingleParam(params[i]));
        }
        sb.append("]");

        return sb.toString();
    }

    /**
     * 格式化单个参数
     *
     * @param param 参数值
     * @return 格式化后的参数字符串
     */
    private String formatSingleParam(Object param) {
        if (param == null) {
            return "null";
        }

        String paramStr = param.toString();

        // 敏感信息脱敏
        if (isSensitiveParam(paramStr)) {
            return SENSITIVE_MASK;
        }

        // 长度截断
        if (paramStr.length() > MAX_PARAM_LENGTH) {
            return paramStr.substring(0, MAX_PARAM_LENGTH) + "...";
        }

        return paramStr;
    }

    /**
     * 格式化结果用于日志输出
     *
     * @param result 结果对象
     * @return 格式化后的结果字符串
     */
    private String formatResult(Object result) {
        if (result == null) {
            return "null";
        }

        // 集合类型显示大小
        if (result instanceof Collection<?> collection) {
            return String.format(
                    "%s(size=%d)", result.getClass().getSimpleName(), collection.size());
        }

        // 数组类型显示长度
        if (result.getClass().isArray()) {
            if (result instanceof Object[]) {
                return String.format("Array(length=%d)", ((Object[]) result).length);
            } else {
                return String.format("Array(length=%d)", java.lang.reflect.Array.getLength(result));
            }
        }

        String resultStr = result.toString();

        // 长度截断
        if (resultStr.length() > MAX_PARAM_LENGTH) {
            return resultStr.substring(0, MAX_PARAM_LENGTH) + "...";
        }

        return resultStr;
    }

    /**
     * 判断是否为敏感参数
     *
     * @param paramStr 参数字符串
     * @return 是否为敏感参数
     */
    private boolean isSensitiveParam(String paramStr) {
        if (!StringUtils.hasText(paramStr)) {
            return false;
        }

        String lowerParam = paramStr.toLowerCase();

        // 检查是否包含敏感关键词
        return lowerParam.contains("password")
                || lowerParam.contains("token")
                || lowerParam.contains("secret")
                || lowerParam.contains("key") && lowerParam.length() > 32; // 可能是密钥
    }
}
