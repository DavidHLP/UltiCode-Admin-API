package com.david.commons.redis.cache.aspect;

import com.david.commons.redis.cache.CacheContext;
import com.david.commons.redis.cache.CacheMetadata;
import com.david.commons.redis.cache.aspect.chain.AspectContext;
import com.david.commons.redis.cache.aspect.chain.cacheable.CacheReadHandler;
import com.david.commons.redis.cache.aspect.chain.cacheable.ConditionHandler;
import com.david.commons.redis.cache.aspect.chain.cacheable.CacheSyncLockHandler;
import com.david.commons.redis.cache.aspect.chain.cacheable.CacheProtectionHandler;
import com.david.commons.redis.cache.enums.CacheOperation;
import com.david.commons.redis.cache.parser.CacheAnnotationParser;
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
 * <p>
 * 使用责任链模式处理 @RedisCacheable 注解的切面类。 负责拦截带有 @RedisCacheable 注解的方法，通过责任链执行缓存操作。
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
    private final RedisCommonsProperties properties;
    private final ConditionHandler conditionHandler;
    private final CacheReadHandler cacheReadHandler;
    private final CacheSyncLockHandler cacheSyncLockHandler;
    private final CacheProtectionHandler cacheProtectionHandler;

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
        List<CacheMetadata> metadataList = annotationParser.parseCacheAnnotations(method).stream()
                .filter(metadata -> metadata.operation() == CacheOperation.CACHEABLE)
                .toList();

        if (metadataList.isEmpty()) {
            log.debug("在方法上未找到 @RedisCacheable 注解：{}", method.getName());
            return joinPoint.proceed();
        }

        log.debug("正在处理方法上的 @RedisCacheable 操作：{}，操作数：{}", method.getName(), metadataList.size());

        // 处理每个缓存元数据
        for (CacheMetadata metadata : metadataList) {
            try {
                // 创建缓存上下文
                CacheContext cacheContext = CacheContext.builder()
                        .method(method)
                        .args(args)
                        .target(joinPoint.getTarget())
                        .build();

                // 创建切面上下文
                AspectContext aspectContext = AspectContext.builder()
                        .context(cacheContext)
                        .metadata(metadata)
                        .joinPoint(joinPoint)
                        .build();

                handleCacheableOperation(aspectContext);

                // 检查处理结果或短路
                if (aspectContext.getCacheHit() || aspectContext.getMethodInvoked() || aspectContext.isEnd()) {
                    Object result = aspectContext.getFinalResult();
                    log.debug(
                            "责任链处理完成，返回结果类型：{}",
                            result != null ? result.getClass().getSimpleName() : "null");
                    return result;
                }

            } catch (Exception e) {
                log.error("处理缓存元数据时出错，键模式：{}，将尝试下一个或直接执行方法", metadata.key(), e);
                // 继续处理下一个元数据或执行原方法
            }
        }

        // 如果所有缓存操作都失败，直接执行原方法
        log.warn("所有缓存操作都失败，直接执行原方法：{}", method.getName());
        return joinPoint.proceed();
    }

    private void handleCacheableOperation(AspectContext aspectContext) {
        log.debug("开始执行责任链处理，键模式：{}", aspectContext.getMetadata().key());

        // 构建责任链：条件检查 -> 缓存读取 -> 同步锁保护 -> 缓存保护（统一处理穿透/击穿/雪崩防护+写入）
        conditionHandler.setNextHandler(cacheReadHandler);
        cacheReadHandler.setNextHandler(cacheSyncLockHandler);
        cacheSyncLockHandler.setNextHandler(cacheProtectionHandler);

        // 启动责任链处理
        conditionHandler.handleRequest(aspectContext);
    }
}
