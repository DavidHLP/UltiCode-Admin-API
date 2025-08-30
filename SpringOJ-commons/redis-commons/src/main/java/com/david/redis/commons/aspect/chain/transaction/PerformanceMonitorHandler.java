package com.david.redis.commons.aspect.chain.transaction;

import com.david.log.commons.core.LogUtils;
import com.david.redis.commons.annotation.RedisTransactional;
import com.david.redis.commons.aspect.chain.AbstractAspectHandler;
import com.david.redis.commons.aspect.chain.AspectChain;
import com.david.redis.commons.aspect.chain.AspectContext;
import com.david.redis.commons.aspect.chain.AspectType;
import com.david.redis.commons.core.transaction.TransactionContext;
import com.david.redis.commons.enums.TimeoutStrategy;
import com.david.redis.commons.monitor.CacheMetricsCollector;

import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 事务性能监控处理器
 *
 * <p>负责监控事务性能指标和处理超时策略。
 *
 * @author David
 */
@Component
public class PerformanceMonitorHandler extends AbstractAspectHandler {

    private final CacheMetricsCollector metricsCollector;

    public PerformanceMonitorHandler(LogUtils logUtils, CacheMetricsCollector metricsCollector) {
        super(logUtils);
        this.metricsCollector = metricsCollector;
    }

    @Override
    protected Set<AspectType> getSupportedAspectTypes() {
        return Set.of(AspectType.TRANSACTION);
    }

    @Override
    public int getOrder() {
        return 80; // 最后执行，收集所有性能数据
    }

    @Override
    public boolean canHandle(AspectContext context) {
        if (!super.canHandle(context)) {
            return false;
        }

        RedisTransactional annotation =
                context.getAttribute(TransactionValidationHandler.TRANSACTION_CONFIG_ATTR);
        return annotation != null && annotation.enableMetrics();
    }

    @Override
    public Object handle(AspectContext context, AspectChain chain) throws Throwable {
        try {
            recordTransactionMetrics(context);
            return chain.proceed(context);

        } catch (Exception e) {
            logException(context, "performance_monitor", e, "性能监控异常");
            return chain.proceed(context);
        }
    }

    /**
     * 记录事务性能指标
     *
     * @param context 切面上下文
     */
    private void recordTransactionMetrics(AspectContext context) {
        TransactionContext transactionContext =
                context.getAttribute(TransactionBeginHandler.TRANSACTION_CONTEXT_ATTR);
        RedisTransactional annotation =
                context.getAttribute(TransactionValidationHandler.TRANSACTION_CONFIG_ATTR);

        if (transactionContext == null || annotation == null) {
            return;
        }

        String methodName = context.getMethodSignature();
        long executionTime = context.getExecutionTime();
        int operationCount = transactionContext.getOperations().size();
        String status = transactionContext.getStatus();

        logExecution(context, "performance_monitor", "性能指标收集: " + transactionContext.getTransactionId() + " (" + executionTime + "ms)");

        // 记录指标
        String operation =
                "TRANSACTION_" + (transactionContext.isCommitted() ? "COMMIT" : "ROLLBACK");
        if (context.hasException()) {
            metricsCollector.recordError(operation, executionTime);
        } else if (transactionContext.isCommitted()) {
            metricsCollector.recordSet(methodName, executionTime);
        }

        // 记录慢事务警告
        if (executionTime > 1000) {
            logExecution(context, "performance_monitor", "慢事务警告: " + transactionContext.getTransactionId() + " (" + executionTime + "ms)");
        }

        // 检查死锁检测
        if (annotation.deadlockDetection() && executionTime > annotation.timeout()) {
            logExecution(context, "performance_monitor", "死锁检测: " + transactionContext.getTransactionId() + " (可能存在死锁)");
        }

        // 处理超时策略
        if (executionTime > annotation.timeout() && annotation.timeout() > 0) {
            handleTimeoutStrategy(
                    context, annotation.timeoutStrategy(), methodName, transactionContext);
        }
    }

    /**
     * 处理超时策略
     *
     * @param context 切面上下文
     * @param strategy 超时策略
     * @param methodName 方法名
     * @param transactionContext 事务上下文
     */
    private void handleTimeoutStrategy(
            AspectContext context,
            TimeoutStrategy strategy,
            String methodName,
            TransactionContext transactionContext) {
        switch (strategy) {
            case ROLLBACK:
                logExecution(context, "timeout_strategy", "超时策略-回滚: " + transactionContext.getTransactionId());
                break;
            case FORCE_COMMIT:
                logExecution(context, "timeout_strategy", "超时策略-强制提交: " + transactionContext.getTransactionId());
                break;
            case THROW_EXCEPTION:
                logExecution(context, "timeout_strategy", "超时策略-抛出异常: " + transactionContext.getTransactionId());
                break;
            case EXTEND_TIMEOUT:
                logExecution(context, "timeout_strategy", "超时策略-延长超时: " + transactionContext.getTransactionId());
                break;
            default:
                logExecution(context, "timeout_strategy", "未知超时策略: " + strategy + " - " + transactionContext.getTransactionId());
        }
    }
}
