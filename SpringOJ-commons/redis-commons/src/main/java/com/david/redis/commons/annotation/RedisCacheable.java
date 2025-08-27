package com.david.redis.commons.annotation;

import com.david.redis.commons.enums.CacheLevel;
import com.david.redis.commons.enums.UpdateStrategy;
import com.david.redis.commons.enums.WarmUpPriority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Redis缓存注解，用于标记需要缓存的方法
 * 支持SpEL表达式动态生成缓存键，支持预热、批量操作、多级缓存等高级功能
 *
 * @author David
 * @since 1.0.0
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

    // ==================== 新增功能属性 ====================

    /**
     * 是否启用缓存预热
     * 启用后会在应用启动或定时任务中预加载缓存数据
     */
    boolean warmUp() default false;

    /**
     * 批量操作阈值
     * 当缓存操作数量达到此阈值时，自动使用批量操作优化性能
     */
    int batchSize() default 10;

    /**
     * 缓存更新策略
     * 定义缓存数据的更新时机和方式
     */
    UpdateStrategy updateStrategy() default UpdateStrategy.WRITE_THROUGH;

    /**
     * 缓存级别
     * 支持多级缓存：L1(本地) + L2(Redis) + L3(数据库)
     */
    CacheLevel[] levels() default {CacheLevel.L2};

    /**
     * 预热优先级
     * 当启用预热时，决定预热的执行顺序
     */
    WarmUpPriority warmUpPriority() default WarmUpPriority.MEDIUM;

    /**
     * 预热延迟时间（毫秒）
     * 应用启动后延迟多长时间开始预热，避免启动时性能影响
     */
    long warmUpDelayMs() default 5000;

    /**
     * 是否启用缓存统计
     * 启用后会收集缓存命中率、响应时间等性能指标
     */
    boolean enableMetrics() default true;

    /**
     * 缓存刷新阈值（0-1之间的小数）
     * 当缓存剩余TTL低于此比例时，触发提前刷新
     * 例如：0.2 表示剩余20%TTL时开始刷新
     */
    double refreshThreshold() default 0.2;
}