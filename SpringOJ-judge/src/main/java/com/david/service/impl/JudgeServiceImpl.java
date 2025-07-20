package com.david.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.david.domain.dto.JudgeContext;
import com.david.domain.dto.JudgeResult;
import com.david.domain.entity.Problem;
import com.david.domain.entity.Submission;
import com.david.domain.entity.TestCase;
import com.david.domain.enums.SubmissionStatus;
import com.david.mapper.ProblemMapper;
import com.david.mapper.SubmissionMapper;
import com.david.mapper.TestCaseMapper;
import com.david.service.JudgeService;
import com.david.strategy.JudgeStrategy;
import com.david.strategy.JudgeStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class JudgeServiceImpl implements JudgeService {

    private final SubmissionMapper submissionMapper;

    private final ProblemMapper problemMapper;

    private final TestCaseMapper testCaseMapper;

    private final JudgeStrategyFactory judgeStrategyFactory;

    @Override
    @Transactional
    public void doJudge(long submissionId) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (Objects.isNull(submission)) {
            throw new IllegalArgumentException("提交记录不存在: " + submissionId);
        }

        Problem problem = problemMapper.selectById(submission.getProblemId());
        if (Objects.isNull(problem)) {
            throw new IllegalArgumentException("题目不存在: " + submission.getProblemId());
        }

        List<TestCase> testCases = testCaseMapper.selectList(new QueryWrapper<TestCase>().eq("problem_id", submission.getProblemId()));

        submission.setStatus(SubmissionStatus.JUDGING);
        submissionMapper.updateById(submission);

        JudgeContext context = new JudgeContext();
        context.setSubmission(submission);
        context.setProblem(problem);
        context.setTestCases(testCases);

        JudgeStrategy judgeStrategy = judgeStrategyFactory.getStrategy(submission.getLanguage());
        JudgeResult result = judgeStrategy.execute(context);

        submission.setStatus(result.getStatus());
        submission.setTimeUsed((int) result.getTime());
        submission.setMemoryUsed((int) result.getMemory());
        if (result.getStatus() == SubmissionStatus.COMPILE_ERROR) {
            submission.setCompileInfo(result.getErrorOutput());
        } else {
            submission.setJudgeInfo(result.getStatus().getStatus());
        }

        submissionMapper.updateById(submission);
    }
}
