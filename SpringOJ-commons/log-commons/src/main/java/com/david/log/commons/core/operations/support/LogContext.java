package com.david.log.commons.core.operations.support;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志上下文管理器
 * 
 * <p>
 * 提供线程安全的上下文信息管理，支持请求追踪和上下文数据存储
 * </p>
 * 
 * @author David
 * @version 1.0
 * @since 2024-08-31
 */
public final class LogContext {

    /**
     * 常用上下文键名常量
     */
    public static final String TRACE_ID = "traceId";
    public static final String USER_ID = "userId";
    public static final String REQUEST_URI = "requestUri";
    public static final String METHOD_NAME = "methodName";
    public static final String CLASS_NAME = "className";
    public static final String START_TIME = "startTime";
    /**
     * 时间格式化器
     */
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    /**
     * 线程本地存储 - 存储当前线程的上下文信息
     */
    private static final ThreadLocal<Map<String, Object>> CONTEXT_HOLDER = ThreadLocal
            .withInitial(ConcurrentHashMap::new);

    /**
     * 私有构造函数，防止实例化
     */
    private LogContext() {
        throw new UnsupportedOperationException("LogContext is a utility class and cannot be instantiated");
    }

    /**
     * 设置上下文值
     * 
     * @param key   键名
     * @param value 值
     */
    public static void put(String key, Object value) {
        if (key != null && value != null) {
            CONTEXT_HOLDER.get().put(key, value);
        }
    }

    /**
     * 获取上下文值
     * 
     * @param key 键名
     * @param <T> 值类型
     * @return 上下文值，如果不存在则返回null
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        if (key == null) {
            return null;
        }
        return (T) CONTEXT_HOLDER.get().get(key);
    }

    /**
     * 获取上下文值，如果不存在则返回默认值
     * 
     * @param key          键名
     * @param defaultValue 默认值
     * @param <T>          值类型
     * @return 上下文值或默认值
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key, T defaultValue) {
        if (key == null) {
            return defaultValue;
        }
        T value = (T) CONTEXT_HOLDER.get().get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 移除上下文值
     *
     * @param key 键名
     */
    public static void remove(String key) {
        if (key != null) {
            CONTEXT_HOLDER.get().remove(key);
        }
    }

    /**
     * 清空当前线程的所有上下文信息
     */
    public static void clear() {
        CONTEXT_HOLDER.get().clear();
    }

    /**
     * 清理当前线程的ThreadLocal，防止内存泄漏
     * 
     * <p>
     * 建议在请求结束时调用此方法
     * </p>
     */
    public static void cleanup() {
        CONTEXT_HOLDER.remove();
    }

    /**
     * 获取当前时间戳字符串
     * 
     * @return 格式化的时间戳
     */
    public static String getCurrentTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    }

    /**
     * 生成简单的追踪ID
     *
     * @return 追踪ID字符串
     */
    public static String generateTraceId() {
        return String.format("TRACE_%d_%d",
                System.currentTimeMillis(),
                Thread.currentThread().getId());
    }

    /**
     * 初始化请求上下文
     *
     * @param traceId 追踪ID
     */
    public static void initRequestContext(String traceId) {
        clear();
        put(TRACE_ID, traceId != null ? traceId : generateTraceId());
        put(START_TIME, System.currentTimeMillis());
    }

    /**
     * 初始化方法上下文
     *
     * @param className  类名
     * @param methodName 方法名
     */
    public static void initMethodContext(String className, String methodName) {
        put(CLASS_NAME, className);
        put(METHOD_NAME, methodName);
    }

    /**
     * 获取格式化的上下文信息字符串
     * 
     * @return 格式化的上下文信息
     */
    public static String getContextInfo() {
        Map<String, Object> context = CONTEXT_HOLDER.get();
        if (context.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        String traceId = get(TRACE_ID);
        if (traceId != null) {
            sb.append("[").append(traceId).append("]");
        }

        String className = get(CLASS_NAME);
        String methodName = get(METHOD_NAME);
        if (className != null && methodName != null) {
            sb.append("[").append(className).append(".").append(methodName).append("]");
        } else if (className != null) {
            sb.append("[").append(className).append("]");
        } else if (methodName != null) {
            sb.append("[").append(methodName).append("]");
        }

        return sb.toString();
    }

    /**
     * 获取执行耗时（毫秒）
     *
     * @return 从START_TIME到现在的耗时，如果START_TIME不存在则返回-1
     */
    public static long getElapsedTime() {
        Long startTime = get(START_TIME);
        if (startTime == null) {
            return -1L;
        }
        return System.currentTimeMillis() - startTime;
    }

    /**
     * 检查上下文是否为空
     * 
     * @return 如果上下文为空则返回true
     */
    public static boolean isEmpty() {
        return CONTEXT_HOLDER.get().isEmpty();
    }

    /**
     * 获取上下文大小
     * 
     * @return 上下文中键值对的数量
     */
    public static int size() {
        return CONTEXT_HOLDER.get().size();
    }
}
