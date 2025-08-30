package com.david.redis.commons.aspect;

import com.david.log.commons.core.LogUtils;
import com.david.redis.commons.annotation.RedisTransactional;
import com.david.redis.commons.aspect.chain.AspectChain;
import com.david.redis.commons.aspect.chain.AspectChainManager;
import com.david.redis.commons.aspect.chain.AspectContext;
import com.david.redis.commons.aspect.chain.AspectType;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 重构后的Redis事务切面
 *
 * <p>基于责任链模式的事务切面实现，提供更好的可扩展性和维护性。
 * 
 * @author David
 */
@Aspect
@Component
@RequiredArgsConstructor
@Order(100) // 确保在其他切面之前执行
public class TransactionAspect {

    private final AspectChainManager chainManager;
    private final LogUtils logUtils;

    /**
     * 处理@RedisTransactional注解的方法
     *
     * @param joinPoint 连接点
     * @param redisTransactional 事务注解
     * @return 方法执行结果
     * @throws Throwable 方法执行异常
     */
    @Around("@annotation(redisTransactional)")
    public Object handleTransaction(
            ProceedingJoinPoint joinPoint, RedisTransactional redisTransactional) throws Throwable {

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        
        // 创建切面上下文
        AspectContext context = new AspectContext(joinPoint, AspectType.TRANSACTION);
        
        logUtils.business()
                .trace(
                        "refactored_transaction_aspect",
                        "transaction",
                        "start",
                        "method: " + methodName,
                        "lockStrategy: " + redisTransactional.lockStrategy(),
                        "retryPolicy: " + redisTransactional.retryPolicy(),
                        "timeoutStrategy: " + redisTransactional.timeoutStrategy());

        try {
            // 创建事务处理器链
            AspectChain chain = chainManager.createChain(AspectType.TRANSACTION);
            
            // 执行处理器链
            Object result = chain.proceed(context);
            
            logUtils.business()
                    .trace(
                            "refactored_transaction_aspect",
                            "transaction",
                            "success",
                            "method: " + methodName,
                            "executionTime: " + context.getExecutionTime() + "ms");
            
            return result != null ? result : context.getResult();
            
        } catch (Throwable throwable) {
            logUtils.exception()
                    .business(
                            "refactored_transaction_aspect_failed",
                            throwable,
                            "事务切面处理失败",
                            "method: " + methodName,
                            "executionTime: " + context.getExecutionTime() + "ms");
            throw throwable;
        }
    }
}
