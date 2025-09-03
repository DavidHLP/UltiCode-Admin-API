package com.david.commons.redis.cache.aspect.chain.cacheable;

import com.david.commons.redis.cache.aspect.chain.AspectContext;
import com.david.commons.redis.cache.aspect.chain.Handler;
import com.david.commons.redis.cache.expression.CacheExpressionEvaluator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * 缓存条件检查处理器
 * 
 * <p>
 * 负责检查缓存操作的条件表达式，决定是否应该继续执行缓存操作
 * 
 * @author David
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConditionHandler extends Handler {

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
                    // 条件不满足：不进行任何缓存相关动作，但必须执行原方法并返回其结果
                    log.debug("缓存条件不满足，直接执行原方法并跳过缓存：{}", condition);
                    actionMethodInvoked(aspectContext);
                    aspectContext.setIsEnd(true);
                    return;
                }

                log.debug("缓存条件检查通过：{}", condition);
            } catch (Exception e) {
                // 表达式评估异常：稳妥起见也当作不满足条件处理，执行原方法以保证业务可用
                log.warn("评估缓存条件时出错，按不满足处理：{}，将直接执行原方法", condition, e);
                actionMethodInvoked(aspectContext);
                aspectContext.setIsEnd(true);
                return;
            }
        }

        // 条件通过，继续执行下一个处理器
        executeHandle(aspectContext);
    }
}
