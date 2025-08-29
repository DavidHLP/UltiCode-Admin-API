package com.david.redis.commons.annotation;

import com.david.redis.commons.enums.EvictTiming;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Redis缓存驱逐注解，用于标记需要清除缓存的方法
 * 支持多键驱逐、延迟删除、级联删除等高级功能
 *
 * @author David
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisEvict {

    /**
     * 要驱逐的缓存键的SpEL表达式数组
     * 支持多个键的同时驱逐
     * 例如：{"'user:' + #userId", "'profile:' + #userId"}
     */
    String[] keys() default {};

    /**
     * 是否驱逐所有缓存条目
     * 当为true时，会清除所有以keyPrefix开头的缓存
     */
    boolean allEntries() default false;

    /**
     * 驱逐条件的SpEL表达式
     * 只有当条件为true时才进行缓存驱逐
     * 例如：#result.success == true
     */
    String condition() default "";

    /**
     * 缓存键前缀
     * 当allEntries为true时，会清除所有以此前缀开头的缓存
     * 如果为空则使用全局配置的前缀
     */
    String keyPrefix() default "";

    /**
     * 是否在方法执行前驱逐缓存
     * 默认在方法执行后驱逐
     */
    boolean beforeInvocation() default false;

    /**
     * 延迟删除时间（毫秒）
     * 延迟指定时间后删除缓存，避免缓存雪崩
     */
    long delayMs() default 0;

    /**
     * 是否启用级联删除
     * 删除缓存时同时清理相关联的缓存数据
     */
    boolean cascade() default false;

    /**
     * 级联删除模式数组
     * 当cascade为true时，按照这些模式删除相关缓存
     */
    String[] cascadePatterns() default {};

    /**
     * 删除时机策略
     */
    EvictTiming timing() default EvictTiming.IMMEDIATE;

    /**
     * 是否软删除
     * 软删除只标记删除而不物理删除，便于数据恢复
     */
    boolean softDelete() default false;
}