package com.david.redis.commons.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 缓存清理工具
 * 用于清理损坏或格式不兼容的缓存数据
 * 
 * @author David
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheCleaner {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 清理指定模式的缓存
     * 
     * @param pattern 缓存键模式，支持通配符
     * @return 清理的缓存数量
     */
    public long cleanCacheByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys == null || keys.isEmpty()) {
                log.info("没有找到匹配模式 {} 的缓存", pattern);
                return 0;
            }

            Long deleted = redisTemplate.delete(keys);
            long deletedCount = deleted != null ? deleted : 0;

            log.info("清理缓存完成 - 模式: {}, 清理数量: {}", pattern, deletedCount);
            return deletedCount;

        } catch (Exception e) {
            log.error("清理缓存失败 - 模式: {}", pattern, e);
            return 0;
        }
    }

    /**
     * 清理单个缓存
     * 
     * @param key 缓存键
     * @return 是否清理成功
     */
    public boolean cleanCache(String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            boolean success = deleted != null && deleted;

            if (success) {
                log.info("清理缓存成功 - 键: {}", key);
            } else {
                log.warn("缓存不存在或清理失败 - 键: {}", key);
            }

            return success;

        } catch (Exception e) {
            log.error("清理缓存失败 - 键: {}", key, e);
            return false;
        }
    }

    /**
     * 清理所有 submission calendar 相关的缓存
     * 这是一个临时方法，用于清理当前损坏的缓存
     */
    public void cleanSubmissionCalendarCache() {
        log.info("开始清理 submission calendar 相关缓存...");

        // 清理所有 getSubmissionCalendar 相关的缓存
        long cleaned = cleanCacheByPattern("springoj:cache:solution:getSubmissionCalendar:*");

        log.info("submission calendar 缓存清理完成，共清理 {} 个缓存", cleaned);
    }
}
