package com.david.log.commons.core.buffer;

import lombok.Builder;

/**
 * 缓冲区状态信息
 *
 * <p>封装日志缓冲区的运行状态和统计信息， 用于监控和调试目的。
 *
 * @author David
 * @param bufferSize 当前缓冲区大小
 * @param maxBufferSize 最大缓冲区大小
 * @param submittedCount 已提交日志数量
 * @param processedCount 已处理日志数量
 * @param droppedCount 已丢弃日志数量
 * @param usageRate 缓冲区使用率 (0.0 - 1.0)
 */
@Builder
public record BufferStatus(
        int bufferSize,
        int maxBufferSize,
        long submittedCount,
        long processedCount,
        long droppedCount,
        double usageRate) {

    /**
     * 获取待处理日志数量
     *
     * @return 待处理数量
     */
    public long getPendingCount() {
        return submittedCount - processedCount - droppedCount;
    }

    /**
     * 获取处理成功率
     *
     * @return 成功率 (0.0 - 1.0)
     */
    public double getSuccessRate() {
        if (submittedCount == 0) {
            return 1.0;
        }
        return (double) processedCount / submittedCount;
    }

    /**
     * 获取丢弃率
     *
     * @return 丢弃率 (0.0 - 1.0)
     */
    public double getDropRate() {
        if (submittedCount == 0) {
            return 0.0;
        }
        return (double) droppedCount / submittedCount;
    }

    /**
     * 判断缓冲区是否健康
     *
     * @return 是否健康
     */
    public boolean isHealthy() {
        return usageRate < 0.8 && getDropRate() < 0.01;
    }
}
