package com.david.redis.commons.aspect.chain.transaction;

import com.david.log.commons.core.LogUtils;
import com.david.redis.commons.annotation.RedisTransactional;
import com.david.redis.commons.aspect.chain.AbstractAspectHandler;
import com.david.redis.commons.aspect.chain.AspectChain;
import com.david.redis.commons.aspect.chain.AspectContext;
import com.david.redis.commons.aspect.chain.AspectType;
import com.david.redis.commons.core.transaction.RedisTransactionManager;
import com.david.redis.commons.core.transaction.TransactionContext;

import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 事务开始处理器
 *
 * <p>负责开始Redis事务，创建事务上下文。
 * 
 * @author David
 */
@Component
public class TransactionBeginHandler extends AbstractAspectHandler {

    public static final String TRANSACTION_CONTEXT_ATTR = "transaction.context";
    private final RedisTransactionManager transactionManager;

    public TransactionBeginHandler(LogUtils logUtils, RedisTransactionManager transactionManager) {
        super(logUtils);
        this.transactionManager = transactionManager;
    }

    @Override
    protected Set<AspectType> getSupportedAspectTypes() {
        return Set.of(AspectType.TRANSACTION);
    }

    @Override
    public int getOrder() {
        return 20; // 在锁获取之后，方法执行之前
    }

    @Override
    public Object handle(AspectContext context, AspectChain chain) throws Throwable {
        RedisTransactional annotation = context.getAttribute(TransactionValidationHandler.TRANSACTION_CONFIG_ATTR);
        String resolvedLockKey = context.getAttribute(LockAcquisitionHandler.RESOLVED_LOCK_KEY_ATTR);
        
        try {
            // 开始事务
            TransactionContext transactionContext = transactionManager.beginTransaction(annotation);
            context.setAttribute(TRANSACTION_CONTEXT_ATTR, transactionContext);
            
            logExecution(context, "transaction_begin", "事务开始: " + transactionContext.getTransactionId());
            
            // 记录方法调用
            transactionManager.addOperation("METHOD_CALL: " + context.getMethodSignature());
            
            return chain.proceed(context);
            
        } catch (Exception e) {
            logException(context, "transaction_begin", e, "事务开始失败");
            throw e;
        }
    }
}
