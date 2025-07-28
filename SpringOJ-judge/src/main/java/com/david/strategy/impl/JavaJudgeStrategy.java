package com.david.strategy.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.david.dto.JudgeContext;
import com.david.dto.JudgeResult;
import com.david.dto.SandboxExecuteRequest;
import com.david.judge.Problem;
import com.david.judge.Submission;
import com.david.judge.TestCase;
import com.david.service.ISandboxService;
import com.david.strategy.DefaultJudgeStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Java 语言判题策略
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JavaJudgeStrategy extends DefaultJudgeStrategy {
    public  final ISandboxService iSandboxService;

    @Override
    public JudgeResult execute(JudgeContext context) {
        SandboxExecuteRequest request = buildSandboxRequest(context.getSubmission(), context.getProblem(),
                context.getTestCases());
        return iSandboxService.executeInSandbox(request);
    }
    @Override
    public SandboxExecuteRequest buildSandboxRequest(Submission submission, Problem problem, List<TestCase> testCases) {
        SandboxExecuteRequest request = new SandboxExecuteRequest();
        request.setSourceCode(submission.getSourceCode());
        request.setLanguage(submission.getLanguage());
        request.setTimeLimit(problem.getTimeLimit());
        request.setMemoryLimit(problem.getMemoryLimit());
        request.setSubmissionId(submission.getId());

        // 直接从TestCase对象中获取输入输出内容
        List<String> inputs = new ArrayList<>();
        List<String> expectedOutputs = new ArrayList<>();

        for (TestCase testCase : testCases) {
            inputs.add(testCase.getInput());
            expectedOutputs.add(testCase.getOutput());
            log.debug("成功获取测试用例: problemId={}, input={}, output={}",
                    problem.getId(), testCase.getInput(), testCase.getOutput());
        }

        request.setInputs(inputs);
        request.setExpectedOutputs(expectedOutputs);

        log.info("构建沙箱请求成功: submissionId={}, problemId={}, testCaseCount={}",
                submission.getId(), problem.getId(), testCases.size());

        return request;
    }
}
