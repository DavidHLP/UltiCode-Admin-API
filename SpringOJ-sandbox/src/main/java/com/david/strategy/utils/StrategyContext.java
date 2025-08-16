package com.david.strategy.utils;

import com.david.chain.utils.JudgmentContext;
import com.david.strategy.Strategy;

import java.util.Objects;

/** 策略上下文：持有具体 {@link Strategy} 实例并对外提供统一的执行入口。 */
public record StrategyContext(Strategy strategy) {
    /**
     * 使用给定策略创建上下文。
     *
     * @param strategy 具体语言的策略实现，不能为空
     */
    public StrategyContext(Strategy strategy) {
        this.strategy = Objects.requireNonNull(strategy, "strategy 不能为空");
    }

    /**
     * 执行策略。
     *
     * @param judgmentContext 评测上下文
     * @return 是否评测通过
     */
    public Boolean execute(JudgmentContext judgmentContext) {
        if (judgmentContext == null) {
            return false;
        }
        return strategy.execute(judgmentContext);
    }
}
