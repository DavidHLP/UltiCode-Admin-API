package com.david.commons.redis.cache.aspect.evict;

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
 * Redis @RedisEvict 缓存清除 AOP 切面
 * <p>
 * 专门处理 @RedisEvict 注解的切面类。
 * 负责拦截带有 @RedisEvict 注解的方法，执行缓存清除操作。
 * </p>
 *
 * @author David
 */
@Aspect
@Component
@Order(99) // 设置切面执行顺序，确保在其他缓存切面之前执行
@Slf4j
public class RedisEvictAspect {

    private final RedisUtils redisUtils;
    private final CacheAnnotationParser annotationParser;
    private final CacheExpressionEvaluator expressionEvaluator;
    private final CacheOperationHandler operationHandler;
    private final CacheFallbackHandler fallbackHandler;
    private final CacheMetricsCollector metricsCollector;
    private final RedisCommonsProperties properties;

    public RedisEvictAspect(RedisUtils redisUtils,
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

    /**
     * 拦截带有 @RedisEvict 注解的方法
     */
    @Around("@annotation(com.david.commons.redis.cache.annotation.RedisEvict)")
    public Object handleEvictOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!properties.isEnabled()) {
            log.debug("Redis commons is disabled, skipping evict operation");
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        // 解析 @RedisEvict 注解
        List<CacheMetadata> metadataList = annotationParser.parseCacheAnnotations(method).stream()
                .filter(metadata -> metadata.getOperation() == CacheOperation.EVICT)
                .toList();

        if (metadataList.isEmpty()) {
            log.debug("No @RedisEvict annotations found on method: {}", method.getName());
            return joinPoint.proceed();
        }

        log.debug("Processing @RedisEvict operations for method: {}, operations: {}",
                method.getName(), metadataList.size());

        // 创建缓存上下文
        CacheContext context = CacheContext.builder()
                .method(method)
                .args(args)
                .target(joinPoint.getTarget())
                .build();

        try {
            return processEvictOperations(joinPoint, metadataList, context);
        } catch (Exception e) {
            log.error("Error processing @RedisEvict operations for method: {}", method.getName(), e);

            // 使用降级处理器处理异常
            if (!metadataList.isEmpty()) {
                CacheMetadata primaryMetadata = metadataList.get(0);
                return fallbackHandler.handleCacheFallback(joinPoint, primaryMetadata, context, e);
            }

            // 如果没有缓存元数据，直接执行原方法
            return joinPoint.proceed();
        }
    }

    /**
     * 处理 @RedisEvict 缓存清除操作
     */
    private Object processEvictOperations(ProceedingJoinPoint joinPoint,
                                          List<CacheMetadata> metadataList,
                                          CacheContext context) throws Throwable {

        // 分离 beforeInvocation 和 afterInvocation 的清除操作
        List<CacheMetadata> beforeEvictions = metadataList.stream()
                .filter(CacheMetadata::isBeforeInvocation)
                .toList();

        List<CacheMetadata> afterEvictions = metadataList.stream()
                .filter(metadata -> !metadata.isBeforeInvocation())
                .toList();

        // 1. 执行方法前的缓存清除操作
        for (CacheMetadata metadata : beforeEvictions) {
            if (shouldExecuteOperation(metadata, context, null)) {
                CacheOperationTimer timer = metricsCollector.startOperation(metadata, context.getMethod().getName());

                try {
                    operationHandler.handleEvict(metadata, context);
                    metricsCollector.recordSuccess(timer, false);
                    log.debug("Before evict executed for key pattern: {}", metadata.getKey());
                } catch (Exception e) {
                    metricsCollector.recordFailure(timer, e);
                    log.warn("Before evict operation failed, using fallback for key pattern: {}", metadata.getKey(), e);
                    fallbackHandler.handleCacheEvictFallback(metadata, context, e);
                }
            }
        }

        // 2. 执行原方法
        Object result;
        try {
            result = joinPoint.proceed();
            context.setResult(result);
        } catch (Exception e) {
            // 如果方法执行失败且有 afterInvocation=true 的清除操作，根据配置决定是否执行
            log.debug("Method execution failed, checking afterInvocation evict operations");

            // 这里可以根据具体需求决定是否在方法失败后仍执行清除操作
            // 通常情况下，如果方法失败，不应该执行 afterInvocation 的清除操作
            throw e;
        }

        // 3. 执行方法后的缓存清除操作
        for (CacheMetadata metadata : afterEvictions) {
            if (shouldExecuteOperation(metadata, context, result)) {
                CacheOperationTimer timer = metricsCollector.startOperation(metadata, context.getMethod().getName());

                try {
                    operationHandler.handleEvict(metadata, context);
                    metricsCollector.recordSuccess(timer, false);
                    log.debug("After evict executed for key pattern: {}", metadata.getKey());
                } catch (Exception e) {
                    metricsCollector.recordFailure(timer, e);
                    log.warn("After evict operation failed, using fallback for key pattern: {}", metadata.getKey(), e);
                    fallbackHandler.handleCacheEvictFallback(metadata, context, e);
                }
            }
        }

        return result;
    }

    /**
     * 判断是否应该执行缓存操作
     */
    private boolean shouldExecuteOperation(CacheMetadata metadata, CacheContext context, Object result) {
        try {
            // 检查 condition 条件
            if (!metadata.getCondition().isEmpty()) {
                boolean conditionResult = expressionEvaluator.evaluateCondition(
                        metadata.getCondition(), context, result);
                if (!conditionResult) {
                    log.debug("Cache evict operation skipped due to condition: {}", metadata.getCondition());
                    return false;
                }
            }

            // 对于 @RedisEvict，unless 条件通常不适用，但仍保留检查
            if (result != null && !metadata.getUnless().isEmpty()) {
                boolean unlessResult = expressionEvaluator.evaluateCondition(
                        metadata.getUnless(), context, result);
                if (unlessResult) {
                    log.debug("Cache evict operation skipped due to unless condition: {}", metadata.getUnless());
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            log.warn("Error evaluating cache evict operation condition, skipping operation", e);
            return false;
        }
    }
}