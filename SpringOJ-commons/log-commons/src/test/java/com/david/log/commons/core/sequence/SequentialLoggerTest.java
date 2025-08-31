package com.david.log.commons.core.sequence;

import static org.junit.jupiter.api.Assertions.*;

import com.david.log.commons.LogUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 序列化日志测试
 * 
 * <p>
 * 验证日志顺序性保证机制
 * </p>
 * 
 * @author David
 * @version 1.0
 * @since 2024-08-31
 */
class SequentialLoggerTest {

    @BeforeEach
    void setUp() {
        // 重置序列号计数器
        LogSequenceGenerator.resetSequence(0);
        // 清理MDC
        MDC.clear();
    }

    @Test
    void testSequenceGeneration() {
        // 测试序列号生成的递增性
        String seq1 = LogSequenceGenerator.nextSequence();
        String seq2 = LogSequenceGenerator.nextSequence();
        String seq3 = LogSequenceGenerator.nextSequence();

        assertEquals("SEQ:00000001", seq1);
        assertEquals("SEQ:00000002", seq2);
        assertEquals("SEQ:00000003", seq3);
    }

    @Test
    void testThreadSafeFormatterConsistency() {
        // 测试线程安全格式化器的一致性
        String timestamp1 = ThreadSafeFormatter.formatCurrentTimestamp();
        String timestamp2 = ThreadSafeFormatter.formatCurrentTimestamp();

        assertNotNull(timestamp1);
        assertNotNull(timestamp2);
        assertTrue(timestamp1.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}"));
    }

    @Test
    void testExtendedTimestampUniqueness() throws InterruptedException {
        // 测试扩展时间戳的唯一性
        String extended1 = ThreadSafeFormatter.formatExtendedTimestamp();
        Thread.sleep(1); // 确保纳秒级差异
        String extended2 = ThreadSafeFormatter.formatExtendedTimestamp();

        assertNotEquals(extended1, extended2);
        assertTrue(extended1.contains("."));
        assertTrue(extended2.contains("."));
    }

    @Test
    void testSequentialLoggerBasicFunctionality() {
        SequentialLogger logger = SequentialLoggerFactory.getLogger("test.logger");

        assertNotNull(logger);
        assertEquals("test.logger", logger.getName());
        assertTrue(logger.isInfoEnabled());
    }

    @Test
    void testLogUtilsIntegration() {
        // 测试LogUtils集成
        assertDoesNotThrow(() -> {
            LogUtils.debug("Debug message with param: {}", "test");
            LogUtils.info("Info message");
            LogUtils.warn("Warning message: {}", "warning");
            LogUtils.error("Error message");
        });
    }

    @Test
    void testConcurrentLoggingOrderPreservation() throws InterruptedException {
        final int threadCount = 10;
        final int messagesPerThread = 100;
        final CountDownLatch latch = new CountDownLatch(threadCount);
        final AtomicInteger completedThreads = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // 启动多个线程并发写日志
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < messagesPerThread; j++) {
                        LogUtils.info("Thread-{} Message-{}: Current sequence should be increasing",
                                threadId, j);

                        // 验证序列号严格递增
                        long currentSeq = LogSequenceGenerator.getCurrentSequence();
                        assertTrue(currentSeq > 0, "序列号应大于0");
                    }
                } finally {
                    completedThreads.incrementAndGet();
                    latch.countDown();
                }
            });
        }

        // 等待所有线程完成
        assertTrue(latch.await(30, TimeUnit.SECONDS), "测试超时");
        assertEquals(threadCount, completedThreads.get());

        // 验证最终序列号
        long finalSequence = LogSequenceGenerator.getCurrentSequence();
        assertEquals(threadCount * messagesPerThread, finalSequence,
                "最终序列号应等于总消息数");

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    void testBusinessLoggerIntegration() {
        // 测试业务日志集成
        assertDoesNotThrow(() -> {
            LogUtils.business()
                    .className("TestClass")
                    .methodName("testMethod")
                    .message("Business operation completed")
                    .info();

            LogUtils.business("Direct business message with param: {}", "value");
        });
    }

    @Test
    void testPerformanceLogging() {
        // 测试性能日志
        assertDoesNotThrow(() -> {
            LogUtils.performance("TestClass", "fastMethod", 50);
            LogUtils.performance("TestClass", "slowMethod", 150);
            LogUtils.performance("TestClass", "verySlowMethod", 1500);
        });
    }

    @Test
    void testMethodTracing() {
        // 测试方法跟踪
        assertDoesNotThrow(() -> {
            LogUtils.enter("TestClass", "testMethod", "param1", "param2");
            LogUtils.exit("TestClass", "testMethod", "result");

            // 自动获取类名和方法名的版本
            LogUtils.enter("auto-param1", "auto-param2");
            LogUtils.exit("auto-result");
        });
    }

    @Test
    void testLoggerCaching() {
        // 测试日志器缓存
        SequentialLogger logger1 = SequentialLoggerFactory.getLogger("cache.test");
        SequentialLogger logger2 = SequentialLoggerFactory.getLogger("cache.test");

        assertSame(logger1, logger2, "相同名称的日志器应该被缓存");

        // 测试清理缓存
        int initialSize = SequentialLoggerFactory.getCacheSize();
        SequentialLoggerFactory.clearCache();
        assertEquals(0, SequentialLoggerFactory.getCacheSize());

        // 重新创建后应该是新实例
        SequentialLogger logger3 = SequentialLoggerFactory.getLogger("cache.test");
        assertNotSame(logger1, logger3);
    }

    @Test
    void testSequenceOverflowCheck() {
        // 测试序列号溢出检查
        assertFalse(LogSequenceGenerator.isNearOverflow(),
                "初始状态不应接近溢出");

        // 设置接近最大值
        LogSequenceGenerator.resetSequence(Long.MAX_VALUE - 100);
        assertTrue(LogSequenceGenerator.isNearOverflow(),
                "接近最大值时应该返回true");
    }
}
