package com.david.redis.commons.aspect.chain.transaction;

import com.david.log.commons.core.LogUtils;
import com.david.redis.commons.annotation.RedisTransactional;
import com.david.redis.commons.aspect.chain.AbstractAspectHandler;
import com.david.redis.commons.aspect.chain.AspectChain;
import com.david.redis.commons.aspect.chain.AspectContext;
import com.david.redis.commons.aspect.chain.AspectType;

import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 事务配置验证处理器
 *
 * <p>负责验证事务配置的合法性，如超时时间、锁配置等。
 *
 * @author David
 */
@Component
public class TransactionValidationHandler extends AbstractAspectHandler {

    public static final String TRANSACTION_CONFIG_ATTR = "transaction.config";
    public static final String LOCK_KEY_ATTR = "transaction.lock.key";

    public TransactionValidationHandler(LogUtils logUtils) {
        super(logUtils);
    }

    @Override
    protected Set<AspectType> getSupportedAspectTypes() {
        return Set.of(AspectType.TRANSACTION);
    }

    @Override
    public int getOrder() {
        return 5; // 最先执行的验证
    }

    @Override
    public Object handle(AspectContext context, AspectChain chain) throws Throwable {
        if (context.getMethod() == null) {
            return chain.proceed(context); // 虚拟上下文直接跳过
        }
        
        try {
            RedisTransactional annotation =
                    context.getMethod().getAnnotation(RedisTransactional.class);
            if (annotation == null) {
                throw new IllegalStateException("@RedisTransactional annotation not found");
            }

            // 验证配置
            validateTransactionConfig(context, annotation);

            // 将配置存储到上下文
            context.setAttribute(TRANSACTION_CONFIG_ATTR, annotation);

            logExecution(context, "transaction_validation", "事务配置验证成功");

            return chain.proceed(context);

        } catch (Exception e) {
            logException(context, "transaction_validation", e, "事务配置验证失败");
            throw e;
        }
    }

    /**
     * 验证事务配置
     *
     * @param context 切面上下文
     * @param annotation 事务注解
     */
    private void validateTransactionConfig(AspectContext context, RedisTransactional annotation) {
        // 验证超时配置
        validateTimeout(annotation.timeout());

        // 验证锁配置
        validateLockConfig(annotation);

        // 验证重试配置
        validateRetryConfig(annotation);
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
            logUtils.business()
                    .trace(
                            "transaction_validation",
                            "timeout_config",
                            "warning",
                            "事务超时时间过短: " + timeout + "ms，建议至少设置为1000ms");
        }

        if (timeout > 300000) { // 5分钟
            logUtils.business()
                    .trace(
                            "transaction_validation",
                            "timeout_config",
                            "warning",
                            "事务超时时间过长: " + timeout + "ms，建议不超过300000ms(5分钟)");
        }
    }

    /**
     * 验证锁配置
     *
     * @param annotation 事务注解
     */
    private void validateLockConfig(RedisTransactional annotation) {
        String lockKey = annotation.lockKey();
        long timeoutMs = annotation.timeout();
        long leaseMs = annotation.lockLeaseTimeMs();

        if (lockKey != null && !lockKey.trim().isEmpty()) {
            if (leaseMs > 0 && leaseMs < timeoutMs) {
                logUtils.business()
                        .trace(
                                "transaction_validation",
                                "lock_config",
                                "warning",
                                "锁租约时间(" + leaseMs + "ms)小于事务超时(" + timeoutMs + "ms)，可能导致锁提前释放");
            }

            if (annotation.lockWaitTimeMs() < 0) {
                throw new IllegalArgumentException("锁等待时间不能为负数: " + annotation.lockWaitTimeMs());
            }
        }
    }

    /**
     * 验证重试配置
     *
     * @param annotation 事务注解
     */
    private void validateRetryConfig(RedisTransactional annotation) {
        // 这里可以添加重试策略的验证逻辑
        // 目前只是记录配置信息
        logUtils.business()
                .trace(
                        "transaction_validation",
                        "retry_config",
                        "validated",
                        "retryPolicy: " + annotation.retryPolicy());
    }
}
