package com.david.redis.commons.annotation;

import com.david.redis.commons.enums.LockStrategy;
import com.david.redis.commons.enums.RetryPolicy;
import com.david.redis.commons.enums.TimeoutStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Redis事务注解，用于标记需要在Redis事务中执行的方法
 *
 * <p>
 * 该注解会将方法中的所有Redis操作包装在一个事务中执行，
 * 确保操作的原子性。支持嵌套事务处理和异常回滚配置。
 * </p>
 *
 * <p>
 * 使用示例：
 * 
 * <pre>
 * {@code
 * @RedisTransactional
 * public void updateUserData(String userId, UserData data) {
 *     redisUtils.set("user:" + userId, data);
 *     redisUtils.hSet("user:index", userId, data.getName());
 * }
 *
 * @RedisTransactional(rollbackFor = { BusinessException.class })
 * public void complexOperation() {
 *     // 复杂的Redis操作
 * }
 * }
 * </pre>
 * </p>
 *
 * <p>
 * 可选：通过 lockKey/wait/lease 配置融合分布式锁与事务，lockKey 支持 SpEL。
 * 当 lockKey 非空时，切面会先获取分布式锁，再开启事务，方法结束后释放锁。
 * </p>
 *
 * @author David
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisTransactional {

    /**
     * 是否为只读事务
     * 只读事务不会执行MULTI/EXEC，仅用于标识和监控
     * 默认为false
     */
    boolean readOnly() default false;

    /**
     * 指定哪些异常类型会触发事务回滚
     * 默认所有RuntimeException和Error都会触发回滚
     * 可以指定特定的异常类型来精确控制回滚行为
     */
    Class<? extends Throwable>[] rollbackFor() default {};

    /**
     * 指定哪些异常类型不会触发事务回滚
     * 即使是RuntimeException，如果在此列表中也不会回滚
     */
    Class<? extends Throwable>[] noRollbackFor() default {};

    /**
     * 事务超时时间（毫秒）
     * 超过此时间未完成的事务将被强制回滚
     * 默认30秒，设置为0表示不限制超时
     */
    long timeout() default 30000;

    /**
     * 事务传播行为
     * 定义当前方法如何参与现有事务
     */
    Propagation propagation() default Propagation.REQUIRED;

    /**
     * 事务隔离级别
     * Redis本身不支持传统的事务隔离级别，
     * 此属性主要用于与其他事务管理器的兼容性
     */
    Isolation isolation() default Isolation.DEFAULT;

    /**
     * 事务标签，用于监控和调试
     * 可以为事务指定一个有意义的名称
     */
    String label() default "";

    /**
     * 分布式锁键（支持SpEL）。为空表示不使用锁
     * 示例："'order:' + #orderId" 或 "T.concat('user:', #userId)"
     */
    String lockKey() default "";

    /**
     * 分布式锁等待时间（毫秒）。仅当 lockKey 非空时生效
     */
    long lockWaitTimeMs() default 5000;

    /**
     * 分布式锁租约时间（毫秒）。仅当 lockKey 非空时生效
     */
    long lockLeaseTimeMs() default 30000;

    // ==================== 新增功能属性 ====================

    /**
     * 锁策略
     * 定义获取锁的策略和类型
     */
    LockStrategy lockStrategy() default LockStrategy.AUTO;

    /**
     * 重试策略
     * 定义事务失败时的重试行为
     */
    RetryPolicy retryPolicy() default RetryPolicy.DEFAULT;

    /**
     * 超时处理策略
     * 定义事务超时时的处理方式
     */
    TimeoutStrategy timeoutStrategy() default TimeoutStrategy.ROLLBACK;

    /**
     * 是否启用死锁检测
     * 启用后会自动检测和解决死锁问题
     */
    boolean deadlockDetection() default true;

    /**
     * 事务优先级
     * 高优先级事务在资源竞争时优先执行
     */
    int priority() default 0;

    /**
     * 是否启用事务监控
     * 启用后会收集事务执行时间、成功率等指标
     */
    boolean enableMetrics() default true;

    /**
     * 事务传播行为枚举
     */
    enum Propagation {
        /**
         * 支持当前事务，如果不存在则创建新事务
         * 这是默认行为
         */
        REQUIRED,

        /**
         * 支持当前事务，如果不存在则以非事务方式执行
         */
        SUPPORTS,

        /**
         * 必须在现有事务中执行，如果不存在则抛出异常
         */
        MANDATORY,

        /**
         * 总是创建新事务，如果存在当前事务则挂起
         */
        REQUIRES_NEW,

        /**
         * 不支持事务，如果存在当前事务则挂起
         */
        NOT_SUPPORTED,

        /**
         * 不支持事务，如果存在当前事务则抛出异常
         */
        NEVER,

        /**
         * 如果存在当前事务，则在嵌套事务中执行
         * 否则行为类似REQUIRED
         */
        NESTED
    }

    /**
     * 事务隔离级别枚举
     */
    enum Isolation {
        /**
         * 使用默认隔离级别
         */
        DEFAULT,

        /**
         * 读未提交
         */
        READ_UNCOMMITTED,

        /**
         * 读已提交
         */
        READ_COMMITTED,

        /**
         * 可重复读
         */
        REPEATABLE_READ,

        /**
         * 串行化
         */
        SERIALIZABLE
    }
}