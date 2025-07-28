package com.david.strategy;

import java.util.List;

import com.david.dto.JudgeContext;
import com.david.dto.JudgeResult;
import com.david.dto.SandboxExecuteRequest;
import com.david.judge.Problem;
import com.david.judge.Submission;
import com.david.judge.TestCase;

/**
 * @author david
 * @since 2023/12/5
 */
public interface JudgeStrategy {
	JudgeResult execute(JudgeContext context);
	SandboxExecuteRequest buildSandboxRequest(Submission submission, Problem problem, List<TestCase> testCases);
}
