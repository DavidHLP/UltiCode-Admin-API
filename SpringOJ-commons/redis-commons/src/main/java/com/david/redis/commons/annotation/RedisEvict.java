package com.david.redis.commons.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Redis缓存驱逐注解，用于标记需要清除缓存的方法
 * 支持多键驱逐和SpEL表达式
 *
 * @author David
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
}