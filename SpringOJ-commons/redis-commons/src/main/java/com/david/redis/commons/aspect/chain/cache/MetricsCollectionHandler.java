package com.david.redis.commons.aspect.chain.cache;

import com.david.log.commons.core.LogUtils;
import com.david.redis.commons.annotation.RedisCacheable;
import com.david.redis.commons.annotation.RedisEvict;
import com.david.redis.commons.aspect.chain.AbstractAspectHandler;
import com.david.redis.commons.aspect.chain.AspectChain;
import com.david.redis.commons.aspect.chain.AspectContext;
import com.david.redis.commons.aspect.chain.AspectType;
import com.david.redis.commons.monitor.CacheMetricsCollector;

import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 指标收集处理器
 *
 * <p>
 * 负责收集缓存操作的性能指标和统计信息。
 *
 * @author David
 */
@Component
public class MetricsCollectionHandler extends AbstractAspectHandler {

    private final CacheMetricsCollector metricsCollector;

    public MetricsCollectionHandler(LogUtils logUtils, CacheMetricsCollector metricsCollector) {
        super(logUtils);
        this.metricsCollector = metricsCollector;
    }

    @Override
    protected Set<AspectType> getSupportedAspectTypes() {
        return Set.of(AspectType.CACHE, AspectType.CACHE_EVICT);
    }

    @Override
    public int getOrder() {
        return 90; // 最后执行，收集所有操作的指标
    }

    @Override
    public boolean canHandle(AspectContext context) {
        if (!super.canHandle(context)) {
            return false;
        }

        // 检查是否启用了指标收集
        return isMetricsEnabled(context);
    }

    @Override
    public Object handle(AspectContext context, AspectChain chain) throws Throwable {
        try {
            collectMetrics(context);
            return chain.proceed(context);

        } catch (Exception e) {
            logException(context, "metrics_collection", e, "指标收集异常");
            // 指标收集失败不影响业务逻辑
            return chain.proceed(context);
        }
    }

    /**
     * 检查是否启用指标收集
     *
     * @param context 切面上下文
     * @return true 如果启用指标收集
     */
    private boolean isMetricsEnabled(AspectContext context) {
        if (context.getMethod() == null) {
            return false; // 虚拟上下文不需要指标收集
        }

        if (context.getAspectType() == AspectType.CACHE) {
            RedisCacheable annotation = context.getMethod().getAnnotation(RedisCacheable.class);
            return annotation != null && annotation.enableMetrics();
        } else if (context.getAspectType() == AspectType.CACHE_EVICT) {
            RedisEvict annotation = context.getMethod().getAnnotation(RedisEvict.class);
            return annotation != null && annotation.enableMetrics();
        }
        return false;
    }

    /**
     * 收集指标
     *
     * @param context 切面上下文
     */
    private void collectMetrics(AspectContext context) {
        String cacheKey = context.getAttribute(CacheKeyGenerationHandler.CACHE_KEY_ATTR, "unknown");
        long executionTime = context.getExecutionTime();

        if (context.getAspectType() == AspectType.CACHE) {
            collectCacheMetrics(context, cacheKey, executionTime);
        } else if (context.getAspectType() == AspectType.CACHE_EVICT) {
            collectEvictMetrics(context, cacheKey, executionTime);
        }
    }

    /**
     * 收集缓存操作指标
     *
     * @param context       切面上下文
     * @param cacheKey      缓存键
     * @param executionTime 执行时间
     */
    private void collectCacheMetrics(AspectContext context, String cacheKey, long executionTime) {
        Boolean cacheHit = context.getAttribute(CacheRetrievalHandler.CACHE_HIT_ATTR, false);

        if (context.hasException()) {
            // 记录错误指标
            metricsCollector.recordError("CACHE_GET", executionTime);
        } else if (cacheHit) {
            // 记录缓存命中指标
            metricsCollector.recordHit(cacheKey, executionTime);
        } else {
            // 记录缓存未命中指标
            metricsCollector.recordMiss(cacheKey, executionTime);

            // 如果缓存了结果，记录设置指标
            if (context.isMethodExecuted() && context.getResult() != null) {
                metricsCollector.recordSet(cacheKey, executionTime);
            }
        }
    }

    /**
     * 收集驱逐操作指标
     *
     * @param context       切面上下文
     * @param cacheKey      缓存键
     * @param executionTime 执行时间
     */
    private void collectEvictMetrics(AspectContext context, String cacheKey, long executionTime) {
        if (context.hasException()) {
            metricsCollector.recordError("CACHE_EVICT", executionTime);
        } else {
            // 记录驱逐操作指标
            metricsCollector.recordDelete(cacheKey, executionTime);
        }
    }
}
