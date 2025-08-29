package com.david.log.commons.core.processor;

import com.david.log.commons.core.buffer.LogBufferManager;
import com.david.log.commons.core.context.LogContext;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 异步日志处理器
 * 
 * <p>
 * 使用独立线程池和缓冲机制处理日志输出，避免阻塞业务流程。
 * 支持优雅关闭和降级处理机制。
 * 
 * @author David
 */
@Slf4j
@Component
public class AsyncLogHandler implements LogProcessor {

    private final LogBufferManager bufferManager;
    private final SyncLogHandler syncLogHandler;
    private final ExecutorService executorService;
    private volatile boolean shutdown = false;

    public AsyncLogHandler(LogBufferManager bufferManager, SyncLogHandler syncLogHandler) {
        this.bufferManager = bufferManager;
        this.syncLogHandler = syncLogHandler;
        this.executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                r -> {
                    Thread thread = new Thread(r, "async-log-handler");
                    thread.setDaemon(true);
                    return thread;
                });
    }

    @Override
    public Object process(LogContext context) {
        if (shutdown) {
            // 系统关闭时降级到同步处理
            return syncLogHandler.process(context);
        }

        try {
            // 提交到缓冲管理器
            boolean submitted = bufferManager.submit(context);

            if (!submitted) {
                // 缓冲区满时降级到同步处理
                log.debug("缓冲区已满，降级使用同步处理");
                return syncLogHandler.process(context);
            }

            return CompletableFuture.completedFuture("ASYNC_SUBMITTED");

        } catch (Exception e) {
            // 异步处理失败时降级到同步处理
            log.warn("异步日志处理失败，降级使用同步处理: {}", e.getMessage());
            return syncLogHandler.process(context);
        }
    }

    @Override
    public boolean supportsAsync() {
        return true;
    }

    @Override
    public ProcessorType getType() {
        return ProcessorType.ASYNC;
    }

    /**
     * 优雅关闭处理器
     */
    @PreDestroy
    public void shutdown() {
        log.info("正在关闭异步日志处理器...");
        shutdown = true;

        try {
            // 刷新缓冲区中的所有日志
            bufferManager.flushAll();

            // 关闭线程池
            executorService.shutdown();

            // 等待任务完成
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                log.warn("异步日志处理器关闭超时，强制关闭");
                executorService.shutdownNow();
            }

            log.info("异步日志处理器已关闭");

        } catch (InterruptedException e) {
            log.error("关闭异步日志处理器时被中断", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("关闭异步日志处理器失败", e);
        }
    }
}
