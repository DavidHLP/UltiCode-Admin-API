package com.david.strategy;

import com.david.chain.utils.JudgmentContext;

/**
 * 策略接口：用于按语言类型执行具体的判题流程。
 */
public interface Strategy {
    /**
     * 执行评测流程。
     *
     * @param judgmentContext 评测上下文，包含题目、提交、语言、测试用例等信息
     * @return 是否评测通过
     */
    Boolean execute(JudgmentContext judgmentContext);
}
