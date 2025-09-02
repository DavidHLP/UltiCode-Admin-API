package com.david.commons.redis.cache.aspect.chain.cacheable;

import com.david.commons.redis.cache.aspect.chain.AspectContext;
import com.david.commons.redis.cache.aspect.chain.Handler;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * 方法执行处理器
 *
 * <p>负责执行原始方法，当缓存未命中时调用此处理器
 *
 * @author David
 */
@Slf4j
@Component
public class MethodInvokeHandler extends Handler {

    @Override
    public void handleRequest(AspectContext aspectContext) {
        // 如果已经缓存命中，不需要执行方法
        if (aspectContext.getCacheHit()) {
            log.debug("缓存已命中，跳过方法执行");
            executeHandle(aspectContext);
            return;
        }

        actionMethodInvoked(aspectContext);

        // 继续执行下一个处理器（通常是缓存写入处理器）
        executeHandle(aspectContext);
    }
}
