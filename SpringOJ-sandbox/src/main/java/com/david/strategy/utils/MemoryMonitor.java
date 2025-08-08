package com.david.strategy.utils;

import java.util.concurrent.atomic.AtomicLong;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Statistics;
import lombok.Builder;
import lombok.Data;

/**
 * 内存监控器
 */
@Data
@Builder
public class MemoryMonitor {
    private AtomicLong maxMemoryUsage;
    private volatile boolean monitoring;
    private ResultCallback<Statistics> statsCallback;

    public void stopMonitoring() {
        monitoring = false;
        if (statsCallback != null) {
            try {
                statsCallback.close();
            } catch (Exception ignored) {
            }
        }
    }
}
