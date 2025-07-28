package com.david.strategy;

import com.david.judge.enums.LanguageType;
import com.david.sandbox.dto.SandboxExecuteRequest;

/**
 * 简化的判题策略接口 - 仅用于构建沙箱请求的语言特定逻辑
 */
public interface JudgeStrategy {

    /**
     * 获取支持的语言类型
     */
    LanguageType getSupportedLanguage();

    /**
     * 为特定语言定制沙箱请求（可选的语言特定优化）
     * @param request 基础沙箱请求
     * @return 定制后的沙箱请求
     */
    default SandboxExecuteRequest customizeRequest(SandboxExecuteRequest request) {
        return request; // 默认不做修改
    }
}
