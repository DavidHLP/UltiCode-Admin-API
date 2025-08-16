package com.david.chain.utils;

import com.david.enums.JudgeStatus;
import com.david.enums.LanguageType;
import com.david.enums.interfaces.LimitType;
import com.david.testcase.TestCase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 评测上下文（传输与链路共享对象）。
 *
 * <p>在策略与责任链各环节之间传递必要信息：题目信息、提交信息、语言、源代码、生成的可运行代码、编译信息、运行资源消耗、判题结论与测试用例等。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JudgmentContext {
    /** 题目ID（必填） */
    private Long problemId;

    /** 提交ID（必填） */
    private Long submissionId;

    /** 解题函数名（必填）：例如 twoSum */
    private String solutionFunctionName;

    /** 提交用户ID（可选） */
    private Long userId;

    private LimitType limitType;

    /** 运行阶段统计的内存使用（可选）。 存储为无单位的数字字符串，按约定单位 MB 解释。例如："128" 表示 128MB。 */
    private String memoryUsed;

    /** 编译/运行阶段统计的时间消耗（可选）。 存储为无单位的数字字符串，按约定单位 ms 解释。例如："1000" 表示 1000ms。 */
    private String timeUsed;

    /** 编译诊断信息（编译阶段产出），如：OK 或 错误堆栈 */
    private String compileInfo;

    /** 判题结论（对比阶段产出），包含是否通过与原因 */
    private String judgeInfo;

    /** 编程语言（必填），决定具体策略 */
    private LanguageType language;

    /** 用户提交的原始解题代码（必填） */
    private String solutionCode;

    /** 生成的可运行主类代码（格式化阶段产出，包含 Main 与 Solution） */
    private String runCode;

    /** 判题状态（各阶段设置），详见 JudgeStatus */
    private JudgeStatus judgeStatus;

    // 只记录第一个测试用例的错误信息
    /** 错误测试用例的Id */
    private Long errorTestCaseId;

    /** 错误测试用例的输出 */
    private String errorTestCaseOutput;

    /** 错误测试用例的期望输出 */
    private String errorTestCaseExpectOutput;

    /** 测试用例列表（必填，至少1条） */
    private List<TestCase> testCases;

    private List<JudgmentResult> judgmentResults;
}
