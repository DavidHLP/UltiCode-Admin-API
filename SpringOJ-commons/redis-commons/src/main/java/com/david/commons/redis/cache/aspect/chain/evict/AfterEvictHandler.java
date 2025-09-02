package com.david.commons.redis.cache.aspect.chain.evict;

import com.david.commons.redis.cache.aspect.chain.AspectContext;
import com.david.commons.redis.cache.aspect.chain.Handler;
import com.david.commons.redis.cache.aspect.operator.CacheEvictOperator;
import com.david.commons.redis.cache.expression.CacheExpressionEvaluator;
import com.david.commons.redis.cache.metrics.CacheMetricsCollector;
import com.david.commons.redis.cache.metrics.CacheOperationTimer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * 方法后缓存清除处理器
 *
 * <p>负责在方法执行后清除缓存，处理 beforeInvocation=false 的清除操作
 *
 * @author David
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AfterEvictHandler extends Handler {

    private final CacheEvictOperator evictOperator;
    private final CacheMetricsCollector metricsCollector;
    private final CacheExpressionEvaluator expressionEvaluator;

    @Override
    public void handleRequest(AspectContext aspectContext) {
        // 只处理 beforeInvocation=false 的清除操作
        if (aspectContext.getMetadata().beforeInvocation()) {
            log.debug("非方法后清除操作，跳过：beforeInvocation=true");
            executeHandle(aspectContext);
            return;
        }

        // 如果方法未执行，不执行方法后清除
        if (!aspectContext.getMethodInvoked()) {
            log.debug("方法未执行，跳过方法后清除");
            executeHandle(aspectContext);
            return;
        }

        // 检查 unless 条件（方法后清除可能需要根据结果判断）
        if (shouldSkipAfterEvict(aspectContext)) {
            log.debug("unless条件不满足，跳过方法后清除");
            executeHandle(aspectContext);
            return;
        }

        String keyPattern = aspectContext.getMetadata().key();
        CacheOperationTimer timer =
                metricsCollector.startOperation(
                        aspectContext.getMetadata(), aspectContext.getContext().getMethodName());

        try {
            // 执行缓存清除操作
            evictOperator.evictCache(aspectContext.getMetadata(), aspectContext.getContext());

            metricsCollector.recordSuccess(timer, false);
            log.debug("方法后缓存清除成功，键模式：{}", keyPattern);

        } catch (Exception e) {
            metricsCollector.recordFailure(timer, e);
            log.warn("方法后缓存清除失败，键模式：{}", keyPattern, e);
            // 不中断处理链，继续执行
        }

        // 继续执行下一个处理器（如果有）
        executeHandle(aspectContext);
    }

    /** 检查是否应该跳过方法后清除（基于 unless 条件） */
    private boolean shouldSkipAfterEvict(AspectContext aspectContext) {
        String unless = aspectContext.getMetadata().unless();
        if (unless.isEmpty()) {
            return false;
        }

        try {
            return expressionEvaluator.evaluateCondition(
                    unless, aspectContext.getContext(), aspectContext.getMethodResult());
        } catch (Exception e) {
            log.warn("评估unless条件时出错，跳过方法后清除：{}", unless, e);
            return true;
        }
    }
}
