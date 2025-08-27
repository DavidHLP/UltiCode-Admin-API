package com.david.redis.commons.core.cache.utils;

import com.david.redis.commons.core.RedisUtils;
import com.david.redis.commons.properties.RedisCommonsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * 缓存键工具类，提供键的格式化和管理功能
 *
 * @author David
 */
@Component
public class CacheKeyUtils {

    private final RedisCommonsProperties properties;
    private final RedisUtils redisUtils;

    @Autowired
    public CacheKeyUtils(RedisCommonsProperties properties, RedisUtils redisUtils) {
        this.properties = properties;
        this.redisUtils = redisUtils;
    }

    /**
     * 格式化缓存键，添加前缀
     *
     * @param key          原始键
     * @param customPrefix 自定义前缀
     * @return 格式化后的键
     */
    public String formatKey(String key, String customPrefix) {
        String prefix = StringUtils.hasText(customPrefix) ? customPrefix : properties.getCache().getKeyPrefix();

        if (StringUtils.hasText(prefix)) {
            return prefix + key;
        }

        return key;
    }

    /**
     * 获取所有匹配前缀的键
     *
     * @param keyPrefix 键前缀
     * @return 匹配的键集合
     */
    public Set<String> getKeysByPrefix(String keyPrefix) {
        String pattern = keyPrefix + "*";
        Set<String> keys = redisUtils.strings().scanKeys(pattern);
        if (keys.isEmpty()) {
            // Fallback to KEYS in case SCAN yields nothing due to server-side sampling
            keys = redisUtils.strings().keys(pattern);
        }
        return keys;
    }

    /**
     * 批量删除匹配前缀的键
     *
     * @param keyPrefix 键前缀
     * @return 删除的键数量
     */
    public long deleteKeysByPrefix(String keyPrefix) {
        Set<String> keys = getKeysByPrefix(keyPrefix);
        if (keys.isEmpty()) {
            return 0;
        }

        return redisUtils.strings().delete(keys.toArray(new String[0]));
    }

    /**
     * 验证缓存键的有效性
     *
     * @param key 缓存键
     * @return 是否有效
     */
    public boolean isValidKey(String key) {
        if (!StringUtils.hasText(key)) {
            return false;
        }

        // 检查键长度限制
        if (key.length() > 512) {
            return false;
        }

        // 检查是否包含非法字符
        return !key.contains(" ") && !key.contains("\n") && !key.contains("\r");
    }

    /**
     * 清理缓存键，移除非法字符
     *
     * @param key 原始键
     * @return 清理后的键
     */
    public String sanitizeKey(String key) {
        if (!StringUtils.hasText(key)) {
            return "";
        }

        return key.replaceAll("[\\s\\n\\r]", "_")
                .substring(0, Math.min(key.length(), 512));
    }
}