package com.david.submission.vo;

import com.david.enums.JudgeStatus;
import com.david.enums.LanguageType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 判题提交的详细视图对象（VO）。
 *
 * <p>
 * 用于在接口中返回一次提交的完整详情信息，包括：基本标识、
 * 用户与题目关联、编程语言、判题状态、资源消耗以及源码、编译与判题日志。
 * </p>
 *
 * <p>
 * 字段含义遵循后端判题系统的统一定义：
 * <ul>
 * <li><b>timeUsed</b> 以毫秒（ms）为单位。</li>
 * <li><b>memoryUsed</b> 以 KB 为单位。</li>
 * <li><b>language</b> 与 {@link LanguageType} 对应。</li>
 * <li><b>status</b> 与 {@link JudgeStatus} 对应。</li>
 * </ul>
 * </p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionDetailVo {
    /**
     * 提交记录主键 ID。
     */
    private Long id;

    /**
     * 提交用户的 ID。
     */
    private Long userId;

    /**
     * 题目的 ID。
     */
    private Long problemId;

    /**
     * 编程语言类型，取值见 {@link LanguageType}。
     */
    private LanguageType language;

    /**
     * 判题状态，取值见 {@link JudgeStatus}。
     */
    private JudgeStatus status;

    /**
     * 程序执行耗时（毫秒）。
     */
    private Integer timeUsed;

    /**
     * 程序执行内存消耗（KB）。
     */
    private Integer memoryUsed;

    /**
     * 用户提交的源代码。
     */
    private String sourceCode;

    /**
     * 编译阶段的详细输出信息（如错误/警告日志）。
     */
    private String compileInfo;

    /**
     * 判题阶段的详细信息（例如各测试点结果、错误信息等）。
     * 具体结构由判题服务返回，通常为可读文本或 JSON 字符串。
     */
    private String judgeInfo;

    /** 首个错误测试用例的 ID（如有）。 */
    private Long errorTestCaseId;

    /** 错误测试用例的实际输出，可读文本。 */
    private String errorTestCaseOutput;

    /** 错误测试用例的期望输出，可读文本。 */
    private String errorTestCaseExpectOutput;
}
