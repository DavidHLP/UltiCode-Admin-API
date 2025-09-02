package com.david.commons.redis.cache.aspect.cacheable;

import com.david.commons.redis.cache.CacheMetadata;
import com.david.commons.redis.cache.aspect.CacheContext;
import com.david.commons.redis.cache.expression.CacheExpressionEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CheckExecuteOperation {
    private final CacheExpressionEvaluator expressionEvaluator;
    /** 判断是否应该执行缓存操作 */
    public boolean shouldExecuteOperation(
            CacheMetadata metadata, CacheContext context, Object result) {
        try {
            // 检查 condition 条件
            if (!metadata.getCondition().isEmpty()) {
                boolean conditionResult =
                        expressionEvaluator.evaluateCondition(
                                metadata.getCondition(), context, result);
                if (!conditionResult) {
                    log.debug("由于条件不满足，缓存操作被跳过：{}", metadata.getCondition());
                    return false;
                }
            }

            // 检查 unless 条件（仅对有返回值的操作有效）
            if (result != null && !metadata.getUnless().isEmpty()) {
                boolean unlessResult =
                        expressionEvaluator.evaluateCondition(
                                metadata.getUnless(), context, result);
                if (unlessResult) {
                    log.debug("由于 unless 条件不满足，缓存操作被跳过：{}", metadata.getUnless());
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            log.warn("评估缓存操作条件时出错，跳过操作", e);
            return false;
        }
    }
}
