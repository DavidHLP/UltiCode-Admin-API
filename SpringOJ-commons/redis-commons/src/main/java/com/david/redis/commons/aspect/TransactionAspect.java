package com.david.redis.commons.aspect;

import com.david.redis.commons.annotation.RedisTransactional;
import com.david.redis.commons.core.transaction.RedisTransactionManager;
import com.david.redis.commons.core.transaction.TransactionContext;
import com.david.redis.commons.exception.RedisTransactionException;
import com.david.redis.commons.core.RedisUtils;
import com.david.redis.commons.core.lock.interfaces.RedisLock;
import com.david.redis.commons.core.cache.CacheKeyGenerator;
import com.david.redis.commons.enums.TimeoutStrategy;
import com.david.redis.commons.monitor.CacheMetricsCollector;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * Redis事务切面
 *
 * <p>
 * 处理@RedisTransactional注解，管理Redis事务的生命周期，
 * 包括事务的开始、提交、回滚和异常处理。
 * </p>
 *
 * @author David
 */
@Slf4j
@Aspect
@Component
@Order(100) // 确保在其他切面之前执行
public class TransactionAspect {

    private final RedisTransactionManager transactionManager;
    private final RedisUtils redisUtils;
    private final CacheKeyGenerator cacheKeyGenerator;
    private final CacheMetricsCollector metricsCollector;

    public TransactionAspect(RedisTransactionManager transactionManager,
            RedisUtils redisUtils,
            CacheKeyGenerator cacheKeyGenerator,
            CacheMetricsCollector metricsCollector) {
        this.transactionManager = transactionManager;
        this.redisUtils = redisUtils;
        this.cacheKeyGenerator = cacheKeyGenerator;
        this.metricsCollector = metricsCollector;
    }

