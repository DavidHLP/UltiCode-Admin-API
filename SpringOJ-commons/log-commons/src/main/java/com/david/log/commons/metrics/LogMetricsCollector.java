package com.david.log.commons.metrics;

import com.david.log.commons.core.context.LogContext;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 日志指标收集器
 * 
 * <p>
 * 集成Micrometer框架收集日志操作的各种指标，
 * 包括执行次数、执行时间、成功率、错误率等。
 * 
 * @author David
 */
@Slf4j
@Component
@ConditionalOnClass(MeterRegistry.class)
@ConditionalOnBean(MeterRegistry.class)
public class LogMetricsCollector {

    private final MeterRegistry meterRegistry;

    // 计时器
    private final Timer executionTimer;

    public LogMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.executionTimer = Timer.builder("log.operations.duration")
                .description("日志操作执行时间")
                .register(meterRegistry != null ? meterRegistry : new SimpleMeterRegistry());
    }

    /**
     * 记录成功操作指标
     * 
     * @param context  日志上下文
     * @param duration 执行时长(纳秒)
     */
    public void recordSuccess(LogContext context, long duration) {
        try {
            // 如果meterRegistry为null，则直接返回，不记录指标
            if (meterRegistry == null) {
                return;
            }

            // 记录成功次数
            Counter.builder("log.operations.success")
                    .description("成功的日志操作次数")
                    .tag("type", context.getLogType() != null ? context.getLogType().getCode() : "UNKNOWN")
                    .tag("level", context.getLevel() != null ? context.getLevel().getName() : "UNKNOWN")
                    .tag("module", context.getModule() != null ? context.getModule() : "UNKNOWN")
                    .tag("async", String.valueOf(context.isAsync()))
                    .register(meterRegistry)
                    .increment();

            // 记录执行时间
            executionTimer.record(duration, TimeUnit.NANOSECONDS);

        } catch (Exception e) {
            log.debug("记录成功指标失败: {}", e.getMessage());
        }
    }

    /**
     * 记录错误操作指标
     * 
     * @param context  日志上下文
     * @param error    错误信息
     * @param duration 执行时长(纳秒)
     */
    public void recordError(LogContext context, Exception error, long duration) {
        try {
            // 如果meterRegistry为null，则直接返回，不记录指标
            if (meterRegistry == null) {
                return;
            }

            // 记录错误次数
            Counter.builder("log.operations.error")
                    .description("失败的日志操作次数")
                    .tag("type", context.getLogType() != null ? context.getLogType().getCode() : "UNKNOWN")
                    .tag("level", context.getLevel() != null ? context.getLevel().getName() : "UNKNOWN")
                    .tag("module", context.getModule() != null ? context.getModule() : "UNKNOWN")
                    .tag("async", String.valueOf(context.isAsync()))
                    .tag("error", error.getClass().getSimpleName())
                    .register(meterRegistry)
                    .increment();

            // 记录执行时间（即使失败也要记录）
            executionTimer.record(duration, TimeUnit.NANOSECONDS);

        } catch (Exception e) {
            log.debug("记录错误指标失败: {}", e.getMessage());
        }
    }

    /**
     * 记录自定义指标
     * 
     * @param name  指标名称
     * @param value 指标值
     * @param tags  标签
     */
    public void recordCustomMetric(String name, double value, String... tags) {
        try {
            if (meterRegistry == null) {
                return;
            }
            meterRegistry.gauge(name, value);
        } catch (Exception e) {
            log.debug("记录自定义指标失败: {}", e.getMessage());
        }
    }
}