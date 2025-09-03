package com.david.commons.redis.lock.annotation;

import com.david.commons.redis.lock.enums.LockType;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁注解
 *
 * <p>用于方法级别的分布式锁控制，支持 SpEL 表达式动态生成锁键
 *
 * @author David
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

    /**
     * 锁键，支持 SpEL 表达式
     *
     * <p>示例： - "user:#{#userId}" - 使用方法参数 - "order:#{#order.id}" - 使用对象属性 - "#{#root.methodName}" -
     * 使用方法名 - "#{T(java.util.UUID).randomUUID().toString()}" - 使用静态方法
     */
    String key();

    /** 锁类型 */
    LockType lockType() default LockType.REENTRANT;

    /** 等待时间 */
    long waitTime() default 10;

    /** 持有时间（租期） */
    long leaseTime() default 30;

    /** 时间单位 */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /** 最大重试次数 */
    int maxRetryAttempts() default 3;

    /** 获取锁失败时的处理策略 */
    LockFailureStrategy failureStrategy() default LockFailureStrategy.EXCEPTION;

    /** 条件表达式，支持 SpEL 当表达式结果为 true 时才应用锁 */
    String condition() default "";

    /** 锁获取失败时的处理策略 */
    enum LockFailureStrategy {
        /** 抛出异常 */
        EXCEPTION,

        /** 返回默认值（null 或基本类型的默认值） */
        RETURN_DEFAULT,

        /** 跳过锁直接执行方法 */
        SKIP_LOCK
    }
}
