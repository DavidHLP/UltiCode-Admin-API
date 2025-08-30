package com.david.redis.commons.aspect.chain;

/**
 * 切面类型枚举
 *
 * @author David
 */
public enum AspectType {

    /** 缓存切面 */
    CACHE,

    /** 事务切面 */
    TRANSACTION,

    /** 缓存驱逐切面 */
    CACHE_EVICT,

    /** 通用切面 */
    GENERAL
}
