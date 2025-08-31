package com.david.commons.redis.cache;

/**
 * 缓存淘汰策略枚举
 *
 * @author David
 */
public enum CacheEvictionPolicy {

    /**
     * 所有键 - 清除所有匹配的缓存键
     */
    ALL_ENTRIES("allEntries"),

    /**
     * 单个键 - 只清除指定的缓存键
     */
    SINGLE_KEY("singleKey"),

    /**
     * 模式匹配 - 清除匹配模式的缓存键
     */
    PATTERN_MATCH("patternMatch"),

    /**
     * 条件清除 - 根据条件清除缓存键
     */
    CONDITIONAL("conditional");

    private final String code;

    CacheEvictionPolicy(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * 根据代码获取淘汰策略
     */
    public static CacheEvictionPolicy fromCode(String code) {
        for (CacheEvictionPolicy policy : values()) {
            if (policy.code.equals(code)) {
                return policy;
            }
        }
        throw new IllegalArgumentException("Unknown cache eviction policy: " + code);
    }
}