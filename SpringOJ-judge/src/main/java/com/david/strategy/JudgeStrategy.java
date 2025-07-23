package com.david.strategy;

import com.david.dto.JudgeResult;

/**
 * @author david
 * @since 2023/12/5
 */
public interface JudgeStrategy {
    JudgeResult execute(JudgeContext context);
}
