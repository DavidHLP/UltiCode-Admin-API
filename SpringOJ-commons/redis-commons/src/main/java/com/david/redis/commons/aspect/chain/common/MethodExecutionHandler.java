package com.david.redis.commons.aspect.chain.common;

import com.david.log.commons.LogUtils;
import com.david.redis.commons.aspect.chain.AbstractAspectHandler;
import com.david.redis.commons.aspect.chain.AspectChain;
import com.david.redis.commons.aspect.chain.AspectContext;
import com.david.redis.commons.aspect.chain.AspectType;
import com.david.redis.commons.aspect.chain.cache.CacheRetrievalHandler;

import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 方法执行处理器
 *
 * <p>负责执行原始方法，是处理器链中的核心处理器。
 *
 * @author David
 */
@Component
public class MethodExecutionHandler extends AbstractAspectHandler {

    public MethodExecutionHandler(LogUtils logUtils) {
        super(logUtils);
    }

    @Override
    protected Set<AspectType> getSupportedAspectTypes() {
        return Set.of(AspectType.CACHE, AspectType.CACHE_EVICT, AspectType.TRANSACTION);
    }

    @Override
    public int getOrder() {
        return 50; // 中间执行顺序
    }

    @Override
    public boolean canHandle(AspectContext context) {
        if (!super.canHandle(context)) {
            return false;
        }

        // 对于缓存切面，如果已经缓存命中，则不需要执行原方法
        if (context.getAspectType() == AspectType.CACHE) {
            Boolean cacheHit = context.getAttribute(CacheRetrievalHandler.CACHE_HIT_ATTR, Boolean.class,false);
            return !cacheHit && !context.isMethodExecuted();
        }

        // 对于其他切面，只要方法还未执行就可以处理
        return !context.isMethodExecuted();
    }

    @Override
    public Object handle(AspectContext context, AspectChain chain) throws Throwable {
        if (context.isMethodExecuted()) {
            // 方法已执行，直接继续链
            return chain.proceed(context);
        }

        long startTime = System.currentTimeMillis();

        try {
            logExecution(context, "method_execution", "方法开始执行");

            // 执行原方法
            context.proceedMethod();

            long executionTime = System.currentTimeMillis() - startTime;
            logExecution(context, "method_execution", "方法执行完成: " + executionTime + "ms");

            return chain.proceed(context);

        } catch (Throwable throwable) {
            long executionTime = System.currentTimeMillis() - startTime;
            logException(
                    context,
                    "method_execution",
                    throwable,
                    "executionTime: " + executionTime + "ms");

            // 设置异常信息到上下文
            context.setException(throwable);

            // 继续执行链，让其他处理器处理异常（如事务回滚）
            try {
                chain.proceed(context);
            } catch (Throwable chainException) {
                // 如果链处理也失败，抛出原始异常
                throwable.addSuppressed(chainException);
            }

            throw throwable;
        }
    }
}
