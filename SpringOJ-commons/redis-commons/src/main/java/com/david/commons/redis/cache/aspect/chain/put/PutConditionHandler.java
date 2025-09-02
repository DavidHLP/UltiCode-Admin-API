package com.david.commons.redis.cache.aspect.chain.put;

import com.david.commons.redis.cache.aspect.chain.AspectContext;
import com.david.commons.redis.cache.aspect.chain.Handler;
import com.david.commons.redis.cache.expression.CacheExpressionEvaluator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * RedisPut 条件检查处理器
 * 
 * <p>
 * 负责检查 @RedisPut 操作的条件表达式，决定是否应该继续执行缓存更新操作
 * 
 * @author David
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutConditionHandler extends Handler {

    private final CacheExpressionEvaluator expressionEvaluator;

    @Override
    public void handleRequest(AspectContext aspectContext) {
        if (aspectContext.getMetadata() == null) {
            log.debug("缓存元数据为空，跳过条件检查");
            executeHandle(aspectContext);
            return;
        }

        // 检查 condition 条件
        String condition = aspectContext.getMetadata().condition();
        if (!condition.isEmpty()) {
            try {
                boolean conditionResult = expressionEvaluator.evaluateCondition(
                        condition,
                        aspectContext.getContext(),
                        null);

                if (!conditionResult) {
                    log.debug("RedisPut 条件不满足，跳过缓存更新操作：{}", condition);
                    aspectContext.setIsEnd(true);
                    return;
                }

                log.debug("RedisPut 条件检查通过：{}", condition);
            } catch (Exception e) {
                log.warn("评估 RedisPut 条件时出错，跳过缓存操作：{}", condition, e);
                aspectContext.setIsEnd(true);
                return;
            }
        }

        // 条件通过，继续执行下一个处理器
        executeHandle(aspectContext);
    }
}
