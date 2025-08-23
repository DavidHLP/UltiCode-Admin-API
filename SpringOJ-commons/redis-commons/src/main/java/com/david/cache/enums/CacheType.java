package com.david.cache.enums;

/**
 * 缓存操作类型
 */
public enum CacheType {
    /** 读缓存，不存在则执行方法体并缓存返回值 */
    READ,
    /** 写缓存，执行方法体并将返回值写入/更新缓存 */
    WRITE,
    /** 删除缓存，执行方法体后删除缓存 */
    DELETE
}
