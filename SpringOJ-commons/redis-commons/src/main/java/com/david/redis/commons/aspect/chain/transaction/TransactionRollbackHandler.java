package com.david.redis.commons.aspect.chain.transaction;

import com.david.log.commons.core.LogUtils;
import com.david.redis.commons.annotation.RedisTransactional;
import com.david.redis.commons.aspect.chain.AbstractAspectHandler;
import com.david.redis.commons.aspect.chain.AspectChain;
import com.david.redis.commons.aspect.chain.AspectContext;
import com.david.redis.commons.aspect.chain.AspectType;
import com.david.redis.commons.core.transaction.RedisTransactionManager;
import com.david.redis.commons.core.transaction.TransactionContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 事务回滚处理器
 *
 * <p>负责在异常情况下回滚Redis事务。
 *
 * @author David
 */
@Component
public class TransactionRollbackHandler extends AbstractAspectHandler {

    private final RedisTransactionManager transactionManager;

    public TransactionRollbackHandler(
            LogUtils logUtils, RedisTransactionManager transactionManager) {
        super(logUtils);
        this.transactionManager = transactionManager;
    }

    @Override
    protected Set<AspectType> getSupportedAspectTypes() {
        return Set.of(AspectType.TRANSACTION);
    }

    @Override
    public int getOrder() {
        return 70; // 在提交处理器之后
    }

    @Override
    public boolean canHandle(AspectContext context) {
        if (!super.canHandle(context)) {
            return false;
        }

        // 只有在有异常且需要回滚时才处理
        return context.hasException() && shouldRollback(context);
    }

    @Override
    public Object handle(AspectContext context, AspectChain chain) throws Throwable {
        TransactionContext transactionContext =
                context.getAttribute(TransactionBeginHandler.TRANSACTION_CONTEXT_ATTR);

        if (transactionContext == null) {
            logExecution(context, "transaction_rollback", "事务上下文不存在");
            return chain.proceed(context);
        }

        try {
            // 执行回滚
            transactionManager.rollbackTransaction(transactionContext);

            logExecution(context, "transaction_rollback", "事务回滚: " + transactionContext.getTransactionId());

            return chain.proceed(context);

        } catch (Exception rollbackException) {
            logException(
                    context,
                    "transaction_rollback",
                    rollbackException,
                    "transactionId: " + transactionContext.getTransactionId());

            // 将回滚异常作为抑制异常添加到原始异常中
            if (context.getException() != null) {
                context.getException().addSuppressed(rollbackException);
            }

            return chain.proceed(context);
        }
    }

    /**
     * 判断是否应该回滚事务
     *
     * @param context 切面上下文
     * @return true 如果应该回滚
     */
    private boolean shouldRollback(AspectContext context) {
        Throwable throwable = context.getException();
        if (throwable == null) {
            return false;
        }

        RedisTransactional annotation =
                context.getAttribute(TransactionValidationHandler.TRANSACTION_CONFIG_ATTR);
        if (annotation == null) {
            return true; // 默认回滚
        }

        // 检查noRollbackFor配置
        Class<? extends Throwable>[] noRollbackFor = annotation.noRollbackFor();
        for (Class<? extends Throwable> exceptionClass : noRollbackFor) {
            if (exceptionClass.isAssignableFrom(throwable.getClass())) {
                logExecution(context, "rollback_check", "异常不回滚: " + throwable.getClass().getSimpleName());
                return false;
            }
        }

        // 检查rollbackFor配置
        Class<? extends Throwable>[] rollbackFor = annotation.rollbackFor();
        if (rollbackFor.length > 0) {
            for (Class<? extends Throwable> exceptionClass : rollbackFor) {
                if (exceptionClass.isAssignableFrom(throwable.getClass())) {
                    logExecution(context, "rollback_check", "异常需回滚: " + throwable.getClass().getSimpleName());
                    return true;
                }
            }
            // 如果指定了rollbackFor但异常不在列表中，则不回滚
            logExecution(context, "rollback_check", "异常不在回滚列表: " + throwable.getClass().getSimpleName());
            return false;
        }

        // 默认行为：RuntimeException和Error会触发回滚
        boolean shouldRollback =
                (throwable instanceof RuntimeException) || (throwable instanceof Error);

        String reason = shouldRollback ? " 是RuntimeException或Error" : " 是受检异常";
        logExecution(context, "rollback_check", "异常回滚检查: " + throwable.getClass().getSimpleName() + reason);

        return shouldRollback;
    }
}
