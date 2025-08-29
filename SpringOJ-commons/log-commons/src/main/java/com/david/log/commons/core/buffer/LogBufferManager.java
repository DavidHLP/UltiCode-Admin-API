package com.david.log.commons.core.buffer;

import com.david.log.commons.core.context.LogContext;
import com.david.log.commons.core.processor.SyncLogHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 日志缓冲管理器
 * 
 * <p>
 * 管理异步日志的缓冲和批量处理，使用环形缓冲区和定时刷新机制，
 * 提供内存使用监控和手动刷新功能。
 * 
 * @author David
 */
@Slf4j
@Component
public class LogBufferManager {

    @Value("${log-commons.buffer-size:1000}")
    private int bufferSize;

    @Value("${log-commons.flush-interval-seconds:5}")
    private int flushIntervalSeconds;

    @Value("${log-commons.batch-size:50}")
    private int batchSize;

    private BlockingQueue<LogContext> buffer;
    private ScheduledExecutorService scheduler;
    private final SyncLogHandler syncLogHandler;

    // 统计指标
    private final AtomicLong submittedCount = new AtomicLong(0);
    private final AtomicLong processedCount = new AtomicLong(0);
    private final AtomicLong droppedCount = new AtomicLong(0);

    public LogBufferManager(SyncLogHandler syncLogHandler) {
        this.syncLogHandler = syncLogHandler;
    }

    @PostConstruct
    public void init() {
        // 初始化缓冲区
        this.buffer = new ArrayBlockingQueue<>(bufferSize);

        // 启动定时刷新任务
        this.scheduler = Executors.newSingleThreadScheduledExecutor(
                r -> {
                    Thread thread = new Thread(r, "log-buffer-scheduler");
                    thread.setDaemon(true);
                    return thread;
                });

        scheduler.scheduleAtFixedRate(
                this::flushBuffer,
                flushIntervalSeconds,
                flushIntervalSeconds,
                TimeUnit.SECONDS);

        log.info("日志缓冲管理器已初始化 - 缓冲区大小:{}, 刷新间隔:{}秒, 批处理大小:{}",
                bufferSize, flushIntervalSeconds, batchSize);
    }

    /**
     * 提交日志到缓冲区
     * 
     * @param context 日志上下文
     * @return 是否成功提交
     */
    public boolean submit(LogContext context) {
        if (context == null) {
            return false;
        }

        submittedCount.incrementAndGet();

        boolean offered = buffer.offer(context);
        if (!offered) {
            // 缓冲区满时的处理策略
            droppedCount.incrementAndGet();
            handleBufferOverflow(context);
            return false;
        }

        // 检查是否需要立即刷新
        if (buffer.size() >= batchSize) {
            scheduler.execute(this::flushBuffer);
        }

        return true;
    }

    /**
     * 手动刷新缓冲区
     */
    public void flush() {
        flushBuffer();
    }

    /**
     * 刷新所有缓冲区内容
     */
    public void flushAll() {
        while (!buffer.isEmpty()) {
            flushBuffer();
        }
    }

    /**
     * 获取缓冲区状态信息
     * 
     * @return 状态信息
     */
    public BufferStatus getStatus() {
        return BufferStatus.builder()
                .bufferSize(buffer.size())
                .maxBufferSize(bufferSize)
                .submittedCount(submittedCount.get())
                .processedCount(processedCount.get())
                .droppedCount(droppedCount.get())
                .usageRate((double) buffer.size() / bufferSize)
                .build();
    }

    /**
     * 执行缓冲区刷新
     */
    private void flushBuffer() {
        if (buffer.isEmpty()) {
            return;
        }

        try {
            List<LogContext> batch = new ArrayList<>(batchSize);

            // 批量取出日志条目
            buffer.drainTo(batch, batchSize);

            if (batch.isEmpty()) {
                return;
            }

            // 批量处理日志
            for (LogContext context : batch) {
                try {
                    syncLogHandler.process(context);
                    processedCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("处理缓冲日志失败 - 操作:{}, 消息:{}",
                            context.getOperation(), context.getMessage(), e);
                }
            }

            log.debug("已刷新日志缓冲区，处理数量: {}", batch.size());

        } catch (Exception e) {
            log.error("刷新日志缓冲区失败", e);
        }
    }

    /**
     * 处理缓冲区溢出
     */
    private void handleBufferOverflow(LogContext context) {
        // 记录溢出警告（使用最简单的方式避免递归）
        System.err.println("警告：日志缓冲区已满，丢弃日志 - " +
                context.getOperation() + ": " + context.getMessage());

        // 可以考虑以下策略：
        // 1. 强制刷新一次缓冲区
        // 2. 丢弃最老的日志条目
        // 3. 降级到同步处理

        // 这里选择强制刷新策略
        scheduler.execute(this::flushBuffer);
    }

    /**
     * 关闭缓冲管理器
     */
    @PreDestroy
    public void shutdown() {
        log.info("正在关闭日志缓冲管理器...");

        try {
            // 刷新所有剩余日志
            flushAll();

            // 关闭调度器
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown();
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    log.warn("日志缓冲管理器关闭超时，强制关闭");
                    scheduler.shutdownNow();
                }
            }

            log.info("日志缓冲管理器已关闭 - 最终状态: {}", getStatus());

        } catch (Exception e) {
            log.error("关闭日志缓冲管理器失败", e);
        }
    }
}
