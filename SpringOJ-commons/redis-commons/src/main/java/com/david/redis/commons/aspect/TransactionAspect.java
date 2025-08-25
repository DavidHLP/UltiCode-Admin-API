package com.david.redis.commons.aspect;

import com.david.redis.commons.annotation.RedisTransactional;
import com.david.redis.commons.core.RedisTransactionManager;
import com.david.redis.commons.core.TransactionContext;
import com.david.redis.commons.exception.RedisTransactionException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

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

    public TransactionAspect(RedisTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
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

        log.debug("开始处理Redis事务方法: {}", methodName);

        TransactionContext context = null;
        boolean shouldManageTransaction = true;

        try {
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

            return result;

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
     * 创建事务异常信息
     *
     * @param methodName    方法名
     * @param transactionId 事务ID
     * @param cause         原始异常
     * @return 格式化的异常消息
     */
    private String createTransactionErrorMessage(String methodName, String transactionId, Throwable cause) {
        return String.format("Redis事务方法[%s]执行失败，事务ID: %s，原因: %s",
                methodName, transactionId, cause.getMessage());
    }

    /**
     * 记录事务性能指标
     *
     * @param context    事务上下文
     * @param methodName 方法名
     */
    private void recordTransactionMetrics(TransactionContext context, String methodName) {
        if (context != null) {
            long executionTime = context.getExecutionTimeMillis();
            int operationCount = context.getOperations().size();

            log.debug("Redis事务性能指标 - 方法: {}, 事务ID: {}, 执行时间: {}ms, 操作数: {}",
                    methodName, context.getTransactionId(), executionTime, operationCount);

            // 这里可以集成监控系统，如Micrometer
            // meterRegistry.timer("redis.transaction.duration", "method", methodName)
            // .record(executionTime, TimeUnit.MILLISECONDS);
        }
    }
}