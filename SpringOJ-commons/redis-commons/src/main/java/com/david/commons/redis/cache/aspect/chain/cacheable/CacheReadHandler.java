package com.david.commons.redis.cache.aspect.chain.cacheable;

import com.david.commons.redis.cache.aspect.chain.AspectContext;
import com.david.commons.redis.cache.aspect.chain.Handler;
import com.david.commons.redis.cache.aspect.operator.CacheReadOperator;
import com.david.commons.redis.cache.metrics.CacheMetricsCollector;
import com.david.commons.redis.cache.metrics.CacheOperationTimer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * 缓存读取处理器
 * 
 * <p>负责从缓存中读取数据，如果缓存命中则结束责任链处理
 * 
 * @author David
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheReadHandler extends Handler {

    private final CacheReadOperator readOperator;
    private final CacheMetricsCollector metricsCollector;

    @Override
    public void handleRequest(AspectContext aspectContext) {
        if (aspectContext.getMetadata() == null) {
            log.debug("缓存元数据为空，跳过缓存读取");
            executeHandle(aspectContext);
            return;
        }

        String keyPattern = aspectContext.getMetadata().key();
        CacheOperationTimer timer = metricsCollector.startOperation(
            aspectContext.getMetadata(), 
            aspectContext.getContext().getMethodName()
        );

        try {
            // 从缓存中获取值
            Object cachedValue = readOperator.readCache(
                aspectContext.getMetadata(), 
                aspectContext.getContext()
            );
            
            if (cachedValue != null) {
                log.debug("缓存命中，键模式：{}", keyPattern);
                metricsCollector.recordSuccess(timer, true);
                
                // 标记缓存命中并结束处理链
                aspectContext.setCacheHit(cachedValue);
                return;
            } else {
                log.debug("缓存未命中，键模式：{}", keyPattern);
                metricsCollector.recordSuccess(timer, false);
            }
        } catch (Exception e) {
            metricsCollector.recordFailure(timer, e);
            log.warn("缓存读取失败，键模式：{}，将继续执行方法", keyPattern, e);
        }

        // 缓存未命中或读取失败，继续执行下一个处理器
        executeHandle(aspectContext);
    }
}