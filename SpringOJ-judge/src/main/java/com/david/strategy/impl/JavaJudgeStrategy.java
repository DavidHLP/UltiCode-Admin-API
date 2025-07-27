package com.david.strategy.impl;

import com.david.service.ISandboxService;
import org.springframework.stereotype.Component;

/**
 * Java 语言判题策略
 */
@Component
public class JavaJudgeStrategy extends DefaultJudgeStrategy {
    public JavaJudgeStrategy(ISandboxService sandboxService) {
        super(sandboxService);
    }
}
