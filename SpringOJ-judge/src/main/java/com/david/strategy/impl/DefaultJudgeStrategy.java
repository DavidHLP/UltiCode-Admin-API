package com.david.strategy.impl;

import com.david.dto.JudgeResult;
import com.david.dto.SandboxExecuteRequest;
import com.david.judge.Problem;
import com.david.judge.Submission;
import com.david.judge.TestCase;
import com.david.service.ISandboxService;
import com.david.service.ITestCaseFileService;
import com.david.strategy.JudgeContext;
import com.david.strategy.JudgeStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 默认判题策略实现
 * 适用于大多数编程语言的通用判题逻辑
 * 支持基于文件的测试用例管理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultJudgeStrategy implements JudgeStrategy {
    private final ISandboxService sandboxService;
    private final ITestCaseFileService testCaseFileService;

    @Override
    public JudgeResult execute(JudgeContext context) {
        try {
            SandboxExecuteRequest request = buildSandboxRequest(context.getSubmission(), context.getProblem(),
                    context.getTestCases());
            return sandboxService.executeInSandbox(request);
        } catch (IOException e) {
            log.error("读取测试用例文件失败: submissionId={}, problemId={}",
                    context.getSubmission().getId(), context.getProblem().getId(), e);

            JudgeResult errorResult = new JudgeResult();
            errorResult.setStatus(com.david.judge.enums.JudgeStatus.SYSTEM_ERROR);
            errorResult.setScore(0);
            errorResult.setTimeUsed(0);
            errorResult.setMemoryUsed(0);
            errorResult.setErrorMessage("测试用例文件读取失败: " + e.getMessage());
            return errorResult;
        }
    }

    private SandboxExecuteRequest buildSandboxRequest(Submission submission, Problem problem, List<TestCase> testCases)
            throws IOException {
        SandboxExecuteRequest request = new SandboxExecuteRequest();
        request.setSourceCode(submission.getSourceCode());
        request.setLanguage(submission.getLanguage());
        request.setTimeLimit(problem.getTimeLimit());
        request.setMemoryLimit(problem.getMemoryLimit());
        request.setSubmissionId(submission.getId());

        // 读取测试用例文件内容
        List<String> inputs = new ArrayList<>();
        List<String> expectedOutputs = new ArrayList<>();

        for (TestCase testCase : testCases) {
            try {
                // 读取输入文件内容
                String inputContent = testCaseFileService.readInputFile(problem.getId(), testCase.getInputFile());
                inputs.add(inputContent);

                // 读取输出文件内容
                String outputContent = testCaseFileService.readOutputFile(problem.getId(), testCase.getOutputFile());
                expectedOutputs.add(outputContent);

                log.debug("成功读取测试用例: problemId={}, inputFile={}, outputFile={}",
                        problem.getId(), testCase.getInputFile(), testCase.getOutputFile());

            } catch (IOException e) {
                log.error("读取测试用例文件失败: problemId={}, inputFile={}, outputFile={}",
                        problem.getId(), testCase.getInputFile(), testCase.getOutputFile(), e);
                throw new IOException("测试用例文件读取失败: " + testCase.getInputFile() + " 或 " + testCase.getOutputFile(), e);
            }
        }

        request.setInputs(inputs);
        request.setExpectedOutputs(expectedOutputs);

        log.info("构建沙箱请求成功: submissionId={}, problemId={}, testCaseCount={}",
                submission.getId(), problem.getId(), testCases.size());

        return request;
    }
}
