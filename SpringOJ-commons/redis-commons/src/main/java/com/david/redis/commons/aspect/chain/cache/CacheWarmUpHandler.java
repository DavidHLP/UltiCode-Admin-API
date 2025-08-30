package com.david.redis.commons.aspect.chain.cache;

import com.david.log.commons.core.LogUtils;
import com.david.redis.commons.annotation.RedisCacheable;
import com.david.redis.commons.aspect.chain.AbstractAspectHandler;
import com.david.redis.commons.aspect.chain.AspectChain;
import com.david.redis.commons.aspect.chain.AspectContext;
import com.david.redis.commons.aspect.chain.AspectType;
import com.david.redis.commons.enums.WarmUpPriority;
import com.david.redis.commons.manager.CacheWarmUpManager;

import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 缓存预热处理器
 *
 * <p>
 * 负责触发缓存预热操作，提升缓存命中率。
 *
 * @author David
 */
@Component
public class CacheWarmUpHandler extends AbstractAspectHandler {

    private final CacheWarmUpManager warmUpManager;

    public CacheWarmUpHandler(LogUtils logUtils, CacheWarmUpManager warmUpManager) {
        super(logUtils);
        this.warmUpManager = warmUpManager;
    }

    @Override
    protected Set<AspectType> getSupportedAspectTypes() {
        return Set.of(AspectType.CACHE);
    }

    @Override
    public int getOrder() {
        return 80; // 在缓存存储之后执行
    }

    @Override
    public boolean canHandle(AspectContext context) {
        if (!super.canHandle(context)) {
            return false;
        }

        if (context.getMethod() == null) {
            return false; // 虚拟上下文不需要预热
        }

        RedisCacheable annotation = context.getMethod().getAnnotation(RedisCacheable.class);
        return annotation != null
                && annotation.warmUp()
                && context.isMethodExecuted()
                && !context.hasException();
    }

    @Override
    public Object handle(AspectContext context, AspectChain chain) throws Throwable {
        try {
            triggerWarmUp(context);
            return chain.proceed(context);

        } catch (Exception e) {
            logException(context, "cache_warmup", e, "缓存预热异常: " + context.getMethod().getName());
            // 预热失败不影响业务逻辑
            return chain.proceed(context);
        }
    }

    /**
     * 触发缓存预热
     *
     * @param context 切面上下文
     */
    private void triggerWarmUp(AspectContext context) {
        String cacheKey = context.getAttribute(CacheKeyGenerationHandler.CACHE_KEY_ATTR);

        if (context.getMethod() == null || cacheKey == null) {
            return;
        }

        RedisCacheable annotation = context.getMethod().getAnnotation(RedisCacheable.class);
        if (annotation == null) {
            return;
        }

        try {
            // 提取键模式用于预热
            String pattern = extractKeyPattern(cacheKey);
            WarmUpPriority priority = annotation.warmUpPriority();

            // 触发预热任务
            warmUpManager.triggerWarmUp(pattern, priority);

            // 预热触发成功，无需记录日志

        } catch (Exception e) {
            logException(context, "cache_warmup", e, "缓存预热失败: " + cacheKey);
        }
    }

    /**
     * 从缓存键提取模式
     *
     * @param cacheKey 缓存键
     * @return 键模式
     */
    private String extractKeyPattern(String cacheKey) {
        // 简单的模式提取：将具体值替换为通配符
        return cacheKey.replaceAll(":\\d+", ":*").replaceAll(":[a-zA-Z0-9]+$", ":*");
    }
}
