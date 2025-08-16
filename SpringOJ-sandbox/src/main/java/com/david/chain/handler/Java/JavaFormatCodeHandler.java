package com.david.chain.handler.Java;

import com.david.chain.Handler;
import com.david.chain.utils.JudgmentContext;
import com.david.chain.utils.JudgmentResult;
import com.david.enums.JudgeStatus;
import com.david.testcase.TestCase;
import com.david.testcase.TestCaseInput;
import com.david.testcase.TestCaseOutput;
import com.david.utils.java.JavaCodeUtils;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class JavaFormatCodeHandler extends Handler {
    @Override
    public Boolean handleRequest(JudgmentContext judgmentContext) {
        try {
            if (judgmentContext == null) {
                return false;
            }

            // 进入判题流程
            judgmentContext.setJudgeStatus(JudgeStatus.JUDGING);

            // 校验必要字段
            String solutionCode = judgmentContext.getSolutionCode();
            if (solutionCode == null || solutionCode.trim().isEmpty()) {
                return createErrorAndExit(judgmentContext, "提交的 solutionCode 为空", null);
            }
            List<TestCase> testCases = judgmentContext.getTestCases();
            if (testCases == null || testCases.isEmpty()) {
                return createErrorAndExit(judgmentContext, "未找到测试用例", null);
            }

            // 校验所有测试用例并收集数据
            List<List<String>> allInputTypes = new ArrayList<>();
            List<List<String>> allInputValues = new ArrayList<>();
            List<String> allExpectedOutputs = new ArrayList<>();
            String outputType = null;

            for (TestCase testCase : testCases) {
                TestCaseOutput testCaseOutput = testCase.getTestCaseOutput();
                if (testCaseOutput == null) {
                    return createErrorAndExit(judgmentContext, "测试用例[" + testCase.getId() + "]缺少期望输出",
                            testCase.getId());
                }

                // 收集期望输出
                String expectedOutput = testCaseOutput.getOutput();
                if (expectedOutput == null || expectedOutput.trim().isEmpty()) {
                    return createErrorAndExit(judgmentContext, "测试用例[" + testCase.getId() + "]期望输出为空",
                            testCase.getId());
                }
                allExpectedOutputs.add(expectedOutput);

                if (outputType == null) {
                    outputType = testCaseOutput.getOutputType();
                    if (outputType == null || outputType.isBlank()) {
                        return createErrorAndExit(judgmentContext, "缺少返回类型：请在测试用例中设置 outputType", testCase.getId());
                    }
                }

                List<TestCaseInput> inputs = testCase.getTestCaseInput();
                if (inputs == null)
                    inputs = new ArrayList<>();
                // 按顺序索引排序，确保输入顺序稳定
                inputs.sort(Comparator.comparing(i -> i.getOrderIndex() == null ? 0 : i.getOrderIndex()));

                List<String> inputTypes = new ArrayList<>();
                List<String> inputValues = new ArrayList<>();
                for (TestCaseInput in : inputs) {
                    inputTypes.add(Objects.toString(in.getInputType(), ""));
                    inputValues.add(Objects.toString(in.getInputContent(), ""));
                }

                allInputTypes.add(inputTypes);
                allInputValues.add(inputValues);
            }

            // 直接使用 JudgmentContext.solutionFunctionName
            String functionName = judgmentContext.getSolutionFunctionName();
            if (functionName == null || functionName.isBlank()) {
                return createErrorAndExit(judgmentContext, "缺少函数名：请在上下文中设置 solutionFunctionName", null);
            }

            // 生成支持多测试用例的 Main，并与 Solution 合并为完整可运行源码
            JavaCodeUtils utils = new JavaCodeUtils();
            String mainSrc = utils.generateMultiTestCaseMainClass(functionName, outputType, allInputTypes,
                    allInputValues, allExpectedOutputs);
            String runSrc = utils.generateMainFixSolutionClass(mainSrc, solutionCode);

            judgmentContext.setRunCode(runSrc);

            // 继续责任链
            if (this.nextHandler != null) {
                return this.nextHandler.handleRequest(judgmentContext);
            }
            return true;
        } catch (Exception e) {
            // 兜底错误信息
            String msg = e.getMessage() == null ? e.toString() : e.getMessage();
            return createErrorAndExit(judgmentContext, "代码格式化阶段出错: " + msg, null);
        }
    }

    /**
     * 创建错误状态并退出
     */
    private boolean createErrorAndExit(JudgmentContext context, String errorMsg, Long testCaseId) {
        context.setJudgeInfo(errorMsg);
        context.setJudgeStatus(JudgeStatus.SYSTEM_ERROR);

        // 统一的错误 JudgmentResult 创建
        JudgmentResult jr = JudgmentResult.builder()
                .testCaseId(testCaseId)
                .judgeStatus(context.getJudgeStatus())
                .memoryUsed(context.getMemoryUsed())
                .timeUsed(context.getTimeUsed())
                .compileInfo(context.getCompileInfo())
                .judgeInfo(context.getJudgeInfo())
                .build();

        List<JudgmentResult> results = context.getJudgmentResults();
        if (results == null)
            results = new ArrayList<>();
        results.add(jr);
        context.setJudgmentResults(results);

        return false;
    }
}
