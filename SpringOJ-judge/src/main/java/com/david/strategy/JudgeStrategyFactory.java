package com.david.strategy;

import com.david.judge.enums.LanguageType;
import com.david.strategy.impl.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author david
 * @since 2023/12/5
 */
@Component
@RequiredArgsConstructor
public class JudgeStrategyFactory {
    private final DefaultJudgeStrategy defaultJudgeStrategy;
    private final JavaJudgeStrategy javaJudgeStrategy;

    public JudgeStrategy getStrategy(LanguageType language) {
        return switch (language) {
            case JAVA -> javaJudgeStrategy;
            default -> defaultJudgeStrategy;
        };
    }
}