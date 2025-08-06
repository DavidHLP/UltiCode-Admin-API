package com.david.strategy.code.impl;

import org.springframework.stereotype.Component;

import com.david.dto.SandboxExecuteRequest;
import com.david.judge.enums.LanguageType;
import com.david.strategy.code.JudgeStrategy;

import lombok.extern.slf4j.Slf4j;

/**
 * Java 语言判题策略 - 简化版本
 */
@Slf4j
@Component
public class JavaJudgeStrategy implements JudgeStrategy {

    @Override
    public LanguageType getSupportedLanguage() {
        return LanguageType.JAVA;
    }

    @Override
    public SandboxExecuteRequest customizeRequest(SandboxExecuteRequest request) {
        // Java特定的优化配置
        if (request.getTimeLimit() == null || request.getTimeLimit() <= 0) {
            request.setTimeLimit(5000); // 默认5秒超时
        }

        if (request.getMemoryLimit() == null || request.getMemoryLimit() <= 0) {
            request.setMemoryLimit(256); // 默认256MB内存限制
        }

        log.debug("Java沙箱请求已定制: timeLimit={}ms, memoryLimit={}MB",
            request.getTimeLimit(), request.getMemoryLimit());

        return request;
    }
}
