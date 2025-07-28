package com.david.strategy;

import java.util.List;

import org.springframework.stereotype.Component;

import com.david.dto.JudgeContext;
import com.david.dto.JudgeResult;
import com.david.dto.SandboxExecuteRequest;
import com.david.judge.Problem;
import com.david.judge.Submission;
import com.david.judge.TestCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认判题策略实现
 * 适用于大多数编程语言的通用判题逻辑
 */
@Slf4j
@Component
@RequiredArgsConstructor
public abstract class DefaultJudgeStrategy implements JudgeStrategy {
    @Override
    public abstract JudgeResult execute(JudgeContext context);
    @Override
    public abstract SandboxExecuteRequest buildSandboxRequest(Submission submission, Problem problem, List<TestCase> testCases);
}
