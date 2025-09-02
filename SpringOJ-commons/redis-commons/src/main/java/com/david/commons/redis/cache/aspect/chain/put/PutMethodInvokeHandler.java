package com.david.commons.redis.cache.aspect.chain.put;

import com.david.commons.redis.cache.aspect.chain.AspectContext;
import com.david.commons.redis.cache.aspect.chain.Handler;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * RedisPut 方法执行处理器
 *
 * <p>负责执行原始方法，@RedisPut 总是需要执行原方法来获取最新结果
 *
 * @author David
 */
@Slf4j
@Component
public class PutMethodInvokeHandler extends Handler {

    @Override
    public void handleRequest(AspectContext aspectContext) {
        // 如果方法已经执行过，不需要重复执行
        actionMethodInvoked(aspectContext);

        // 继续执行下一个处理器（通常是缓存更新处理器）
        executeHandle(aspectContext);
    }
}
