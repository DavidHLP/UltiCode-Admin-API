package com.david.strategy;

import com.david.strategy.impl.JavaJudgeStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JudgeStrategyFactory {

    @Autowired
    private JavaJudgeStrategy javaJudgeStrategy;

    public JudgeStrategy getStrategy(String language) {
        // 目前仅支持Java，后续可扩展
        if ("java".equalsIgnoreCase(language)) {
            return javaJudgeStrategy;
        }
        throw new IllegalArgumentException("不支持的编程语言: " + language);
    }
}
