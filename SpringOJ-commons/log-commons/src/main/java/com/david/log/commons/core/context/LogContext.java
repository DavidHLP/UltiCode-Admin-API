package com.david.log.commons.core.context;

import com.david.log.commons.core.operations.LogLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 日志上下文信息封装
 * 
 * <p>
 * 封装单次日志操作的所有上下文信息，包括操作名称、模块信息、
 * 用户信息、链路追踪信息等。采用不可变对象设计，线程安全。
 * 
 * @author David
 */
@Getter
@ToString
@Builder(toBuilder = true)
public class LogContext {

    /**
     * 操作名称
     */
    private final String operation;

    /**
     * 模块名称
     */
    private final String module;

    /**
     * 用户ID
     */
    private final String userId;

    /**
     * 链路追踪ID
     */
    private final String traceId;

    /**
     * 会话ID
     */
    private final String sessionId;

    /**
     * 日志消息
     */
    private final String message;

    /**
     * 格式化参数
     */
    private final Object[] args;

    /**
     * 异常信息
     */
    private final Throwable throwable;

    /**
     * 日志级别
     */
    private final LogLevel level;

    /**
     * 是否异步处理
     */
    @Builder.Default
    private final boolean async = true;

    /**
     * 创建时间
     */
    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 扩展元数据
     */
    @Builder.Default
    private final Map<String, Object> metadata = new HashMap<>();

    /**
     * 日志类型
     */
    private final LogType logType;

    /**
     * 获取扩展元数据的副本
     * 
     * @return 元数据副本
     */
    public Map<String, Object> getMetadata() {
        return new HashMap<>(metadata);
    }

    /**
     * 获取指定键的元数据值
     * 
     * @param key 键名
     * @return 元数据值
     */
    public Object getMetadataValue(String key) {
        return metadata.get(key);
    }

    /**
     * 创建带有新元数据的上下文副本
     * 
     * @param key   键名
     * @param value 值
     * @return 新的上下文对象
     */
    public LogContext withMetadata(String key, Object value) {
        Map<String, Object> newMetadata = new HashMap<>(this.metadata);
        newMetadata.put(key, value);
        return this.toBuilder()
                .metadata(newMetadata)
                .build();
    }

    /**
     * 创建带有新级别的上下文副本
     * 
     * @param level 新的日志级别
     * @return 新的上下文对象
     */
    public LogContext withLevel(LogLevel level) {
        return this.toBuilder()
                .level(level)
                .build();
    }

    /**
     * 创建带有新异步设置的上下文副本
     * 
     * @param async 是否异步
     * @return 新的上下文对象
     */
    public LogContext withAsync(boolean async) {
        return this.toBuilder()
                .async(async)
                .build();
    }
}
