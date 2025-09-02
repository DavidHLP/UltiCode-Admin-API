package com.david.commons.redis.cache.aspect.cacheable;

import com.david.commons.redis.cache.CacheAnnotationParser;
import com.david.commons.redis.cache.CacheMetadata;
import com.david.commons.redis.cache.CacheOperation;
import com.david.commons.redis.cache.aspect.CacheContext;
import com.david.commons.redis.cache.fallback.CacheFallbackHandler;
import com.david.commons.redis.cache.handler.CacheOperationHandler;
import com.david.commons.redis.cache.metrics.CacheMetricsCollector;
import com.david.commons.redis.cache.metrics.CacheOperationTimer;
import com.david.commons.redis.config.RedisCommonsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Redis @RedisCacheable 缓存 AOP 切面
 *
 * <p>专门处理 @RedisCacheable 注解的切面类。 负责拦截带有 @RedisCacheable 注解的方法，执行缓存查询和存储操作。
 *
 * @author David
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@Order(100) // 设置切面执行顺序，确保在事务切面之后执行
public class RedisCacheableAspect {

    private final CacheAnnotationParser annotationParser;
    private final CacheOperationHandler operationHandler;
    private final CacheFallbackHandler fallbackHandler;
    private final CacheMetricsCollector metricsCollector;
    private final RedisCommonsProperties properties;
    private final CheckExecuteOperation checkExecuteOperation;

    /** 拦截带有 @RedisCacheable 注解的方法 */
    @Around("@annotation(com.david.commons.redis.cache.annotation.RedisCacheable)")
    public Object handleCacheableOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        // 检查 Redis 功能是否启用
        if (!properties.isEnabled()) {
            log.debug("Redis 功能已禁用，跳过缓存操作");
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        // 解析 @RedisCacheable 注解
        List<CacheMetadata> metadataList =
                annotationParser.parseCacheAnnotations(method).stream()
                        .filter(metadata -> metadata.getOperation() == CacheOperation.CACHEABLE)
                        .toList();

        if (metadataList.isEmpty()) {
            log.debug("在方法上未找到 @RedisCacheable 注解：{}", method.getName());
            return joinPoint.proceed();
        }

        log.debug("正在处理方法上的 @RedisCacheable 操作：{}，操作数：{}", method.getName(), metadataList.size());

        // 创建缓存上下文
        CacheContext context =
                CacheContext.builder()
                        .method(method)
                        .args(args)
                        .target(joinPoint.getTarget())
                        .build();

        try {
            return processCacheableOperations(joinPoint, metadataList, context);
        } catch (Exception e) {
            log.error("处理方法上的 @RedisCacheable 操作时出错：{}", method.getName(), e);

            // 使用降级处理器处理异常
            CacheMetadata primaryMetadata = metadataList.get(0);
            return fallbackHandler.handleCacheFallback(joinPoint, primaryMetadata, context, e);

            // 如果没有缓存元数据，直接执行原方法
        }
    }

    /** 处理 @RedisCacheable 缓存操作 */
    private Object processCacheableOperations(
            ProceedingJoinPoint joinPoint, List<CacheMetadata> metadataList, CacheContext context)
            throws Throwable {

        Object result = null;
        boolean methodExecuted = false;

        // 1. 尝试从缓存获取数据
        for (CacheMetadata metadata : metadataList) {
            // 判断是否应该执行缓存操作（例如，基于条件表达式）
            if (checkExecuteOperation.shouldExecuteOperation(metadata, context, null)) {
                CacheOperationTimer timer =
                        metricsCollector.startOperation(metadata, context.getMethod().getName());

                try {
                    // 从缓存中获取值
                    Object cachedValue = operationHandler.handleCacheable(metadata, context);
                    if (cachedValue != null) {
                        log.debug("缓存命中，键模式：{}", metadata.getKey());
                        metricsCollector.recordSuccess(timer, true);
                        return cachedValue; // 缓存命中，直接返回
                    } else {
                        log.debug("缓存未命中，键模式：{}", metadata.getKey());
                        metricsCollector.recordSuccess(timer, false);
                    }
                } catch (Exception e) {
                    metricsCollector.recordFailure(timer, e);
                    log.warn("缓存操作失败，正在使用回退处理，键模式：{}", metadata.getKey(), e);

                    try {
                        // 缓存操作失败时，尝试使用降级处理
                        return fallbackHandler.handleCacheableFallback(
                                joinPoint, metadata, context, e);
                    } catch (Throwable fallbackException) {
                        log.error("缓存操作的回退处理也失败了", fallbackException);
                        // 降级失败，继续处理其他缓存操作或执行原方法
                    }
                }
            }
        }

        // 2. 缓存未命中，执行原方法
        result = joinPoint.proceed();
        methodExecuted = true;
        context.setResult(result);

        // 3. 将方法结果存入缓存
        for (CacheMetadata metadata : metadataList) {
            // 判断是否应该将结果存入缓存（例如，基于条件或除非条件）
            if (checkExecuteOperation.shouldExecuteOperation(metadata, context, result)) {
                CacheOperationTimer timer =
                        metricsCollector.startOperation(metadata, context.getMethod().getName());

                try {
                    operationHandler.handleCacheableResult(metadata, context);
                    metricsCollector.recordSuccess(timer, false);
                    log.debug("已缓存结果，键模式：{}", metadata.getKey());
                } catch (Exception e) {
                    metricsCollector.recordFailure(timer, e);
                    log.warn("缓存结果失败，键模式：{}", metadata.getKey(), e);
                    fallbackHandler.handleCachePutFallback(metadata, context, e);
                }
            }
        }

        return result;
    }
}
