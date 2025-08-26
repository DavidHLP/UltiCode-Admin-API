package com.david.redis.commons.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Redis缓存注解，用于标记需要缓存的方法
 * 支持SpEL表达式动态生成缓存键
 *
 * @author David
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisCacheable {

    /**
     * 缓存键的SpEL表达式
     * 支持方法参数引用，如：'user:' + #userId
     * 支持方法名引用：#methodName
     * 支持类名引用：#className
     */
    String key();

    /**
     * 缓存过期时间（秒）
     * 默认1小时
     */
    long ttl() default 3600;

    /**
     * 缓存条件的SpEL表达式
     * 只有当条件为true时才进行缓存
     * 例如：#result != null
     */
    String condition() default "";

    /**
     * 缓存值的类型
     * 用于反序列化时的类型转换
     */
    Class<?> type() default Object.class;

    /**
     * 是否缓存null值
     * 默认不缓存null值
     */
    boolean cacheNullValues() default false;

    /**
     * 缓存键前缀
     * 如果为空则使用全局配置的前缀
     */
    String keyPrefix() default "";
}