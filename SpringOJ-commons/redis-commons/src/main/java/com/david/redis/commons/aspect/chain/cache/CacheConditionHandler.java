package com.david.redis.commons.aspect.chain.cache;

import com.david.log.commons.LogUtils;
import com.david.redis.commons.annotation.RedisCacheable;
import com.david.redis.commons.annotation.RedisEvict;
import com.david.redis.commons.aspect.chain.AbstractAspectHandler;
import com.david.redis.commons.aspect.chain.AspectChain;
import com.david.redis.commons.aspect.chain.AspectContext;
import com.david.redis.commons.aspect.chain.AspectType;
import com.david.redis.commons.aspect.chain.utils.CacheConditionEvaluator;

import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 缓存条件评估处理器
 *
 * <p>
 * 负责评估缓存和驱逐条件，决定是否继续执行后续处理器。
 * 
 * @author David
 */
@Component
public class CacheConditionHandler extends AbstractAspectHandler {

    public static final String CACHE_CONDITION_MET_ATTR = "cache.condition.met";
    public static final String EVICT_CONDITION_MET_ATTR = "evict.condition.met";

    private final CacheConditionEvaluator conditionEvaluator;

    public CacheConditionHandler(LogUtils logUtils, CacheConditionEvaluator conditionEvaluator) {
        super(logUtils);
        this.conditionEvaluator = conditionEvaluator;
    }

    @Override
    protected Set<AspectType> getSupportedAspectTypes() {
        return Set.of(AspectType.CACHE, AspectType.CACHE_EVICT);
    }

    @Override
    public int getOrder() {
        return 5; // 最先执行的处理器之一
    }

    @Override
    public Object handle(AspectContext context, AspectChain chain) throws Throwable {
        try {
            // 评估缓存条件
            var conditionMet = evaluateCondition(context);

            // 设置相应的条件标志
            setConditionFlag(context, conditionMet);

            return chain.proceed(context);

        } catch (Exception e) {
            logException(context, "cache_condition_evaluation", e, "缓存条件评估失败: " + context.getMethod().getName());
            throw e;
        }
    }

    /**
     * 评估缓存和驱逐条件
     *
     * @param context 切面上下文
     * @return true 如果条件满足
     */
    private boolean evaluateCondition(AspectContext context) {
        return switch (context.getAspectType()) {
            case CACHE -> evaluateCacheCondition(context);
            case CACHE_EVICT -> evaluateEvictCondition(context);
            default -> true; // 默认条件满足
        };
    }

    /**
     * 设置缓存或驱逐条件标志
     *
     * @param context      切面上下文
     * @param conditionMet 条件是否满足
     */
    private void setConditionFlag(AspectContext context, boolean conditionMet) {
        switch (context.getAspectType()) {
            case CACHE -> context.setAttribute(CACHE_CONDITION_MET_ATTR, conditionMet);
            case CACHE_EVICT -> context.setAttribute(EVICT_CONDITION_MET_ATTR, conditionMet);
            default -> throw new IllegalStateException("Unexpected aspect type: " + context.getAspectType());
        }
    }

    /**
     * 评估缓存条件
     *
     * @param context 切面上下文
     * @return true 如果条件满足
     */
    private boolean evaluateCacheCondition(AspectContext context) {
        var annotation = context.getMethod().getAnnotation(RedisCacheable.class);
        return evaluateConditionByAnnotation(annotation.condition(), context);
    }

    /**
     * 评估驱逐条件
     *
     * @param context 切面上下文
     * @return true 如果条件满足
     */
    private boolean evaluateEvictCondition(AspectContext context) {
        var annotation = context.getMethod().getAnnotation(RedisEvict.class);
        return evaluateConditionByAnnotation(annotation.condition(), context);
    }

    /**
     * 评估条件是否满足
     *
     * @param condition 条件表达式
     * @param context   切面上下文
     * @return true 如果条件满足
     */
    private boolean evaluateConditionByAnnotation(String condition, AspectContext context) {
        return condition.isEmpty() || conditionEvaluator.evaluateCondition(
                condition,
                context.getMethod(),
                context.getArgs(),
                context.getResult());
    }
}
