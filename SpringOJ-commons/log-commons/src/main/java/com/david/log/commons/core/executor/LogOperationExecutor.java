package com.david.log.commons.core.executor;

import com.david.log.commons.core.context.LogContext;
import com.david.log.commons.core.processor.LogProcessor;
import com.david.log.commons.core.processor.ProcessorType;
import com.david.log.commons.metrics.LogMetricsCollector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 日志操作执行器
 * 
 * <p>
 * 统一的日志操作执行入口，负责协调各种处理器完成日志记录。
 * 支持同步和异步执行模式，集成异常处理和指标收集。
 * 
 * @author David
 */
@Slf4j
@Component
public class LogOperationExecutor {

    private final List<LogProcessor> processors;
    private final LogMetricsCollector metricsCollector;

    public LogOperationExecutor(List<LogProcessor> processors,
            LogMetricsCollector metricsCollector) {
        this.processors = processors;
        this.metricsCollector = metricsCollector;
    }

    /**
     * 执行日志记录操作
     *
     * @param context 日志上下文
     * @return 执行结果
     */
    public Object execute(LogContext context) {
        long startTime = System.nanoTime();

        try {
            // 选择合适的处理器
            LogProcessor processor = selectProcessor(context);

            // 执行日志处理
            Object result = processor.process(context);

            // 记录成功指标
            recordSuccessMetrics(context, startTime);

            return result;

        } catch (Exception e) {
            // 记录异常指标
            recordErrorMetrics(context, e, startTime);

            // 异常处理 - 不向上抛出，避免影响业务流程
            handleException(context, e);

            return null;
        }
    }

    /**
     * 异步执行日志记录操作
     * 
     * @param context 日志上下文
     * @return CompletableFuture
     */
    public CompletableFuture<Void> executeAsync(LogContext context) {
        return CompletableFuture.runAsync(() -> {
            LogContext asyncContext = context.withAsync(true);
            execute(asyncContext);
        });
    }

    /**
     * 选择合适的处理器
     */
    private LogProcessor selectProcessor(LogContext context) {
        ProcessorType targetType = context.isAsync() ? ProcessorType.ASYNC : ProcessorType.SYNC;

        return processors.stream()
                .filter(p -> p.getType() == targetType)
                .findFirst()
                .orElseGet(() -> {
                    // 降级策略：如果找不到目标处理器，使用同步处理器
                    log.warn("未找到{}处理器，降级使用同步处理器", targetType);
                    return processors.stream()
                            .filter(p -> p.getType() == ProcessorType.SYNC)
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("未找到可用的日志处理器"));
                });
    }

    /**
     * 处理异常
     */
    private void handleException(LogContext context, Exception e) {
        try {
            // 使用最简单的方式记录错误日志，避免循环错误
            log.error("日志操作执行失败 - 操作:{}, 模块:{}, 错误:{}",
                    context.getOperation(),
                    context.getModule(),
                    e.getMessage(), e);
        } catch (Exception logError) {
            // 如果连基本日志都无法记录，只能使用System.err
            System.err.println("严重错误：无法记录日志操作异常 - " + e.getMessage());
            logError.printStackTrace();
        }
    }

    /**
     * 记录成功指标
     */
    private void recordSuccessMetrics(LogContext context, long startTime) {
        try {
            long duration = System.nanoTime() - startTime;
            metricsCollector.recordSuccess(context, duration);
        } catch (Exception e) {
            log.warn("记录成功指标失败: {}", e.getMessage());
        }
    }

    /**
     * 记录异常指标
     */
    private void recordErrorMetrics(LogContext context, Exception error, long startTime) {
        try {
            long duration = System.nanoTime() - startTime;
            metricsCollector.recordError(context, error, duration);
        } catch (Exception e) {
            log.warn("记录异常指标失败: {}", e.getMessage());
        }
    }
}
