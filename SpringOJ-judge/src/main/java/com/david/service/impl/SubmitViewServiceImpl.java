package com.david.service.impl;

import com.david.enums.JudgeStatus;
import com.david.interfaces.ProblemServiceReignClient;
import com.david.interfaces.SubmissionServiceFeignClient;
import com.david.interfaces.TestCaseServiceFeignClient;
import com.david.producer.SubmitProducer;
import com.david.service.ISubmitViewService;
import com.david.submission.Submission;
import com.david.submission.dto.CompareDescription;
import com.david.submission.dto.SubmitCodeRequest;
import com.david.submission.dto.SubmitToSandboxRequest;
import com.david.testcase.TestCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 判题服务实现类 - 优雅重构版本，使用责任链模式处理判题流程
 *
 * @author David
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubmitViewServiceImpl implements ISubmitViewService {

    private final SubmissionServiceFeignClient submissionServiceFeignClient;
    private final TestCaseServiceFeignClient testCaseServiceFeignClient;
    private final ProblemServiceReignClient problemServiceReignClient;
    private final SubmitProducer sandboxProducer;

    @Override
    public Long submitAndJudge(SubmitCodeRequest request, Long userId) {
        Long submissionId = submitAndJudgeToCreate(request, userId);
        if (submissionId == null || submissionId <= 0L) {
            throw new RuntimeException("创建提交记录失败");
        }
        CompareDescription compareDescription =
                problemServiceReignClient.getCompareDescription(request.getProblemId()).getData();
        if (compareDescription == null) {
            throw new RuntimeException("获取题目信息失败");
        }
        List<TestCase> testCases =
                testCaseServiceFeignClient
                        .getTestCasesByProblemId(request.getProblemId())
                        .getData();
        if (testCases == null || testCases.isEmpty()) {
            throw new RuntimeException("获取测试用例失败");
        }
        sandboxProducer.submitToSandbox(
                SubmitToSandboxRequest.builder()
                        .language(request.getLanguage())
                        .problemId(request.getProblemId())
                        .submissionId(submissionId)
                        .sourceCode(request.getSourceCode())
                        .testCases(testCases)
                        .difficulty(compareDescription.getDifficulty())
                        .solutionFunctionName(compareDescription.getSolutionFunctionName())
                        .userId(userId)
                        .build());
        return submitAndJudgeToUpdate(request, userId, submissionId);
    }

    @Override
    public Long submitAndJudgeToUpdate(SubmitCodeRequest request, Long userId, Long submissionId) {
        return submissionServiceFeignClient
                .updateSubmissionThenCallback(
                        Submission.builder()
                                .userId(userId)
                                .id(submissionId)
                                .problemId(request.getProblemId())
                                .language(request.getLanguage())
                                .status(JudgeStatus.JUDGING)
                                .sourceCode(request.getSourceCode())
                                .build())
                .getData();
    }

    @Override
    public Long submitAndJudgeToCreate(SubmitCodeRequest request, Long userId) {
        return submissionServiceFeignClient
                .createSubmissionThenCallback(
                        Submission.builder()
                                .userId(userId)
                                .problemId(request.getProblemId())
                                .language(request.getLanguage())
                                .status(JudgeStatus.PENDING)
                                .sourceCode(request.getSourceCode())
                                .build())
                .getData();
    }
}