    /**
     * 处理@RedisTransactional注解的方法
     *
     * @param joinPoint          连接点
     * @param redisTransactional 事务注解
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("@annotation(redisTransactional)")
    public Object handleTransaction(ProceedingJoinPoint joinPoint, RedisTransactional redisTransactional)
            throws Throwable {

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        long startTime = System.currentTimeMillis();

        log.debug("开始处理Redis事务方法: {} - 锁策略: {}, 重试策略: {}, 超时策略: {}",
                methodName, redisTransactional.lockStrategy(),
                redisTransactional.retryPolicy(), redisTransactional.timeoutStrategy());

        TransactionContext context = null;

        // 解析锁参数
        String lockKeyExp = redisTransactional.lockKey();
        String resolvedLockKey = null;
        if (StringUtils.hasText(lockKeyExp)) {
            resolvedLockKey = cacheKeyGenerator.resolveSpELExpression(lockKeyExp, method, joinPoint.getArgs());
            long timeoutMs = redisTransactional.timeout();
            long leaseMs = redisTransactional.lockLeaseTimeMs();
            if (timeoutMs > 0 && leaseMs > 0 && leaseMs < timeoutMs) {
                log.warn("锁租约时间({}ms)小于事务超时({}ms)，可能导致锁提前释放", leaseMs, timeoutMs);
            }
        }

        try {
            if (StringUtils.hasText(resolvedLockKey)) {
                // 锁+事务融合 - 使用新的 RedisUtils 锁操作
                try (RedisLock ignored = redisUtils.locks().tryLock(
                        resolvedLockKey,
                        Duration.ofMillis(Math.max(0, redisTransactional.lockWaitTimeMs())),
                        Duration.ofMillis(Math.max(0, redisTransactional.lockLeaseTimeMs())))) {

                    // 检查超时配置
                    validateTimeout(redisTransactional.timeout());

                    // 开始事务
                    context = transactionManager.beginTransaction(redisTransactional);
                    log.debug("Redis事务已开始(带锁): {} | 锁键: {}", context.getTransactionId(), resolvedLockKey);

                    // 记录方法调用
                    transactionManager.addOperation("METHOD_CALL: " + methodName);

                    // 执行目标方法
                    Object result = joinPoint.proceed();

                    // 检查事务超时
                    if (context.isTimeout()) {
                        throw RedisTransactionException.transactionTimeout(
                                context.getTransactionId(), redisTransactional.timeout());
                    }

                    // 提交事务
                    transactionManager.commitTransaction(context);
                    log.debug("Redis事务提交成功(带锁): {}", context.getTransactionId());

                    // 记录事务性能指标
                    recordTransactionMetrics(context, methodName, startTime, redisTransactional);

                    return result;
                }
            } else {
                // 仅事务
                // 检查超时配置
                validateTimeout(redisTransactional.timeout());

                // 开始事务
                context = transactionManager.beginTransaction(redisTransactional);
                log.debug("Redis事务已开始: {}", context.getTransactionId());

                // 记录方法调用
                transactionManager.addOperation("METHOD_CALL: " + methodName);

                // 执行目标方法
                Object result = joinPoint.proceed();

                // 检查事务超时
                if (context.isTimeout()) {
                    throw RedisTransactionException.transactionTimeout(
                            context.getTransactionId(), redisTransactional.timeout());
                }

                // 提交事务
                transactionManager.commitTransaction(context);
                log.debug("Redis事务提交成功: {}", context.getTransactionId());

                // 记录事务性能指标
                recordTransactionMetrics(context, methodName, startTime, redisTransactional);

                return result;
            }
        } catch (Throwable throwable) {
            log.error("Redis事务方法执行异常: {}, 事务ID: {}",
                    methodName, context != null ? context.getTransactionId() : "unknown", throwable);

            // 判断是否需要回滚
            if (context != null && shouldRollback(throwable, redisTransactional)) {
                try {
                    transactionManager.rollbackTransaction(context);
                    log.debug("Redis事务回滚成功: {}", context.getTransactionId());
                } catch (Exception rollbackException) {
                    log.error("Redis事务回滚失败: {}", context.getTransactionId(), rollbackException);
                    // 将回滚异常作为抑制异常添加到原始异常中
                    throwable.addSuppressed(rollbackException);
                }
            }

            // 记录事务性能指标（即使失败也要记录）
            if (context != null) {
                recordTransactionMetrics(context, methodName, startTime, redisTransactional);
            }

            // 重新抛出原始异常
            throw throwable;
        }
    }

    /**
     * 判断是否应该回滚事务
     *
     * @param throwable  抛出的异常
     * @param annotation 事务注解
     * @return 如果应该回滚返回true
     */
    private boolean shouldRollback(Throwable throwable, RedisTransactional annotation) {
        // 检查noRollbackFor配置
        Class<? extends Throwable>[] noRollbackFor = annotation.noRollbackFor();
        if (noRollbackFor.length > 0) {
            for (Class<? extends Throwable> exceptionClass : noRollbackFor) {
                if (exceptionClass.isAssignableFrom(throwable.getClass())) {
                    log.debug("异常{}在noRollbackFor列表中，不进行回滚", throwable.getClass().getSimpleName());
                    return false;
                }
            }
        }

        // 检查rollbackFor配置
        Class<? extends Throwable>[] rollbackFor = annotation.rollbackFor();
        if (rollbackFor.length > 0) {
            for (Class<? extends Throwable> exceptionClass : rollbackFor) {
                if (exceptionClass.isAssignableFrom(throwable.getClass())) {
                    log.debug("异常{}在rollbackFor列表中，进行回滚", throwable.getClass().getSimpleName());
                    return true;
                }
            }
            // 如果指定了rollbackFor但异常不在列表中，则不回滚
            log.debug("异常{}不在rollbackFor列表中，不进行回滚", throwable.getClass().getSimpleName());
            return false;
        }

        // 默认行为：RuntimeException和Error会触发回滚
        boolean shouldRollback = (throwable instanceof RuntimeException) || (throwable instanceof Error);

        if (shouldRollback) {
            log.debug("异常{}是RuntimeException或Error，进行回滚", throwable.getClass().getSimpleName());
        } else {
            log.debug("异常{}是受检异常，不进行回滚", throwable.getClass().getSimpleName());
        }

        return shouldRollback;
    }

