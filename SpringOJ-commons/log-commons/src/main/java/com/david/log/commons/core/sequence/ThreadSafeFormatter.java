package com.david.log.commons.core.sequence;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 线程安全的时间戳格式化器
 * 
 * <p>
 * 使用ThreadLocal避免多线程竞争，提供高精度时间戳
 * </p>
 * 
 * @author David
 * @version 1.0
 * @since 2024-08-31
 */
public final class ThreadSafeFormatter {

    /**
     * 高精度时间格式
     */
    private static final String HIGH_PRECISION_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * 线程本地时间格式化器
     */
    private static final ThreadLocal<DateTimeFormatter> FORMATTER = ThreadLocal
            .withInitial(() -> DateTimeFormatter.ofPattern(HIGH_PRECISION_PATTERN));

    /**
     * 私有构造函数，防止实例化
     */
    private ThreadSafeFormatter() {
        throw new UnsupportedOperationException("ThreadSafeFormatter is a utility class and cannot be instantiated");
    }

    /**
     * 格式化当前时间戳
     * 
     * @return 格式化的时间戳字符串
     */
    public static String formatCurrentTimestamp() {
        return FORMATTER.get().format(LocalDateTime.now());
    }

    /**
     * 格式化指定时间戳
     * 
     * @param dateTime 要格式化的时间
     * @return 格式化的时间戳字符串
     */
    public static String formatTimestamp(LocalDateTime dateTime) {
        return FORMATTER.get().format(dateTime);
    }

    /**
     * 获取纳秒级扩展时间戳（用于同毫秒内的排序）
     * 
     * @return 包含纳秒信息的扩展时间戳
     */
    public static String formatExtendedTimestamp() {
        long nanoTime = System.nanoTime();
        String baseTimestamp = formatCurrentTimestamp();
        // 添加纳秒后缀用于同毫秒内的细分排序
        long nanoSuffix = nanoTime % 1000000; // 取纳秒部分的最后6位
        return String.format("%s.%06d", baseTimestamp, nanoSuffix);
    }

    /**
     * 清理ThreadLocal资源
     */
    public static void cleanup() {
        FORMATTER.remove();
    }
}
