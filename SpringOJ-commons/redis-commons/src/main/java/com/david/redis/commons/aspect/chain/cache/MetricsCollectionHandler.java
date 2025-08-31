package com.david.redis.commons.aspect.chain.cache;

import com.david.log.commons.LogUtils;
import com.david.redis.commons.annotation.RedisCacheable;
import com.david.redis.commons.annotation.RedisEvict;
import com.david.redis.commons.aspect.chain.AbstractAspectHandler;
import com.david.redis.commons.aspect.chain.AspectChain;
import com.david.redis.commons.aspect.chain.AspectContext;
import com.david.redis.commons.aspect.chain.AspectType;
import com.david.redis.commons.monitor.CacheMetricsCollector;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

/**
 * 指标收集处理器
 *
 * <p>负责收集缓存操作的性能指标和统计信息。支持缓存获取和缓存驱逐操作。
 *
 * <p>优化了 JDK11 风格，使用 Optional 和流式处理。
 */
@Component
public class MetricsCollectionHandler extends AbstractAspectHandler {

    private final CacheMetricsCollector metricsCollector;

    /**
     * 构造函数
     *
     * @param logUtils 日志工具类
     * @param metricsCollector 缓存指标收集器，用于记录缓存操作的各种统计信息
     */
    public MetricsCollectionHandler(LogUtils logUtils, CacheMetricsCollector metricsCollector) {
        super(logUtils);
        this.metricsCollector = metricsCollector;
    }

    /**
     * 获取支持的切面类型
     *
     * @return 支持的 AspectType 集合，包括 CACHE（缓存获取）和 CACHE_EVICT（缓存驱逐）
     */
    @Override
    protected Set<AspectType> getSupportedAspectTypes() {
        return Set.of(AspectType.CACHE, AspectType.CACHE_EVICT);
    }

    /**
     * 获取处理器的执行顺序
     *
     * @return 顺序值，数值越小优先级越高；此处为 90
     */
    @Override
    public int getOrder() {
        return 90;
    }

    /**
     * 判断当前上下文是否可以被该处理器处理
     *
     * @param context 切面上下文信息
     * @return 如果父类能处理且启用了指标收集，则返回 true
     */
    @Override
    public boolean canHandle(AspectContext context) {
        return super.canHandle(context) && isMetricsEnabled(context);
    }

    /**
     * 处理切面逻辑，收集指标信息
     *
     * @param context 切面上下文
     * @param chain 切面链，允许调用下一个处理器
     * @return 切面链的执行结果
     * @throws Throwable 处理链中可能抛出的异常
     */
    @Override
    public Object handle(AspectContext context, AspectChain chain) throws Throwable {
        try {
            collectMetrics(context);
        } catch (Exception e) {
            logException(context, "metrics_collection", e, "指标收集异常");
        }
        // 指标收集失败不影响业务逻辑
        return chain.proceed(context);
    }

    /**
     * 检查是否启用指标收集
     *
     * @param context 切面上下文
     * @return 如果方法上对应注解 enableMetrics 为 true，则返回 true，否则返回 false
     */
    private boolean isMetricsEnabled(AspectContext context) {
        return Optional.ofNullable(context.getMethod())
                .map(
                        method ->
                                switch (context.getAspectType()) {
                                    case CACHE ->
                                            Optional.ofNullable(
                                                            method.getAnnotation(
                                                                    RedisCacheable.class))
                                                    .map(RedisCacheable::enableMetrics)
                                                    .orElse(false);
                                    case CACHE_EVICT ->
                                            Optional.ofNullable(
                                                            method.getAnnotation(RedisEvict.class))
                                                    .map(RedisEvict::enableMetrics)
                                                    .orElse(false);
                                    default -> false;
                                })
                .orElse(false);
    }

    /**
     * 收集指标信息
     *
     * @param context 切面上下文
     */
    private void collectMetrics(AspectContext context) {
        String cacheKey =
                context.getAttribute(
                        CacheKeyGenerationHandler.CACHE_KEY_ATTR, String.class, "unknown");
        long executionTime = context.getExecutionTime();

        switch (context.getAspectType()) {
            case CACHE -> collectCacheMetrics(context, cacheKey, executionTime);
            case CACHE_EVICT -> collectEvictMetrics(context, cacheKey, executionTime);
            default -> {}
        }
    }

    /**
     * 收集缓存获取操作的指标
     *
     * <p>包括命中、未命中、设置缓存以及异常统计。
     *
     * @param context 切面上下文
     * @param cacheKey 缓存键
     * @param executionTime 方法执行耗时
     */
    private void collectCacheMetrics(AspectContext context, String cacheKey, long executionTime) {
        boolean cacheHit =
                context.getAttribute(CacheRetrievalHandler.CACHE_HIT_ATTR, Boolean.class, false);

        Optional.of(context)
                .filter(AspectContext::hasException)
                .ifPresentOrElse(
                        ctx -> metricsCollector.recordError("CACHE_GET", executionTime),
                        () -> {
                            if (cacheHit) {
                                metricsCollector.recordHit(cacheKey, executionTime);
                            } else {
                                metricsCollector.recordMiss(cacheKey, executionTime);
                                Optional.of(context)
                                        .filter(AspectContext::isMethodExecuted)
                                        .map(AspectContext::getResult)
                                        .ifPresent(
                                                result ->
                                                        metricsCollector.recordSet(
                                                                cacheKey, executionTime));
                            }
                        });
    }

    /**
     * 收集缓存驱逐操作的指标
     *
     * <p>包括删除缓存成功和异常统计。
     *
     * @param context 切面上下文
     * @param cacheKey 缓存键
     * @param executionTime 方法执行耗时
     */
    private void collectEvictMetrics(AspectContext context, String cacheKey, long executionTime) {
        Optional.of(context)
                .filter(AspectContext::hasException)
                .ifPresentOrElse(
                        ctx -> metricsCollector.recordError("CACHE_EVICT", executionTime),
                        () -> metricsCollector.recordDelete(cacheKey, executionTime));
    }
}
