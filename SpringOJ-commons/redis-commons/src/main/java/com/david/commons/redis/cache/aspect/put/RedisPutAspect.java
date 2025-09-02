package com.david.commons.redis.cache.aspect.put;

import com.david.commons.redis.RedisUtils;
import com.david.commons.redis.cache.CacheAnnotationParser;
import com.david.commons.redis.cache.CacheMetadata;
import com.david.commons.redis.cache.CacheOperation;
import com.david.commons.redis.cache.aspect.CacheContext;
import com.david.commons.redis.cache.expression.CacheExpressionEvaluator;
import com.david.commons.redis.cache.fallback.CacheFallbackHandler;
import com.david.commons.redis.cache.handler.CacheOperationHandler;
import com.david.commons.redis.cache.metrics.CacheMetricsCollector;
import com.david.commons.redis.cache.metrics.CacheOperationTimer;
import com.david.commons.redis.config.RedisCommonsProperties;
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
 * Redis @RedisPut 缓存更新 AOP 切面
 *
 * <p>专门处理 @RedisPut 注解的切面类。 负责拦截带有 @RedisPut 注解的方法，总是执行方法并更新缓存。
 *
 * @author David
 */
@Aspect
@Component
@Order(101) // 设置切面执行顺序，确保在其他缓存切面之后执行
@Slf4j
public class RedisPutAspect {

    private final RedisUtils redisUtils;
    private final CacheAnnotationParser annotationParser;
    private final CacheExpressionEvaluator expressionEvaluator;
    private final CacheOperationHandler operationHandler;
    private final CacheFallbackHandler fallbackHandler;
    private final CacheMetricsCollector metricsCollector;
    private final RedisCommonsProperties properties;

    public RedisPutAspect(
            RedisUtils redisUtils,
            CacheAnnotationParser annotationParser,
            CacheExpressionEvaluator expressionEvaluator,
            CacheOperationHandler operationHandler,
            CacheFallbackHandler fallbackHandler,
            CacheMetricsCollector metricsCollector,
            RedisCommonsProperties properties) {
        this.redisUtils = redisUtils;
        this.annotationParser = annotationParser;
        this.expressionEvaluator = expressionEvaluator;
        this.operationHandler = operationHandler;
        this.fallbackHandler = fallbackHandler;
        this.metricsCollector = metricsCollector;
        this.properties = properties;
    }

    /** 拦截带有 @RedisPut 注解的方法 */
    @Around("@annotation(com.david.commons.redis.cache.annotation.RedisPut)")
    public Object handlePutOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!properties.isEnabled()) {
            log.debug("Redis commons is disabled, skipping put operation");
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        // 解析 @RedisPut 注解
        List<CacheMetadata> metadataList =
                annotationParser.parseCacheAnnotations(method).stream()
                        .filter(metadata -> metadata.getOperation() == CacheOperation.PUT)
                        .toList();

        if (metadataList.isEmpty()) {
            log.debug("No @RedisPut annotations found on method: {}", method.getName());
            return joinPoint.proceed();
        }

        log.debug(
                "Processing @RedisPut operations for method: {}, operations: {}",
                method.getName(),
                metadataList.size());

        // 创建缓存上下文
        CacheContext context =
                CacheContext.builder()
                        .method(method)
                        .args(args)
                        .target(joinPoint.getTarget())
                        .build();

        try {
            return processPutOperations(joinPoint, metadataList, context);
        } catch (Exception e) {
            log.error("Error processing @RedisPut operations for method: {}", method.getName(), e);

            // 使用降级处理器处理异常
            CacheMetadata primaryMetadata = metadataList.get(0);
            return fallbackHandler.handleCacheFallback(joinPoint, primaryMetadata, context, e);

            // 如果没有缓存元数据，直接执行原方法
        }
    }

    /** 处理 @RedisPut 缓存更新操作 */
    private Object processPutOperations(
            ProceedingJoinPoint joinPoint, List<CacheMetadata> metadataList, CacheContext context)
            throws Throwable {

        // 1. 总是执行原方法（@RedisPut 的特性）
        Object result = joinPoint.proceed();
        context.setResult(result);

        // 2. 将方法结果更新到缓存
        for (CacheMetadata metadata : metadataList) {
            if (shouldExecuteOperation(metadata, context, result)) {
                CacheOperationTimer timer =
                        metricsCollector.startOperation(metadata, context.getMethod().getName());

                try {
                    operationHandler.handlePut(metadata, context);
                    metricsCollector.recordSuccess(timer, false);
                    log.debug("Cache put executed for key pattern: {}", metadata.getKey());
                } catch (Exception e) {
                    metricsCollector.recordFailure(timer, e);
                    log.warn(
                            "Put operation failed, using fallback for key pattern: {}",
                            metadata.getKey(),
                            e);
                    fallbackHandler.handleCachePutFallback(metadata, context, e);
                }
            }
        }

        return result;
    }

    /** 判断是否应该执行缓存操作 */
    private boolean shouldExecuteOperation(
            CacheMetadata metadata, CacheContext context, Object result) {
        try {
            // 检查 condition 条件
            if (!metadata.getCondition().isEmpty()) {
                boolean conditionResult =
                        expressionEvaluator.evaluateCondition(
                                metadata.getCondition(), context, result);
                if (!conditionResult) {
                    log.debug(
                            "Cache put operation skipped due to condition: {}",
                            metadata.getCondition());
                    return false;
                }
            }

            // 检查 unless 条件
            if (result != null && !metadata.getUnless().isEmpty()) {
                boolean unlessResult =
                        expressionEvaluator.evaluateCondition(
                                metadata.getUnless(), context, result);
                if (unlessResult) {
                    log.debug(
                            "Cache put operation skipped due to unless condition: {}",
                            metadata.getUnless());
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            log.warn("Error evaluating cache put operation condition, skipping operation", e);
            return false;
        }
    }
}
