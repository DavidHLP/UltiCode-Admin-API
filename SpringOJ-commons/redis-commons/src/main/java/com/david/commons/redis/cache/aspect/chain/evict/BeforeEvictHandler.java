package com.david.commons.redis.cache.aspect.chain.evict;

import com.david.commons.redis.cache.aspect.chain.AspectContext;
import com.david.commons.redis.cache.aspect.chain.Handler;
import com.david.commons.redis.cache.aspect.operator.CacheEvictOperator;
import com.david.commons.redis.cache.metrics.CacheMetricsCollector;
import com.david.commons.redis.cache.metrics.CacheOperationTimer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * 方法前缓存清除处理器
 * 
 * <p>负责在方法执行前清除缓存，处理 beforeInvocation=true 的清除操作
 * 
 * @author David
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BeforeEvictHandler extends Handler {

    private final CacheEvictOperator evictOperator;
    private final CacheMetricsCollector metricsCollector;

    @Override
    public void handleRequest(AspectContext aspectContext) {
        if (aspectContext.getMetadata() == null) {
            log.debug("缓存元数据为空，跳过方法前清除");
            executeHandle(aspectContext);
            return;
        }

        // 只处理 beforeInvocation=true 的清除操作
        if (!aspectContext.getMetadata().beforeInvocation()) {
            log.debug("非方法前清除操作，跳过：beforeInvocation=false");
            executeHandle(aspectContext);
            return;
        }

        String keyPattern = aspectContext.getMetadata().key();
        CacheOperationTimer timer = metricsCollector.startOperation(
            aspectContext.getMetadata(), 
            aspectContext.getContext().getMethodName()
        );

        try {
            // 执行缓存清除操作
            evictOperator.evictCache(
                aspectContext.getMetadata(), 
                aspectContext.getContext()
            );
            
            metricsCollector.recordSuccess(timer, false);
            log.debug("方法前缓存清除成功，键模式：{}", keyPattern);
            
        } catch (Exception e) {
            metricsCollector.recordFailure(timer, e);
            log.warn("方法前缓存清除失败，键模式：{}", keyPattern, e);
            // 不中断处理链，继续执行
        }

        // 继续执行下一个处理器
        executeHandle(aspectContext);
    }
}
