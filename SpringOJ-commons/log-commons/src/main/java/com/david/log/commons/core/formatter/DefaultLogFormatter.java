package com.david.log.commons.core.formatter;

import com.david.log.commons.core.context.LogContext;
import com.david.log.commons.core.masker.SensitiveDataMasker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collection;

/**
 * 默认日志格式化器实现
 * 
 * <p>
 * 提供标准的日志格式化功能，支持参数截断、集合大小显示、
 * 敏感信息脱敏等特性。输出格式清晰易读。
 * 
 * @author David
 */
@Slf4j
@Component
public class DefaultLogFormatter implements LogFormatter {

    private static final int MAX_PARAM_LENGTH = 100;
    private static final String TRUNCATE_SUFFIX = "...";

    private final SensitiveDataMasker sensitiveDataMasker;

    public DefaultLogFormatter(SensitiveDataMasker sensitiveDataMasker) {
        this.sensitiveDataMasker = sensitiveDataMasker;
    }

    @Override
    public String format(LogContext context) {
        StringBuilder sb = new StringBuilder();

        // 添加模块信息
        if (StringUtils.hasText(context.getModule())) {
            sb.append("[").append(context.getModule()).append("] ");
        }

        // 添加操作信息
        if (StringUtils.hasText(context.getOperation())) {
            sb.append("[").append(context.getOperation()).append("] ");
        }

        // 添加用户信息
        if (StringUtils.hasText(context.getUserId())) {
            sb.append("[User:").append(context.getUserId()).append("] ");
        }

        // 添加链路追踪信息
        if (StringUtils.hasText(context.getTraceId())) {
            sb.append("[Trace:").append(context.getTraceId()).append("] ");
        }

        // 添加主要消息（支持占位符替换）
        String formattedMessage = formatMessage(context.getMessage(), context.getArgs());
        sb.append(formattedMessage);

        // 如果还有未使用的参数，额外输出
        int placeholderCount = countPlaceholders(context.getMessage());
        Object[] remainingArgs = getRemainingArgs(context.getArgs(), placeholderCount);
        if (remainingArgs != null && remainingArgs.length > 0) {
            String argsStr = formatArgs(remainingArgs);
            if (StringUtils.hasText(argsStr)) {
                sb.append(" - ").append(argsStr);
            }
        }

        // 添加扩展元数据
        if (!context.getMetadata().isEmpty()) {
            sb.append(" [Metadata:").append(formatMetadata(context)).append("]");
        }

        return sb.toString();
    }

    @Override
    public String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Args[");

        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(formatObject(args[i]));
        }

        sb.append("]");
        return sb.toString();
    }

    @Override
    public String formatObject(Object obj) {
        if (obj == null) {
            return "null";
        }

        // 集合类型显示大小信息
        if (obj instanceof Collection<?> collection) {
            return String.format("%s(size=%d)",
                    obj.getClass().getSimpleName(), collection.size());
        }

        // 数组类型显示长度信息
        if (obj.getClass().isArray()) {
            if (obj instanceof Object[]) {
                return String.format("Array(length=%d)", ((Object[]) obj).length);
            } else {
                return String.format("Array(length=%d)",
                        java.lang.reflect.Array.getLength(obj));
            }
        }

        // 敏感数据脱敏处理
        String result = sensitiveDataMasker.mask(obj);

        // 长度截断
        if (result.length() > MAX_PARAM_LENGTH) {
            return result.substring(0, MAX_PARAM_LENGTH) + TRUNCATE_SUFFIX;
        }

        return result;
    }

    @Override
    public FormatterType getType() {
        return FormatterType.DEFAULT;
    }

    /**
     * 格式化消息，支持SLF4J风格的占位符替换
     */
    private String formatMessage(String message, Object[] args) {
        if (message == null) {
            return "";
        }

        if (args == null || args.length == 0) {
            return message;
        }

        StringBuilder result = new StringBuilder();
        int messageIndex = 0;
        int argIndex = 0;

        while (messageIndex < message.length() && argIndex < args.length) {
            int placeholderIndex = message.indexOf("{}", messageIndex);
            if (placeholderIndex == -1) {
                // 没有更多占位符，添加剩余的消息
                result.append(message.substring(messageIndex));
                break;
            }

            // 添加占位符前的内容
            result.append(message.substring(messageIndex, placeholderIndex));

            // 替换占位符
            result.append(formatObject(args[argIndex]));

            messageIndex = placeholderIndex + 2; // 跳过 "{}"
            argIndex++;
        }

        // 添加剩余的消息内容
        if (messageIndex < message.length()) {
            result.append(message.substring(messageIndex));
        }

        return result.toString();
    }

    /**
     * 统计消息中占位符的数量
     */
    private int countPlaceholders(String message) {
        if (message == null) {
            return 0;
        }

        int count = 0;
        int index = 0;
        while ((index = message.indexOf("{}", index)) != -1) {
            count++;
            index += 2;
        }
        return count;
    }

    /**
     * 获取未被占位符使用的剩余参数
     */
    private Object[] getRemainingArgs(Object[] args, int usedCount) {
        if (args == null || args.length <= usedCount) {
            return null;
        }

        Object[] remaining = new Object[args.length - usedCount];
        System.arraycopy(args, usedCount, remaining, 0, remaining.length);
        return remaining;
    }

    /**
     * 格式化元数据信息
     */
    private String formatMetadata(LogContext context) {
        StringBuilder sb = new StringBuilder();
        context.getMetadata().forEach((key, value) -> {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(key).append("=").append(formatObject(value));
        });
        return sb.toString();
    }
}
