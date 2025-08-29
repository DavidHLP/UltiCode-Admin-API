package com.david.log.commons.core.formatter;

import com.david.log.commons.core.context.LogContext;

/**
 * 日志格式化器接口
 * 
 * <p>
 * 定义日志格式化的统一接口，支持多种输出格式。
 * 负责将日志上下文信息转换为最终的日志字符串。
 * 
 * @author David
 */
public interface LogFormatter {

    /**
     * 格式化日志消息
     * 
     * @param context 日志上下文
     * @return 格式化后的日志字符串
     */
    String format(LogContext context);

    /**
     * 格式化参数数组
     * 
     * @param args 参数数组
     * @return 格式化后的参数字符串
     */
    String formatArgs(Object[] args);

    /**
     * 格式化单个对象
     * 
     * @param obj 对象
     * @return 格式化后的字符串
     */
    String formatObject(Object obj);

    /**
     * 获取格式化器类型
     * 
     * @return 格式化器类型
     */
    FormatterType getType();
}
