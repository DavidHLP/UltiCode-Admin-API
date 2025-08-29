package com.david.redis.commons.manager;

import com.david.log.commons.core.LogUtils;
import com.david.redis.commons.core.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 批量操作管理器
 * 提供批量缓存读写、管道操作等高性能功能
 * 
 * @author David
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class BatchOperationManager {

    private final RedisUtils redisUtils;
    private final LogUtils logUtils;
    private final Map<String, List<String>> batchBuffer = new ConcurrentHashMap<>();
    private final Map<String, Long> lastFlushTime = new ConcurrentHashMap<>();

    // 批量操作配置
    private static final int DEFAULT_BATCH_SIZE = 10;
    private static final long FLUSH_INTERVAL_MS = 1000; // 1秒

    /**
     * 批量获取缓存
     * 
     * @param keys 缓存键列表
     * @param type 值类型
     * @return 缓存结果映射
     */
    public <T> Map<String, T> batchGet(List<String> keys, Class<T> type) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, T> result = new HashMap<>();

        try {
            // 直接使用提供的键进行批量获取，不再生成额外的批量键
            List<Object> values = redisUtils.strings().multiGet(keys);

            // 组装结果
            for (int i = 0; i < keys.size() && i < values.size(); i++) {
                Object value = values.get(i);
                if (value != null) {
                    try {
                        @SuppressWarnings("unchecked")
                        T typedValue = (T) value;
                        result.put(keys.get(i), typedValue);
                    } catch (ClassCastException e) {
                        logUtils.exception().business("batch_get_type_cast_failed", e, 
                                "键: " + keys.get(i), "期望类型: " + type.getSimpleName(), "实际类型: " + value.getClass().getSimpleName());
                    }
                }
            }

            logUtils.performance().timing("batch_get_complete", 0, "请求键数: " + keys.size(), "命中数: " + result.size());

        } catch (Exception e) {
            logUtils.exception().system("batch_get_failed", e, "high");
        }

        return result;
    }

    /**
     * 批量设置缓存
     * 
     * @param keyValues 键值对映射
     * @param ttl       过期时间（秒）
     */
    public void batchSet(Map<String, Object> keyValues, long ttl) {
        if (keyValues == null || keyValues.isEmpty()) {
            return;
        }

        try {
            // 分批处理
            int batchSize = DEFAULT_BATCH_SIZE;
            List<Map.Entry<String, Object>> entries = new ArrayList<>(keyValues.entrySet());

            for (int i = 0; i < entries.size(); i += batchSize) {
                int end = Math.min(i + batchSize, entries.size());
                Map<String, Object> batch = new HashMap<>();

                for (int j = i; j < end; j++) {
                    Map.Entry<String, Object> entry = entries.get(j);
                    batch.put(entry.getKey(), entry.getValue());
                }

                // 使用 mset 批量设置
                redisUtils.strings().multiSet(batch);

                // 批量设置过期时间
                if (ttl > 0) {
                    for (String key : batch.keySet()) {
                        redisUtils.strings().expire(key, ttl, TimeUnit.SECONDS);
                    }
                }
            }

            logUtils.performance().timing("batch_set_complete", 0, "键数: " + keyValues.size(), "TTL: " + ttl + "秒");

        } catch (Exception e) {
            logUtils.exception().system("batch_set_failed", e, "high");
        }
    }

    /**
     * 批量删除缓存
     * 
     * @param keys 要删除的键列表
     * @return 删除的键数量
     */
    public long batchDelete(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0;
        }

        try {
            Long deleted = redisUtils.strings().delete(keys.toArray(new String[0]));
            long deletedCount = deleted != null ? deleted : 0;

            logUtils.performance().timing("batch_delete_complete", 0, "请求删除: " + keys.size(), "实际删除: " + deletedCount);
            return deletedCount;

        } catch (Exception e) {
            logUtils.exception().system("batch_delete_failed", e, "high");
            return 0;
        }
    }

    /**
     * 异步批量操作
     * 将操作加入批量缓冲区，达到阈值或超时时自动执行
     * 
     * @param operation 操作类型
     * @param key       缓存键
     */
    public void addToBatch(String operation, String key) {
        String batchKey = operation;

        batchBuffer.computeIfAbsent(batchKey, k -> new ArrayList<>()).add(key);

        // 检查是否需要立即刷新
        if (shouldFlush(batchKey)) {
            flushBatch(batchKey);
        }
    }

    /**
     * 检查是否应该刷新批次
     */
    private boolean shouldFlush(String batchKey) {
        List<String> batch = batchBuffer.get(batchKey);
        if (batch == null) {
            return false;
        }

        // 检查批次大小
        if (batch.size() >= DEFAULT_BATCH_SIZE) {
            return true;
        }

        // 检查时间间隔
        long lastFlush = lastFlushTime.getOrDefault(batchKey, 0L);
        return System.currentTimeMillis() - lastFlush > FLUSH_INTERVAL_MS;
    }

    /**
     * 刷新批次操作
     */
    private void flushBatch(String batchKey) {
        List<String> batch = batchBuffer.remove(batchKey);
        if (batch == null || batch.isEmpty()) {
            return;
        }

        lastFlushTime.put(batchKey, System.currentTimeMillis());

        // 异步执行批量操作
        CompletableFuture.runAsync(() -> {
            try {
                if ("DELETE".equals(batchKey)) {
                    batchDelete(batch);
                }
                // 可以添加其他批量操作类型

            } catch (Exception e) {
                logUtils.exception().system("batch_operation_failed", e, "high");
            }
        });
    }

    /**
     * 强制刷新所有待处理的批次
     */
    public void flushAll() {
        Set<String> batchKeys = new HashSet<>(batchBuffer.keySet());
        for (String batchKey : batchKeys) {
            flushBatch(batchKey);
        }
    }
}
