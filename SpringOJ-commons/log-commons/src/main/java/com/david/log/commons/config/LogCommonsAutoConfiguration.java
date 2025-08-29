package com.david.log.commons.config;

import com.david.log.commons.core.LogUtils;
import com.david.log.commons.core.buffer.LogBufferManager;
import com.david.log.commons.core.executor.LogOperationExecutor;
import com.david.log.commons.core.formatter.DefaultLogFormatter;
import com.david.log.commons.core.formatter.LogFormatter;
import com.david.log.commons.core.masker.DefaultSensitiveDataMasker;
import com.david.log.commons.core.masker.SensitiveDataMasker;
import com.david.log.commons.core.operations.BusinessLogOperations;
import com.david.log.commons.core.operations.ExceptionLogOperations;
import com.david.log.commons.core.operations.PerformanceLogOperations;
import com.david.log.commons.core.operations.SecurityLogOperations;
import com.david.log.commons.core.operations.impl.BusinessLogOperationsImpl;
import com.david.log.commons.core.operations.impl.ExceptionLogOperationsImpl;
import com.david.log.commons.core.operations.impl.PerformanceLogOperationsImpl;
import com.david.log.commons.core.operations.impl.SecurityLogOperationsImpl;
import com.david.log.commons.core.processor.AsyncLogHandler;
import com.david.log.commons.core.processor.LogProcessor;
import com.david.log.commons.core.processor.SyncLogHandler;
import com.david.log.commons.metrics.LogMetricsCollector;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 日志组件自动配置类
 *
 * <p>遵循Spring Boot自动配置规范，提供日志组件的默认配置 和条件化Bean创建，支持通过配置属性灵活定制。
 *
 * @author David
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(LogCommonsProperties.class)
@ConditionalOnProperty(
        prefix = "log-commons",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class LogCommonsAutoConfiguration {

    private final LogCommonsProperties properties;

    public LogCommonsAutoConfiguration(LogCommonsProperties properties) {
        this.properties = properties;
        log.info("初始化LogCommons自动配置 - 配置: {}", properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public SensitiveDataMasker sensitiveDataMasker() {
        DefaultSensitiveDataMasker masker = new DefaultSensitiveDataMasker();

        // 添加自定义敏感关键词
        if (properties.getSensitive().getKeywords() != null) {
            for (String keyword : properties.getSensitive().getKeywords()) {
                masker.addSensitiveKeyword(keyword);
            }
        }

        // 添加自定义敏感数据模式
        if (properties.getSensitive().getPatterns() != null) {
            for (String pattern : properties.getSensitive().getPatterns()) {
                masker.addSensitivePattern(pattern);
            }
        }

        return masker;
    }

    @Bean
    @ConditionalOnMissingBean
    public LogFormatter logFormatter(SensitiveDataMasker sensitiveDataMasker) {
        return new DefaultLogFormatter(sensitiveDataMasker);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(MeterRegistry.class)
    @ConditionalOnProperty(
            prefix = "log-commons.metrics",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    public LogMetricsCollector logMetricsCollector(MeterRegistry meterRegistry) {
        return new LogMetricsCollector(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(name = "noOpLogMetricsCollector")
    @ConditionalOnProperty(prefix = "log-commons.metrics", name = "enabled", havingValue = "false")
    public LogMetricsCollector noOpLogMetricsCollector() {
        // 提供一个空实现，避免依赖注入失败
        return new LogMetricsCollector(null) {
            @Override
            public void recordSuccess(
                    com.david.log.commons.core.context.LogContext context, long duration) {
                // 空实现
            }

            @Override
            public void recordError(
                    com.david.log.commons.core.context.LogContext context,
                    Exception error,
                    long duration) {
                // 空实现
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public SyncLogHandler syncLogHandler(LogFormatter logFormatter) {
        return new SyncLogHandler(logFormatter);
    }

    @Bean
    @ConditionalOnMissingBean
    public LogBufferManager logBufferManager(SyncLogHandler syncLogHandler) {
        return new LogBufferManager(syncLogHandler);
    }

    @Bean
    @ConditionalOnMissingBean
    public AsyncLogHandler asyncLogHandler(
            LogBufferManager bufferManager, SyncLogHandler syncLogHandler) {
        return new AsyncLogHandler(bufferManager, syncLogHandler);
    }

    @Bean
    @ConditionalOnMissingBean
    public LogOperationExecutor logOperationExecutor(
            List<LogProcessor> processors, LogMetricsCollector metricsCollector) {
        return new LogOperationExecutor(processors, metricsCollector);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "log-commons.modules.business",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    public BusinessLogOperations businessLogOperations(LogOperationExecutor executor) {
        return new BusinessLogOperationsImpl(executor);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "log-commons.modules.performance",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    public PerformanceLogOperations performanceLogOperations(LogOperationExecutor executor) {
        return new PerformanceLogOperationsImpl(executor);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "log-commons.modules.security",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    public SecurityLogOperations securityLogOperations(LogOperationExecutor executor) {
        return new SecurityLogOperationsImpl(executor);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "log-commons.modules.exception",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    public ExceptionLogOperations exceptionLogOperations(LogOperationExecutor executor) {
        return new ExceptionLogOperationsImpl(executor);
    }

    @Bean
    @ConditionalOnMissingBean
    public LogUtils logUtils(
            BusinessLogOperations businessLogOperations,
            PerformanceLogOperations performanceLogOperations,
            SecurityLogOperations securityLogOperations,
            ExceptionLogOperations exceptionLogOperations) {
        return new LogUtils(
                businessLogOperations,
                performanceLogOperations,
                securityLogOperations,
                exceptionLogOperations);
    }
}
