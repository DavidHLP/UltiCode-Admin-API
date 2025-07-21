package com.david.service.impl;

import com.david.dto.JudgeResult;
import com.david.dto.SandboxExecuteRequest;
import com.david.dto.SubmitCodeRequest;
import com.david.judge.Problem;
import com.david.judge.Submission;
import com.david.judge.TestCase;
import com.david.judge.enums.JudgeStatus;
import com.david.interfaces.ProblemServiceFeignClient;
import com.david.interfaces.SubmissionServiceFeignClient;
import com.david.service.IJudgeService;
import com.david.service.ISandboxService;
import com.david.utils.AsyncContextUtil;
import com.david.utils.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 判题服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JudgeServiceImpl implements IJudgeService {

    private final SubmissionServiceFeignClient submissionService;
    private final ProblemServiceFeignClient problemServiceFeignClient;
    private final ISandboxService sandboxService;

    @Override
    public void judge(Long submissionId) {
        try {
            // 获取提交记录
            ResponseResult<Submission> submissionResponse = submissionService.getSubmissionById(submissionId);
            if (submissionResponse.getCode() != 200 || submissionResponse.getData() == null) {
                log.error("提交记录不存在: {}", submissionId);
                return;
            }
            Submission submission = submissionResponse.getData();

            // 更新状态为判题中
            submission.setStatus(JudgeStatus.JUDGING);
            submissionService.updateSubmission(submissionId, submission);

            // 获取题目信息
            ResponseResult<Problem> problemResponse = problemServiceFeignClient.getProblemById(submission.getProblemId());
            if (problemResponse.getCode() != 200 || problemResponse.getData() == null) {
                log.error("题目不存在或获取失败: {}", submission.getProblemId());
                updateSubmissionError(submissionId, "题目不存在或获取失败");
                return;
            }
            Problem problem = problemResponse.getData();

            // 获取测试用例
            ResponseResult<List<TestCase>> testCasesResponse = problemServiceFeignClient.getTestCasesByProblemId(problem.getId());
            if (testCasesResponse.getCode() != 200 || testCasesResponse.getData() == null || testCasesResponse.getData().isEmpty()) {
                log.error("题目没有测试用例或获取失败: {}", problem.getId());
                updateSubmissionError(submissionId, "题目没有测试用例或获取失败");
                return;
            }
            List<TestCase> testCases = testCasesResponse.getData();

            // 构建沙箱执行请求
            SandboxExecuteRequest request = buildSandboxRequest(submission, problem, testCases);

            // 发送到沙箱执行
            JudgeResult result = sandboxService.executeInSandbox(request);

            // 更新判题结果
            updateJudgeResult(submissionId, result);

        } catch (Exception e) {
            log.error("判题过程中发生异常: submissionId={}", submissionId, e);
            updateSubmissionError(submissionId, "系统错误: " + e.getMessage());
        }
    }

    @Override
    public void judgeAsync(SubmitCodeRequest request, Long submissionId) {
        judgeAsync(request, submissionId, null);
    }

    /**
     * 异步判题方法（带权限上下文）
     * @param request 提交代码请求
     * @param submissionId 提交ID
     * @param authContext 权限上下文
     */
    public void judgeAsync(SubmitCodeRequest request, Long submissionId, Map<String, String> authContext) {
        try {
            // 设置异步权限上下文
            if (authContext != null) {
                AsyncContextUtil.setAsyncAuthContext(authContext);
                log.debug("设置异步判题权限上下文: submissionId={}, authContext={}", submissionId, authContext);
            }
            
            // 执行判题逻辑
            judge(submissionId);
            
        } finally {
            // 清理异步权限上下文
            AsyncContextUtil.clearAsyncAuthContext();
            log.debug("清理异步判题权限上下文: submissionId={}", submissionId);
        }
    }

    private SandboxExecuteRequest buildSandboxRequest(Submission submission, Problem problem,
            List<TestCase> testCases) {
        SandboxExecuteRequest request = new SandboxExecuteRequest();
        request.setSourceCode(submission.getSourceCode());
        request.setLanguage(submission.getLanguage());
        request.setTimeLimit(problem.getTimeLimit());
        request.setMemoryLimit(problem.getMemoryLimit());
        request.setSubmissionId(submission.getId());

        // 提取输入和期望输出
        List<String> inputs = testCases.stream()
                .map(TestCase::getInputFile)
                .collect(Collectors.toList());
        List<String> expectedOutputs = testCases.stream()
                .map(TestCase::getOutputFile)
                .collect(Collectors.toList());

        request.setInputs(inputs);
        request.setExpectedOutputs(expectedOutputs);

        return request;
    }

    private void updateSubmissionError(Long submissionId, String errorMessage) {
        JudgeResult result = new JudgeResult();
        result.setStatus(JudgeStatus.SYSTEM_ERROR);
        result.setScore(0);
        result.setTimeUsed(0);
        result.setMemoryUsed(0);
        result.setErrorMessage(errorMessage);

        updateJudgeResult(submissionId, result);
    }

    private void updateJudgeResult(Long submissionId, JudgeResult result) {
        ResponseResult<Submission> submissionResponse = submissionService.getSubmissionById(submissionId);
        if (submissionResponse.getCode() == 200 && submissionResponse.getData() != null) {
            Submission submission = submissionResponse.getData();
            submission.setStatus(result.getStatus());
            submission.setScore(result.getScore());
            submission.setTimeUsed(result.getTimeUsed());
            submission.setMemoryUsed(result.getMemoryUsed());
            submission.setCompileInfo(result.getCompileInfo());
            submissionService.updateSubmission(submissionId, submission);
        }
    }
}
