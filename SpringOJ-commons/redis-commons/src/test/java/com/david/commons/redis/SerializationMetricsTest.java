package com.david.commons.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SerializationMetrics 单元测试
 *
 * @author David
 */
class SerializationMetricsTest {

    private RedisUtilsImpl.SerializationMetrics metrics;

    @BeforeEach
    void setUp() {
        metrics = new RedisUtilsImpl.SerializationMetrics();
    }

    @Test
    void testInitialState() {
        // 测试初始状态
        assertThat(metrics.getTotalOperations()).isEqualTo(0);
        assertThat(metrics.getSuccessfulOperations()).isEqualTo(0);
        assertThat(metrics.getFailedOperations()).isEqualTo(0);
        assertThat(metrics.getSuccessRate()).isEqualTo(0.0);
        assertThat(metrics.getAverageDuration()).isEqualTo(Duration.ZERO);
        assertThat(metrics.getMaxDuration()).isEqualTo(Duration.ZERO);
        assertThat(metrics.getMinDuration()).isEqualTo(Duration.ZERO);
        assertThat(metrics.getTotalDataSize()).isEqualTo(0);
        assertThat(metrics.getAverageDataSize()).isEqualTo(0.0);
    }

    @Test
    void testSuccessfulOperation() {
        // 记录成功操作
        Duration duration = Duration.ofMillis(100);
        int dataSize = 1024;

        metrics.recordOperation(duration, true, dataSize);

        assertThat(metrics.getTotalOperations()).isEqualTo(1);
        assertThat(metrics.getSuccessfulOperations()).isEqualTo(1);
        assertThat(metrics.getFailedOperations()).isEqualTo(0);
        assertThat(metrics.getSuccessRate()).isEqualTo(1.0);
        assertThat(metrics.getAverageDuration()).isEqualTo(duration);
        assertThat(metrics.getMaxDuration()).isEqualTo(duration);
        assertThat(metrics.getMinDuration()).isEqualTo(duration);
        assertThat(metrics.getTotalDataSize()).isEqualTo(dataSize);
        assertThat(metrics.getAverageDataSize()).isEqualTo(dataSize);
    }

    @Test
    void testFailedOperation() {
        // 记录失败操作
        Duration duration = Duration.ofMillis(50);
        int dataSize = 512;

        metrics.recordOperation(duration, false, dataSize);

        assertThat(metrics.getTotalOperations()).isEqualTo(1);
        assertThat(metrics.getSuccessfulOperations()).isEqualTo(0);
        assertThat(metrics.getFailedOperations()).isEqualTo(1);
        assertThat(metrics.getSuccessRate()).isEqualTo(0.0);
        assertThat(metrics.getAverageDuration()).isEqualTo(duration);
        assertThat(metrics.getMaxDuration()).isEqualTo(duration);
        assertThat(metrics.getMinDuration()).isEqualTo(duration);
        assertThat(metrics.getTotalDataSize()).isEqualTo(dataSize);
        assertThat(metrics.getAverageDataSize()).isEqualTo(dataSize);
    }

    @Test
    void testMultipleOperations() {
        // 记录多个操作
        metrics.recordOperation(Duration.ofMillis(100), true, 1000);
        metrics.recordOperation(Duration.ofMillis(200), true, 2000);
        metrics.recordOperation(Duration.ofMillis(50), false, 500);

        assertThat(metrics.getTotalOperations()).isEqualTo(3);
        assertThat(metrics.getSuccessfulOperations()).isEqualTo(2);
        assertThat(metrics.getFailedOperations()).isEqualTo(1);
        assertThat(metrics.getSuccessRate()).isEqualTo(2.0 / 3.0);

        // 平均耗时应该是 (100 + 200 + 50) / 3 = 116.67ms
        Duration expectedAverage = Duration.ofNanos((100_000_000L + 200_000_000L + 50_000_000L) / 3);
        assertThat(metrics.getAverageDuration()).isEqualTo(expectedAverage);

        // 最大最小耗时
        assertThat(metrics.getMaxDuration()).isEqualTo(Duration.ofMillis(200));
        assertThat(metrics.getMinDuration()).isEqualTo(Duration.ofMillis(50));

        // 数据大小
        assertThat(metrics.getTotalDataSize()).isEqualTo(3500);
        assertThat(metrics.getAverageDataSize()).isEqualTo(3500.0 / 3.0);
    }

    @Test
    void testConcurrentOperations() throws InterruptedException {
        // 测试并发操作
        int threadCount = 10;
        int operationsPerThread = 100;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    Duration duration = Duration.ofMillis(threadIndex * 10 + j);
                    boolean success = (j % 2 == 0); // 一半成功，一半失败
                    int dataSize = threadIndex * 100 + j;
                    metrics.recordOperation(duration, success, dataSize);
                }
            });
        }

        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }

        // 验证结果
        int expectedTotal = threadCount * operationsPerThread;
        assertThat(metrics.getTotalOperations()).isEqualTo(expectedTotal);
        assertThat(metrics.getSuccessfulOperations()).isEqualTo(expectedTotal / 2);
        assertThat(metrics.getFailedOperations()).isEqualTo(expectedTotal / 2);
        assertThat(metrics.getSuccessRate()).isEqualTo(0.5);
    }

    @Test
    void testToString() {
        // 测试 toString 方法
        metrics.recordOperation(Duration.ofMillis(100), true, 1000);
        metrics.recordOperation(Duration.ofMillis(200), false, 2000);

        String result = metrics.toString();
        assertThat(result).contains("SerializationMetrics");
        assertThat(result).contains("total=2");
        assertThat(result).contains("success=1");
        assertThat(result).contains("failed=1");
        assertThat(result).contains("successRate=50.00%");
        assertThat(result).contains("avgDuration=150ms");
        assertThat(result).contains("maxDuration=200ms");
        assertThat(result).contains("minDuration=100ms");
        assertThat(result).contains("avgDataSize=1500.0 bytes");
    }

    @Test
    void testEdgeCases() {
        // 测试边界情况

        // 零耗时操作
        metrics.recordOperation(Duration.ZERO, true, 0);
        assertThat(metrics.getAverageDuration()).isEqualTo(Duration.ZERO);
        assertThat(metrics.getMaxDuration()).isEqualTo(Duration.ZERO);
        assertThat(metrics.getMinDuration()).isEqualTo(Duration.ZERO);

        // 非常大的耗时
        Duration largeDuration = Duration.ofSeconds(10);
        metrics.recordOperation(largeDuration, true, 1000000);

        assertThat(metrics.getMaxDuration()).isEqualTo(largeDuration);
        assertThat(metrics.getMinDuration()).isEqualTo(Duration.ZERO);

        // 负数据大小（虽然不应该发生，但测试健壮性）
        metrics.recordOperation(Duration.ofMillis(100), true, -100);
        // 应该不会崩溃，但总数据大小会受影响
    }

    @Test
    void testPrecision() {
        // 测试精度
        Duration nanosDuration = Duration.ofNanos(123456789);
        metrics.recordOperation(nanosDuration, true, 100);

        assertThat(metrics.getAverageDuration()).isEqualTo(nanosDuration);
        assertThat(metrics.getMaxDuration()).isEqualTo(nanosDuration);
        assertThat(metrics.getMinDuration()).isEqualTo(nanosDuration);
    }
}