package com.david.log.commons.core.sequence;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 日志序列号生成器
 * 
 * <p>
 * 提供全局递增的日志序列号，确保日志的严格时序关系
 * </p>
 * 
 * @author David
 * @version 1.0
 * @since 2024-08-31
 */
public final class LogSequenceGenerator {

    /**
     * 全局序列号计数器
     */
    private static final AtomicLong SEQUENCE_COUNTER = new AtomicLong(0);

    /**
     * 序列号格式化长度
     */
    private static final int SEQUENCE_LENGTH = 8;

    /**
     * 私有构造函数，防止实例化
     */
    private LogSequenceGenerator() {
        throw new UnsupportedOperationException("LogSequenceGenerator is a utility class and cannot be instantiated");
    }

    /**
     * 生成下一个序列号
     * 
     * @return 格式化的序列号字符串，如 "SEQ:00000001"
     */
    public static String nextSequence() {
        long sequence = SEQUENCE_COUNTER.incrementAndGet();
        return formatSequence(sequence);
    }

    /**
     * 获取当前序列号（不递增）
     * 
     * @return 当前序列号
     */
    public static long getCurrentSequence() {
        return SEQUENCE_COUNTER.get();
    }

    /**
     * 重置序列号（仅用于测试）
     * 
     * @param value 重置值
     */
    public static void resetSequence(long value) {
        SEQUENCE_COUNTER.set(value);
    }

    /**
     * 格式化序列号
     * 
     * @param sequence 原始序列号
     * @return 格式化的序列号字符串
     */
    private static String formatSequence(long sequence) {
        return String.format("SEQ:%0" + SEQUENCE_LENGTH + "d", sequence);
    }

    /**
     * 检查序列号是否即将溢出
     * 
     * @return 如果即将溢出则返回true
     */
    public static boolean isNearOverflow() {
        return SEQUENCE_COUNTER.get() > Long.MAX_VALUE - 1000000;
    }
}
