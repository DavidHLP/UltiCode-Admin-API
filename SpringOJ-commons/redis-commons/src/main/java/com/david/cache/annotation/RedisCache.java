package com.david.cache.annotation;

import com.david.cache.enums.CacheType;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 通用 Redis 缓存注解（方法级）
 *
 * <p>特性：
 * - 支持 READ / WRITE / DELETE 三种缓存操作类型；
 * - key 由 keyPrefix + ":" + SpEL 表达式生成；
 * - 默认启用分布式锁以避免缓存击穿和并发写入；
 * - 支持可选的 null 值缓存，用以防止缓存穿透（默认关闭）。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisCache {

    /** 缓存键前缀（命名空间隔离） */
    String keyPrefix() default "";

    /** 使用 SpEL 的 key 表达式，例如："#id"、"#user.id" */
    String key();

    /** 过期时间，默认 5（单位见 unit） */
    long timeout() default 5L;

    /** 时间单位，默认分钟 */
    TimeUnit unit() default TimeUnit.MINUTES;

    /** 缓存操作类型 */
    CacheType type() default CacheType.READ;

    /** 是否启用分布式锁（仅在 READ / WRITE 时生效） */
    boolean useLock() default true;

    /** 是否缓存 null 值以防止穿透（仅在 READ/WRITE 时生效），默认不缓存 */
    boolean cacheNull() default false;
}
