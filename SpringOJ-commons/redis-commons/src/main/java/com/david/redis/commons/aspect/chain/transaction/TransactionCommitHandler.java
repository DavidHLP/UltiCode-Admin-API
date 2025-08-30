package com.david.redis.commons.aspect.chain.transaction;

import com.david.log.commons.core.LogUtils;
import com.david.redis.commons.annotation.RedisTransactional;
import com.david.redis.commons.aspect.chain.AbstractAspectHandler;
import com.david.redis.commons.aspect.chain.AspectChain;
import com.david.redis.commons.aspect.chain.AspectContext;
import com.david.redis.commons.aspect.chain.AspectType;
import com.david.redis.commons.core.transaction.RedisTransactionManager;
import com.david.redis.commons.core.transaction.TransactionContext;
import com.david.redis.commons.exception.RedisTransactionException;

import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 事务提交处理器
 *
 * <p>负责提交Redis事务，处理超时检查。
 *
 * @author David
 */
@Component
public class TransactionCommitHandler extends AbstractAspectHandler {

    private final RedisTransactionManager transactionManager;

    public TransactionCommitHandler(LogUtils logUtils, RedisTransactionManager transactionManager) {
        super(logUtils);
        this.transactionManager = transactionManager;
    }

    @Override
    protected Set<AspectType> getSupportedAspectTypes() {
        return Set.of(AspectType.TRANSACTION);
    }

    @Override
    public int getOrder() {
        return 60; // 在方法执行之后
    }

    @Override
    public boolean canHandle(AspectContext context) {
        if (!super.canHandle(context)) {
            return false;
        }

        // 只有方法执行成功且没有异常时才提交
        return context.isMethodExecuted() && !context.hasException();
    }

    @Override
    public Object handle(AspectContext context, AspectChain chain) throws Throwable {
        TransactionContext transactionContext =
                context.getAttribute(TransactionBeginHandler.TRANSACTION_CONTEXT_ATTR);
        RedisTransactional annotation =
                context.getAttribute(TransactionValidationHandler.TRANSACTION_CONFIG_ATTR);

        if (transactionContext == null) {
            logExecution(context, "transaction_commit", "事务上下文不存在");
            return chain.proceed(context);
        }

        try {
            // 检查事务超时
            if (transactionContext.isTimeout()) {
                throw RedisTransactionException.transactionTimeout(
                        transactionContext.getTransactionId(), annotation.timeout());
            }

            // 提交事务
            transactionManager.commitTransaction(transactionContext);

            logExecution(context, "transaction_commit", "事务提交: " + transactionContext.getTransactionId());

            return chain.proceed(context);

        } catch (Exception e) {
            logException(context, "transaction_commit", e, "事务提交失败: " + transactionContext.getTransactionId());
            throw e;
        }
    }
}
