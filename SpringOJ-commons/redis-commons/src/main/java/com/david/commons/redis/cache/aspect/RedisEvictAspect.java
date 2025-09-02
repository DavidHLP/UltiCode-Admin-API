package com.david.commons.redis.cache.aspect;

import com.david.commons.redis.cache.CacheContext;
import com.david.commons.redis.cache.CacheMetadata;
import com.david.commons.redis.cache.aspect.chain.AspectContext;
import com.david.commons.redis.cache.aspect.chain.evict.AfterEvictHandler;
import com.david.commons.redis.cache.aspect.chain.evict.BeforeEvictHandler;
import com.david.commons.redis.cache.aspect.chain.evict.EvictConditionHandler;
import com.david.commons.redis.cache.aspect.chain.evict.EvictMethodInvokeHandler;
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
 * Redis @RedisEvict 缓存清除 AOP 切面
 *
 * <p>使用责任链模式处理 @RedisEvict 注解的切面类。 负责拦截带有 @RedisEvict 注解的方法，通过责任链执行缓存清除操作。
 *
 * @author David
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@Order(99) // 设置切面执行顺序，确保在其他缓存切面之前执行
public class RedisEvictAspect {

    private final CacheAnnotationParser annotationParser;
    private final RedisCommonsProperties properties;
    private final EvictConditionHandler evictConditionHandler;
    private final BeforeEvictHandler beforeEvictHandler;
    private final EvictMethodInvokeHandler evictMethodInvokeHandler;
    private final AfterEvictHandler afterEvictHandler;

    /** 拦截带有 @RedisEvict 注解的方法 */
    @Around("@annotation(com.david.commons.redis.cache.annotation.RedisEvict)")
    public Object handleEvictOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!properties.isEnabled()) {
            log.debug("Redis 功能已禁用，跳过缓存清除操作");
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        // 解析 @RedisEvict 注解
        List<CacheMetadata> metadataList =
                annotationParser.parseCacheAnnotations(method).stream()
                        .filter(metadata -> metadata.operation() == CacheOperation.EVICT)
                        .toList();

        if (metadataList.isEmpty()) {
            log.debug("在方法上未找到 @RedisEvict 注解：{}", method.getName());
            return joinPoint.proceed();
        }

        log.debug("正在处理方法上的 @RedisEvict 操作：{}，操作数：{}", method.getName(), metadataList.size());

        // 处理每个缓存元数据
        for (CacheMetadata metadata : metadataList) {
            try {
                // 创建缓存上下文
                CacheContext cacheContext =
                        CacheContext.builder()
                                .method(method)
                                .args(args)
                                .target(joinPoint.getTarget())
                                .build();

                // 创建切面上下文
                AspectContext aspectContext =
                        AspectContext.builder()
                                .context(cacheContext)
                                .metadata(metadata)
                                .joinPoint(joinPoint)
                                .build();

                handleEvictOperation(aspectContext);

                // 检查处理结果
                if (aspectContext.getMethodInvoked()) {
                    Object result = aspectContext.getFinalResult();
                    log.debug(
                            "缓存清除责任链处理完成，返回结果类型：{}",
                            result != null ? result.getClass().getSimpleName() : "null");
                    return result;
                }

            } catch (Exception e) {
                log.error("处理缓存清除元数据时出错，键模式：{}，将尝试下一个或直接执行方法", metadata.key(), e);
                // 继续处理下一个元数据或执行原方法
            }
        }

        // 如果所有缓存操作都失败，直接执行原方法
        log.warn("所有缓存清除操作都失败，直接执行原方法：{}", method.getName());
        return joinPoint.proceed();
    }

    private void handleEvictOperation(AspectContext aspectContext) {
        log.debug("开始执行缓存清除责任链处理，键模式：{}", aspectContext.getMetadata().key());

        // 启动责任链处理
        evictConditionHandler.setNextHandler(beforeEvictHandler);
        beforeEvictHandler.setNextHandler(evictMethodInvokeHandler);
        evictMethodInvokeHandler.setNextHandler(afterEvictHandler);
        evictConditionHandler.handleRequest(aspectContext);
    }
}
