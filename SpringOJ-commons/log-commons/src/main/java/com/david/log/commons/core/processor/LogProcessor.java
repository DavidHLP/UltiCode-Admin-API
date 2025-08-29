package com.david.log.commons.core.processor;

import com.david.log.commons.core.context.LogContext;

/**
 * 日志处理器接口
 * 
 * <p>
 * 定义日志处理的统一接口，支持同步和异步处理模式。
 * 实现类负责将日志上下文信息转换为实际的日志输出。
 * 
 * @author David
 */
public interface LogProcessor {

    /**
     * 处理日志记录
     * 
     * @param context 日志上下文
     * @return 处理结果，同步模式返回具体结果，异步模式返回Future
     */
    Object process(LogContext context);

    /**
     * 判断是否支持异步处理
     * 
     * @return 是否支持异步
     */
    boolean supportsAsync();

    /**
     * 获取处理器类型
     * 
     * @return 处理器类型
     */
    ProcessorType getType();
}
