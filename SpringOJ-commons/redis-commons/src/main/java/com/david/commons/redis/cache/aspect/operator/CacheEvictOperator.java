package com.david.commons.redis.cache.aspect.operator;

import com.david.commons.redis.RedisUtils;
import com.david.commons.redis.cache.CacheContext;
import com.david.commons.redis.cache.CacheMetadata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 缓存清除操作器
 *
 * <p>
 * 负责缓存的清除操作，支持单个键清除和批量模式清除
 *
 * @author David
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CacheEvictOperator {

    private final RedisUtils redisUtils;
    private final CacheKeyBuilder keyBuilder;

    /**
     * 清除缓存
     *
     * @param metadata 缓存元数据
     * @param context  缓存上下文
     */
    public void evictCache(CacheMetadata metadata, CacheContext context) {
        try {
            String cacheKey = keyBuilder.buildCacheKey(metadata, context, context.getResult());

            // 检查是否为通配符模式（以冒号结尾或含有*和?）
            boolean isWildcardPattern = isWildcardPattern(cacheKey);
            boolean shouldBatchEvict = metadata.allEntries() || isWildcardPattern;

            if (shouldBatchEvict) {
                // 批量删除匹配的键
                String pattern = normalizePattern(cacheKey);
                log.debug("批量驱逐匹配模式的缓存: {}", pattern);
                evictByPattern(pattern, metadata.batchSize());
            } else {
                // 删除单个键
                log.debug("驱逐单个缓存键: {}", cacheKey);
                evictSingleKey(cacheKey);
            }

        } catch (Exception e) {
            log.error("缓存驱逐操作失败，键模式: {}", metadata.key(), e);
            // 缓存清除失败不影响业务逻辑
        }
    }

    /**
     * 检查是否为通配符模式
     * 支持的通配符模式：
     * 1. 以冒号结尾: "user:pageUsers:" -> 匹配 "user:pageUsers:*"
     * 2. 包含*: "user:*:data" -> 直接使用
     * 3. 包含?: "user:?:data" -> 直接使用
     */
    private boolean isWildcardPattern(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }

        // 以冒号结尾且不是空串，视为通配符模式
        if (key.endsWith(":") && key.length() > 1) {
            return true;
        }

        // 包含 Redis 通配符
        return key.contains("*") || key.contains("?");
    }

    /**
     * 标准化通配符模式
     * 以冒号结尾的键自动添加 "*" 后缀
     */
    private String normalizePattern(String key) {
        if (key == null || key.isEmpty()) {
            return key;
        }

        // 如果以冒号结尾且不含有其他通配符，添加 "*"
        if (key.endsWith(":") && !key.contains("*") && !key.contains("?")) {
            return key + "*";
        }

        return key;
    }

    /**
     * 清除单个缓存键
     */
    public void evictSingleKey(String cacheKey) {
        try {
            Boolean deleted = redisUtils.common().delete(cacheKey);
            if (Boolean.TRUE.equals(deleted)) {
                log.debug("成功驱逐缓存键: {}", cacheKey);
            } else {
                log.debug("缓存键不存在或已被删除: {}", cacheKey);
            }
        } catch (Exception e) {
            log.error("单个缓存键驱逐失败: {}", cacheKey, e);
        }
    }

    /**
     * 根据模式批量删除缓存
     */
    public void evictByPattern(String pattern, int batchSize) {
        try {
            // 使用 SCAN 增量扫描并分批删除，避免 KEYS 导致的阻塞
            final int effectiveBatch = batchSize > 0 ? batchSize : 1000;
            final List<String> batch = new ArrayList<>(effectiveBatch);
            final long[] totalDeleted = new long[1];

            log.debug("开始按模式驱逐缓存: {}, 批次大小: {}", pattern, effectiveBatch);

            redisUtils
                    .common()
                    .scan(
                            pattern,
                            effectiveBatch,
                            key -> {
                                batch.add(key);
                                if (batch.size() >= effectiveBatch) {
                                    deleteBatch(batch, totalDeleted);
                                }
                            });

            // 清理残留批次
            if (!batch.isEmpty()) {
                deleteBatch(batch, totalDeleted);
            }

            log.debug("模式驱逐完成: '{}', 删除了 {} 个键", pattern, totalDeleted[0]);
        } catch (Exception e) {
            log.error("按模式驱逐键失败: {}", pattern, e);
        }
    }

    /**
     * 删除批次中的所有键
     */
    private void deleteBatch(List<String> batch, long[] totalDeleted) {
        try {
            Long deleted = redisUtils.common().delete(batch);
            totalDeleted[0] += deleted != null ? deleted : 0;
            log.debug("批量删除 {} 个键, 实际删除: {}", batch.size(), deleted);
            batch.clear();
        } catch (Exception e) {
            log.error("批量删除 {} 个键失败", batch.size(), e);
            batch.clear();
        }
    }

    /**
     * 检查缓存键是否存在
     */
    public boolean exists(String cacheKey) {
        try {
            return redisUtils.common().hasKey(cacheKey);
        } catch (Exception e) {
            log.error("检查缓存键存在性失败: {}", cacheKey, e);
            return false;
        }
    }

    /**
     * 获取匹配模式的键数量
     */
    public long countKeysByPattern(String pattern) {
        try {
            final long[] count = new long[1];
            redisUtils.common().scan(pattern, 1000, key -> count[0]++);
            return count[0];
        } catch (Exception e) {
            log.error("按模式统计键数量失败: {}", pattern, e);
            return 0;
        }
    }
}
