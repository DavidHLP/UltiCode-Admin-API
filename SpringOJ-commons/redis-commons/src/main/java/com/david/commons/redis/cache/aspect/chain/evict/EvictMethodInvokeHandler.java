package com.david.commons.redis.cache.aspect.chain.evict;

import com.david.commons.redis.cache.aspect.chain.AspectContext;
import com.david.commons.redis.cache.aspect.chain.Handler;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * 清除操作的方法执行处理器
 *
 * <p>负责执行原始方法，在缓存清除流程中处理业务方法调用
 *
 * @author David
 */
@Slf4j
@Component
public class EvictMethodInvokeHandler extends Handler {

    @Override
    public void handleRequest(AspectContext aspectContext) {
        // 如果方法已经执行过，不需要重复执行
        actionMethodInvoked(aspectContext);

        // 继续执行下一个处理器（通常是方法后清除处理器）
        executeHandle(aspectContext);
    }
}
