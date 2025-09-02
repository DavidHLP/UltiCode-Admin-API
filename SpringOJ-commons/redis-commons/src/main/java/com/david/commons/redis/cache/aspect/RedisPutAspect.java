package com.david.commons.redis.cache.aspect;

import com.david.commons.redis.cache.CacheContext;
import com.david.commons.redis.cache.CacheMetadata;
import com.david.commons.redis.cache.aspect.chain.AspectContext;
import com.david.commons.redis.cache.aspect.chain.put.PutCacheUpdateHandler;
import com.david.commons.redis.cache.aspect.chain.put.PutConditionHandler;
import com.david.commons.redis.cache.aspect.chain.put.PutMethodInvokeHandler;
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
 * Redis @RedisPut 缓存更新 AOP 切面
 *
 * <p>专门处理 @RedisPut 注解的切面类。 负责拦截带有 @RedisPut 注解的方法，总是执行方法并更新缓存。
 *
 * <p>使用责任链模式处理缓存更新流程，不包含降级服务逻辑。
 *
 * @author David
 */
@Slf4j
@Aspect
@Component
@Order(101) // 设置切面执行顺序，确保在其他缓存切面之后执行
@RequiredArgsConstructor
public class RedisPutAspect {

    private final CacheAnnotationParser annotationParser;
    private final RedisCommonsProperties properties;
    private final PutConditionHandler conditionHandler;
    private final PutMethodInvokeHandler methodInvokeHandler;
    private final PutCacheUpdateHandler cacheUpdateHandler;

    /** 拦截带有 @RedisPut 注解的方法 */
    @Around("@annotation(com.david.commons.redis.cache.annotation.RedisPut)")
    public Object handlePutOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!properties.isEnabled()) {
            log.debug("Redis通用配置已禁用，跳过Put操作");
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        // 解析 @RedisPut 注解
        List<CacheMetadata> metadataList =
                annotationParser.parseCacheAnnotations(method).stream()
                        .filter(metadata -> metadata.operation() == CacheOperation.PUT)
                        .toList();

        if (metadataList.isEmpty()) {
            log.debug("方法 {} 上未找到 @RedisPut 注解", method.getName());
            return joinPoint.proceed();
        }

        log.debug(
                "正在处理方法 {} 的 @RedisPut 操作，共 {} 个操作",
                method.getName(),
                metadataList.size());

        // 创建缓存上下文
        CacheContext context =
                CacheContext.builder()
                        .method(method)
                        .args(args)
                        .target(joinPoint.getTarget())
                        .build();

        // 处理每个 @RedisPut 操作
        Object result = null;
        for (CacheMetadata metadata : metadataList) {
            try {
                result = handlePutOperation(joinPoint, metadata, context);
            } catch (Exception e) {
                log.error(
                        "处理方法 {} 的 @RedisPut 操作时发生错误，键：{}",
                        method.getName(),
                        metadata.key(),
                        e);
                // 继续处理其他操作，不中断整个流程
            }
        }

        return result;
    }

    /** 使用责任链处理单个 @RedisPut 操作 */
    private Object handlePutOperation(
            ProceedingJoinPoint joinPoint, CacheMetadata metadata, CacheContext context) {

        // 创建切面上下文
        AspectContext aspectContext = new AspectContext();
        aspectContext.setJoinPoint(joinPoint);
        aspectContext.setContext(context);
        aspectContext.setMetadata(metadata);

        conditionHandler.setNextHandler(methodInvokeHandler);
        methodInvokeHandler.setNextHandler(cacheUpdateHandler);

        // 启动责任链处理
        conditionHandler.handleRequest(aspectContext);

        // 返回最终结果
        return aspectContext.getFinalResult();
    }
}
