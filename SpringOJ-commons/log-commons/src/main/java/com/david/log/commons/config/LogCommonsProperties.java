package com.david.log.commons.config;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 日志组件配置属性
 *
 * <p>定义日志组件的配置项，支持通过application.yml进行配置。
 *
 * @author David
 */
@Data
@ConfigurationProperties(prefix = "log-commons")
public class LogCommonsProperties {

    /** 是否启用日志组件 */
    private boolean enabled = true;

    /** 默认是否使用异步处理 */
    private boolean defaultAsync = true;

    /** 缓冲区大小 */
    private int bufferSize = 1000;

    /** 刷新间隔(秒) */
    private int flushIntervalSeconds = 5;

    /** 批处理大小 */
    private int batchSize = 50;

    /** 模块配置 */
    private ModuleConfig modules = new ModuleConfig();

    /** 敏感信息脱敏配置 */
    private SensitiveConfig sensitive = new SensitiveConfig();

    /** 指标收集配置 */
    private MetricsConfig metrics = new MetricsConfig();

    @Data
    public static class ModuleConfig {
        /** 业务日志配置 */
        private LogModuleConfig business = new LogModuleConfig();

        /** 性能日志配置 */
        private LogModuleConfig performance = new LogModuleConfig();

        /** 安全日志配置 */
        private LogModuleConfig security = new LogModuleConfig();

        /** 异常日志配置 */
        private LogModuleConfig exception = new LogModuleConfig();
    }

    @Data
    public static class LogModuleConfig {
        /** 是否启用该模块 */
        private boolean enabled = true;

        /** 日志级别 */
        private String level = "INFO";

        /** 是否使用异步处理 */
        private boolean async = true;

        /** 是否启用指标收集 */
        private boolean metricsEnabled = true;
    }

    @Data
    public static class SensitiveConfig {
        /** 是否启用敏感信息脱敏 */
        private boolean enabled = true;

        /** 自定义敏感关键词 */
        private String[] keywords = {"password", "token", "secret", "key", "credit", "card"};

        /** 自定义敏感数据正则表达式 */
        private String[] patterns = {
            "1[3-9]\\d{9}", "[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}", "\\d{17}[\\dXx]"
        };

        /** 脱敏标记 */
        private String maskPattern = "***";
    }

    @Data
    public static class MetricsConfig {
        /** 是否启用指标收集 */
        private boolean enabled = true;

        /** 指标前缀 */
        private String prefix = "log.commons";

        /** 是否记录详细指标 */
        private boolean detailed = true;
    }
}
