package com.david.redis.commons.aspect;

import com.david.log.commons.LogUtils;
import com.david.redis.commons.annotation.RedisCacheable;
import com.david.redis.commons.annotation.RedisEvict;
import com.david.redis.commons.aspect.chain.AspectChain;
import com.david.redis.commons.aspect.chain.AspectChainManager;
import com.david.redis.commons.aspect.chain.AspectContext;
import com.david.redis.commons.aspect.chain.AspectType;

import lombok.RequiredArgsConstructor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 重构后的Redis缓存切面
 *
 * <p>基于责任链模式的缓存切面实现，提供更好的可扩展性和维护性。
 *
 * @author David
 */
@Aspect
@Component
@RequiredArgsConstructor
public class CacheAspect {

    private final AspectChainManager chainManager;
    private final LogUtils logUtils;

    /** 处理@RedisCacheable注解的方法 */
    @Around("@annotation(redisCacheable)")
    public Object handleCacheable(ProceedingJoinPoint joinPoint, RedisCacheable redisCacheable)
            throws Throwable {

        // 创建切面上下文
        AspectContext context = new AspectContext(joinPoint, AspectType.CACHE);

        logUtils.business()
                .trace(
                        "refactored_cache_aspect",
                        "cacheable",
                        "start",
                        "method: " + context.getMethodSignature());

        try {
            // 创建缓存处理器链
            AspectChain chain = chainManager.createChain(AspectType.CACHE);

            // 执行处理器链
            Object result = chain.proceed(context);

            logUtils.business()
                    .trace(
                            "refactored_cache_aspect",
                            "cacheable",
                            "success",
                            "method: " + context.getMethodSignature(),
                            "executionTime: " + context.getExecutionTime() + "ms");

            return result != null ? result : context.getResult();

        } catch (Throwable throwable) {
            logUtils.exception()
                    .business(
                            "refactored_cache_aspect_failed",
                            throwable,
                            "缓存切面处理失败",
                            "method: " + context.getMethodSignature(),
                            "executionTime: " + context.getExecutionTime() + "ms");
            throw throwable;
        }
    }

    /** 处理@RedisEvict注解的方法 */
    @Around("@annotation(redisEvict)")
    public Object handleEvict(ProceedingJoinPoint joinPoint, RedisEvict redisEvict)
            throws Throwable {

        // 创建切面上下文
        AspectContext context = new AspectContext(joinPoint, AspectType.CACHE_EVICT);

        logUtils.business()
                .trace(
                        "refactored_cache_aspect",
                        "evict",
                        "start",
                        "method: " + context.getMethodSignature(),
                        "beforeInvocation: " + redisEvict.beforeInvocation());

        try {
            // 创建缓存驱逐处理器链
            AspectChain chain = chainManager.createChain(AspectType.CACHE_EVICT);

            // 执行处理器链
            Object result = chain.proceed(context);

            logUtils.business()
                    .trace(
                            "refactored_cache_aspect",
                            "evict",
                            "success",
                            "method: " + context.getMethodSignature(),
                            "executionTime: " + context.getExecutionTime() + "ms");

            return result != null ? result : context.getResult();

        } catch (Throwable throwable) {
            logUtils.exception()
                    .business(
                            "refactored_cache_evict_failed",
                            throwable,
                            "缓存驱逐切面处理失败",
                            "method: " + context.getMethodSignature(),
                            "executionTime: " + context.getExecutionTime() + "ms");
            throw throwable;
        }
    }
}
