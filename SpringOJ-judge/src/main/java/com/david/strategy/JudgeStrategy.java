package com.david.strategy;

import com.david.domain.dto.JudgeContext;
import com.david.domain.dto.JudgeResult;

public interface JudgeStrategy {
    /**
     * 执行判题
     * @param context 判题上下文
     * @return 判题结果
     */
    JudgeResult execute(JudgeContext context);
}
