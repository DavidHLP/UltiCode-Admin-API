package com.david.commons.redis.cache.aspect.chain.put;

import com.david.commons.redis.cache.aspect.chain.AspectContext;
import com.david.commons.redis.cache.aspect.chain.Handler;
import com.david.commons.redis.cache.aspect.operator.CacheWriteOperator;
import com.david.commons.redis.cache.expression.CacheExpressionEvaluator;
import com.david.commons.redis.cache.metrics.CacheMetricsCollector;
import com.david.commons.redis.cache.metrics.CacheOperationTimer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * RedisPut 缓存更新处理器
 * 
 * <p>负责将方法执行结果更新到缓存中，只有在条件满足且方法执行成功时才会更新
 * 
 * @author David
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutCacheUpdateHandler extends Handler {

    private final CacheWriteOperator writeOperator;
    private final CacheMetricsCollector metricsCollector;
    private final CacheExpressionEvaluator expressionEvaluator;

    @Override
    public void handleRequest(AspectContext aspectContext) {
        // 如果方法未执行，不更新缓存
        if (!aspectContext.getMethodInvoked()) {
            log.debug("方法未执行，跳过缓存更新");
            executeHandle(aspectContext);
            return;
        }

        if (aspectContext.getMetadata() == null) {
            log.debug("缓存元数据为空，跳过缓存更新");
            executeHandle(aspectContext);
            return;
        }

        // 检查 unless 条件
        if (shouldSkipCacheUpdate(aspectContext)) {
            log.debug("unless条件不满足，跳过缓存更新");
            executeHandle(aspectContext);
            return;
        }

        String keyPattern = aspectContext.getMetadata().key();
        CacheOperationTimer timer = metricsCollector.startOperation(
            aspectContext.getMetadata(), 
            aspectContext.getContext().getMethodName()
        );

        try {
            // 将方法结果更新到缓存
            writeOperator.writePutValue(
                aspectContext.getMetadata(), 
                aspectContext.getContext()
            );
            
            metricsCollector.recordSuccess(timer, false);
            log.debug("缓存更新成功，键模式：{}", keyPattern);
            
        } catch (Exception e) {
            metricsCollector.recordFailure(timer, e);
            log.warn("缓存更新失败，键模式：{}", keyPattern, e);
        }

        // 继续执行下一个处理器（如果有）
        executeHandle(aspectContext);
    }

    /**
     * 检查是否应该跳过缓存更新（基于 unless 条件）
     */
    private boolean shouldSkipCacheUpdate(AspectContext aspectContext) {
        String unless = aspectContext.getMetadata().unless();
        if (unless.isEmpty()) {
            return false;
        }

        try {
            return expressionEvaluator.evaluateCondition(
                unless, 
                aspectContext.getContext(), 
                aspectContext.getMethodResult()
            );
        } catch (Exception e) {
            log.warn("评估unless条件时出错，跳过缓存更新：{}", unless, e);
            return true;
        }
    }
}
