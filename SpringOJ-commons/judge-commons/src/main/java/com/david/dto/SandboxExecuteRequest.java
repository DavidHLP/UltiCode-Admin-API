package com.david.dto;

import java.util.List;

import com.david.judge.enums.LanguageType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 沙箱执行请求DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SandboxExecuteRequest {
    /** 源代码 */
    private String sourceCode;

    /** 编程语言 */
    private LanguageType language;

    /** 时间限制(ms) */
    private Integer timeLimit;

    /** 内存限制(MB) */
    private Integer memoryLimit;

    /** 测试用例输入列表 */
    private List<String> inputs;

    /** 期望输出列表 */
    private List<String> expectedOutputs;

    /** 提交ID */
    private Long submissionId;
}
