package com.david.redis.commons.manager;

import com.david.log.commons.core.LogUtils;
import com.david.redis.commons.core.RedisUtils;
import com.david.redis.commons.enums.WarmUpPriority;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 缓存预热管理器
 * 支持启动预热、定时预热、触发式预热等多种预热策略
 * 
 * @author David
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class CacheWarmUpManager {

    private final RedisUtils redisUtils;
    private final BatchOperationManager batchManager;
    private final LogUtils logUtils;

    // 预热任务队列，按优先级排序
    private final PriorityBlockingQueue<WarmUpTask> warmUpQueue = new PriorityBlockingQueue<>();
    private final Map<String, WarmUpTask> registeredTasks = new ConcurrentHashMap<>();
    private final AtomicBoolean isWarming = new AtomicBoolean(false);

    /**
     * 应用启动时预热
     */
    @EventListener(ApplicationReadyEvent.class)
    public void warmUpOnStartup() {
        logUtils.business().event("cache_warmup_startup", "应用启动完成，开始缓存预热");

        CompletableFuture.runAsync(() -> {
            try {
                // 延迟5秒开始预热，避免启动时性能影响
                Thread.sleep(5000);
                executeWarmUp(WarmUpPriority.HIGH);

                // 再延迟10秒预热中优先级数据
                Thread.sleep(10000);
                executeWarmUp(WarmUpPriority.MEDIUM);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logUtils.exception().system("cache_warmup_interrupted", e, "medium");
            } catch (Exception e) {
                logUtils.exception().system("cache_warmup_startup_failed", e, "high");
            }
        });
    }

    /**
     * 定时预热（每5分钟执行一次）
     */
    @Scheduled(fixedRate = 300000)
    public void scheduledWarmUp() {
        if (isWarming.get()) {
            logUtils.business().trace("cache_warmup_scheduled", "skip", "预热正在进行中，跳过定时预热");
            return;
        }

        logUtils.business().trace("cache_warmup_scheduled", "start", "开始定时预热");
        executeWarmUp(WarmUpPriority.LOW);
    }

    /**
     * 触发式预热
     * 
     * @param pattern  缓存键模式
     * @param priority 预热优先级
     */
    public void triggerWarmUp(String pattern, WarmUpPriority priority) {
        if (pattern == null || pattern.trim().isEmpty()) {
            logUtils.business().trace("cache_warmup_trigger", "empty_pattern", "预热模式为空，跳过预热");
            return;
        }

        WarmUpTask task = new WarmUpTask(pattern, priority, System.currentTimeMillis());
        registeredTasks.put(pattern, task);
        warmUpQueue.offer(task);

        logUtils.business().audit("system", "cache_warmup_trigger", "success", "模式: " + pattern, "优先级: " + priority);

        // 如果是高优先级，立即执行
        if (priority == WarmUpPriority.HIGH) {
            CompletableFuture.runAsync(() -> executeWarmUp(priority));
        }
    }

    /**
     * 注册预热任务
     * 
     * @param pattern    缓存键模式
     * @param priority   预热优先级
     * @param dataLoader 数据加载器
     */
    public void registerWarmUpTask(String pattern, WarmUpPriority priority, DataLoader dataLoader) {
        WarmUpTask task = new WarmUpTask(pattern, priority, System.currentTimeMillis());
        task.setDataLoader(dataLoader);
        registeredTasks.put(pattern, task);

        logUtils.business().audit("system", "cache_warmup_register", "success", "模式: " + pattern, "优先级: " + priority);
    }

    /**
     * 执行预热操作
     */
    private void executeWarmUp(WarmUpPriority targetPriority) {
        if (!isWarming.compareAndSet(false, true)) {
            logUtils.business().trace("cache_warmup_execute", "skip", "预热正在进行中，跳过本次预热");
            return;
        }

        try {
            List<WarmUpTask> tasksToExecute = new ArrayList<>();

            // 收集指定优先级的任务
            WarmUpTask task;
            while ((task = warmUpQueue.poll()) != null) {
                if (task.getPriority() == targetPriority) {
                    tasksToExecute.add(task);
                } else {
                    // 放回队列
                    warmUpQueue.offer(task);
                    break;
                }
            }

            if (tasksToExecute.isEmpty()) {
                logUtils.business().trace("cache_warmup_execute", "no_tasks", "没有找到优先级为 " + targetPriority + " 的预热任务");
                return;
            }

            logUtils.business().event("cache_warmup_execute_start", "优先级: " + targetPriority, "任务数: " + tasksToExecute.size());

            int totalWarmed = 0;
            for (WarmUpTask warmUpTask : tasksToExecute) {
                try {
                    int warmed = executeTask(warmUpTask);
                    totalWarmed += warmed;

                    // 任务间隔，避免对系统造成压力
                    Thread.sleep(100);

                } catch (Exception e) {
                    logUtils.exception().business("cache_warmup_task_failed", e, "模式: " + warmUpTask.getPattern());
                }
            }

            logUtils.performance().timing("cache_warmup_complete", 0, "优先级: " + targetPriority, "预热键数: " + totalWarmed);

        } catch (Exception e) {
            logUtils.exception().system("cache_warmup_execute_failed", e, "high");
        } finally {
            isWarming.set(false);
        }
    }

    /**
     * 执行单个预热任务
     */
    private int executeTask(WarmUpTask task) {
        String pattern = task.getPattern();

        try {
            // 查找匹配的键
            Set<String> existingKeys = redisUtils.strings().scanKeys(pattern);

            if (existingKeys.isEmpty()) {
                // 如果没有现有键，尝试使用数据加载器
                if (task.getDataLoader() != null) {
                    Map<String, Object> data = task.getDataLoader().loadData(pattern);
                    if (!data.isEmpty()) {
                        batchManager.batchSet(data, 3600); // 默认1小时TTL
                        logUtils.business().trace("cache_warmup_load_data", "success", "模式: " + pattern, "数据量: " + data.size());
                        return data.size();
                    }
                }
                return 0;
            }

            // 检查现有键的TTL，刷新即将过期的缓存
            List<String> keysToRefresh = new ArrayList<>();
            for (String key : existingKeys) {
                Long ttl = redisUtils.strings().getExpire(key);
                if (ttl != null && ttl > 0 && ttl < 300) { // TTL小于5分钟
                    keysToRefresh.add(key);
                }
            }

            if (!keysToRefresh.isEmpty()) {
                // 刷新即将过期的缓存
                for (String key : keysToRefresh) {
                    redisUtils.strings().expire(key, 3600, java.util.concurrent.TimeUnit.SECONDS);
                }
                logUtils.business().trace("cache_warmup_refresh", "success", "模式: " + pattern, "刷新数: " + keysToRefresh.size());
                return keysToRefresh.size();
            }

            logUtils.business().trace("cache_warmup_check", "complete", "模式: " + pattern, "现有键数: " + existingKeys.size());
            return existingKeys.size();

        } catch (Exception e) {
            logUtils.exception().business("cache_warmup_task_execute_failed", e, "模式: " + pattern);
            return 0;
        }
    }

    /**
     * 获取预热统计信息
     */
    public WarmUpStats getWarmUpStats() {
        return new WarmUpStats(
                registeredTasks.size(),
                warmUpQueue.size(),
                isWarming.get());
    }

    /**
     * 数据加载器接口
     */
    @FunctionalInterface
    public interface DataLoader {
        Map<String, Object> loadData(String pattern);
    }

    /**
     * 预热任务类
     */
    private static class WarmUpTask implements Comparable<WarmUpTask> {
        private final String pattern;
        private final WarmUpPriority priority;
        private final long createTime;
        private DataLoader dataLoader;

        public WarmUpTask(String pattern, WarmUpPriority priority, long createTime) {
            this.pattern = pattern;
            this.priority = priority;
            this.createTime = createTime;
        }

        @Override
        public int compareTo(WarmUpTask other) {
            // 按优先级排序，优先级高的在前
            int priorityCompare = Integer.compare(this.priority.getLevel(), other.priority.getLevel());
            if (priorityCompare != 0) {
                return priorityCompare;
            }
            // 优先级相同时，按创建时间排序
            return Long.compare(this.createTime, other.createTime);
        }

        // Getters and setters
        public String getPattern() {
            return pattern;
        }

        public WarmUpPriority getPriority() {
            return priority;
        }

        public DataLoader getDataLoader() {
            return dataLoader;
        }

        public void setDataLoader(DataLoader dataLoader) {
            this.dataLoader = dataLoader;
        }
    }

    /**
         * 预热统计信息
         */
        public record WarmUpStats(int registeredTasks,int pendingTasks,boolean isWarming) {
    }
}