    /**
     * 验证超时配置
     *
     * @param timeout 超时时间
     */
    private void validateTimeout(long timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("事务超时时间不能为负数: " + timeout);
        }

        if (timeout > 0 && timeout < 1000) {
            log.warn("事务超时时间过短: {}ms，建议至少设置为1000ms", timeout);
        }

        if (timeout > 300000) { // 5分钟
            log.warn("事务超时时间过长: {}ms，建议不超过300000ms(5分钟)", timeout);
        }
    }

    /**
     * 记录事务性能指标
     *
     * @param context    事务上下文
     * @param methodName 方法名
     * @param startTime  开始时间
     * @param annotation 事务注解
     */
    private void recordTransactionMetrics(TransactionContext context, String methodName,
            long startTime, RedisTransactional annotation) {
        if (context != null) {
            long executionTime = System.currentTimeMillis() - startTime;
            int operationCount = context.getOperations().size();
            String status = context.getStatus().toString();

            log.debug("Redis事务性能指标 - 方法: {}, 事务ID: {}, 执行时间: {}ms, 操作数: {}, 状态: {}",
                    methodName, context.getTransactionId(), executionTime, operationCount, status);

            // 使用新的性能监控组件
            if (annotation.enableMetrics()) {
                String operation = "TRANSACTION_" + (context.isCommitted() ? "COMMIT" : "ROLLBACK");
                if (context.isCommitted()) {
                    metricsCollector.recordSet(methodName, executionTime);
                } else {
                    metricsCollector.recordError(operation, executionTime);
                }
            }

            // 记录慢事务警告
            if (executionTime > 1000) { // 超过1秒的事务
                log.warn("检测到慢Redis事务 - 方法: {}, 事务ID: {}, 执行时间: {}ms, 操作数: {}, 锁策略: {}, 重试策略: {}",
                        methodName, context.getTransactionId(), executionTime, operationCount,
                        annotation.lockStrategy(), annotation.retryPolicy());
            }

            // 检查死锁检测
            if (annotation.deadlockDetection() && executionTime > annotation.timeout()) {
                log.warn("事务执行时间超过配置超时 - 方法: {}, 事务ID: {}, 执行时间: {}ms, 配置超时: {}ms, 可能存在死锁",
                        methodName, context.getTransactionId(), executionTime, annotation.timeout());
            }

            // 处理超时策略
            if (executionTime > annotation.timeout() && annotation.timeout() > 0) {
                handleTimeoutStrategy(context, annotation.timeoutStrategy(), methodName);
            }
        }
    }

    /**
     * 处理超时策略
     */
    private void handleTimeoutStrategy(TransactionContext context, TimeoutStrategy strategy, String methodName) {
        switch (strategy) {
            case ROLLBACK:
                log.info("执行超时策略 ROLLBACK - 方法: {}, 事务ID: {}", methodName, context.getTransactionId());
                // 超时回滚逻辑已在主流程中处理
                break;
            case FORCE_COMMIT:
                log.warn("执行超时策略 FORCE_COMMIT - 方法: {}, 事务ID: {}", methodName, context.getTransactionId());
                // 强制提交，忽略超时
                break;
            case THROW_EXCEPTION:
                log.error("执行超时策略 THROW_EXCEPTION - 方法: {}, 事务ID: {}", methodName, context.getTransactionId());
                // 异常已在主流程中抛出
                break;
            case EXTEND_TIMEOUT:
                log.info("执行超时策略 EXTEND_TIMEOUT - 方法: {}, 事务ID: {}", methodName, context.getTransactionId());
                // 延长超时时间的逻辑
                break;
            default:
                log.warn("未知的超时策略: {} - 方法: {}, 事务ID: {}", strategy, methodName, context.getTransactionId());
        }
    }
}