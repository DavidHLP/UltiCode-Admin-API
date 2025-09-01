package com.david.commons.redis.cache;

import lombok.Getter;

/**
 * 缓存同步策略枚举
 *
 * @author David
 */
@Getter
public enum CacheSyncPolicy {

    /**
     * 同步执行 - 缓存操作与业务方法同步执行
     */
    SYNC("sync"),

    /**
     * 异步执行 - 缓存操作异步执行，不阻塞业务方法
     */
    ASYNC("async"),

    /**
     * 最佳努力 - 尽力执行缓存操作，失败时不影响业务
     */
    BEST_EFFORT("bestEffort");

    private final String code;

    CacheSyncPolicy(String code) {
        this.code = code;
    }

	/**
     * 根据代码获取同步策略
     */
    public static CacheSyncPolicy fromCode(String code) {
        for (CacheSyncPolicy policy : values()) {
            if (policy.code.equals(code)) {
                return policy;
            }
        }
        throw new IllegalArgumentException("Unknown cache sync policy: " + code);
    }
}