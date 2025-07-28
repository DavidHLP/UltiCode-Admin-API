package com.david.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.david.interfaces.ProblemServiceFeignClient;
import com.david.interfaces.SubmissionServiceFeignClient;
import com.david.judge.Problem;
import com.david.judge.Submission;
import com.david.judge.TestCase;
import com.david.judge.enums.JudgeStatus;
import com.david.producer.SandboxProducer;
import com.david.sandbox.dto.SandboxExecuteRequest;
import com.david.sandbox.dto.SubmitCodeRequest;
import com.david.service.IJudgeService;
import com.david.strategy.impl.JudgeStrategyFactory;
import com.david.utils.ResponseResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 判题服务实现类 - 最终优化版本，调用层级不超过2层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JudgeServiceImpl implements IJudgeService {

	private final SubmissionServiceFeignClient submissionServiceFeignClient;
	private final ProblemServiceFeignClient problemServiceFeignClient;
	private final SandboxProducer sandboxProducer;
	private final JudgeStrategyFactory strategyFactory;

	@Override
	public Long submitAndJudge(SubmitCodeRequest request, Long userId) {
		// 1. 创建提交记录
		Submission submission = createSubmission(request, userId);

		// 2. 直接执行判题
		executeJudge(submission);

		return submission.getId();
	}

	@Override
	public Submission getSubmission(Long submissionId) {
		return getSubmissionSafely(submissionId);
	}

	/**
	 * 创建提交记录
	 */
	private Submission createSubmission(SubmitCodeRequest request, Long userId) {
		Submission submission = Submission.builder()
			.language(request.getLanguage())
			.sourceCode(request.getSourceCode())
			.userId(userId)
			.problemId(request.getProblemId())
			.status(JudgeStatus.PENDING)
			.build();

		ResponseResult<Submission> response = submissionServiceFeignClient.createSubmission(submission);
		if (isResponseValid(response)) {
			throw new RuntimeException("创建提交记录失败");
		}
		return response.getData();
	}

	/**
	 * 执行判题 - 整合所有判题逻辑，减少调用层级
	 */
	private void executeJudge(Submission submission) {
		try {
			// 验证语言支持
			if (!strategyFactory.isLanguageSupported(submission.getLanguage())) {
				throw new RuntimeException("不支持的编程语言: " + submission.getLanguage());
			}

			// 更新状态为判题中
			updateSubmissionStatus(submission.getId());

			// 获取题目和测试用例
			Problem problem = getProblemSafely(submission.getProblemId());
			List<TestCase> testCases = getTestCasesSafely(problem.getId());

			// 构建并定制沙箱请求
			SandboxExecuteRequest sandboxRequest = buildSandboxRequest(submission, problem, testCases);
			sandboxRequest = strategyFactory.getStrategy(submission.getLanguage()).customizeRequest(sandboxRequest);

			// 发送到沙箱执行
			sandboxProducer.executeInSandbox(sandboxRequest);

			log.info("判题请求已发送到沙箱: submissionId={}, problemId={}, language={}",
				submission.getId(), problem.getId(), submission.getLanguage());

		} catch (Exception e) {
			log.error("判题失败: submissionId={}", submission.getId(), e);
			updateSubmissionError(submission.getId(), e.getMessage());
		}
	}

	/**
	 * 构建沙箱执行请求
	 */
	private SandboxExecuteRequest buildSandboxRequest(Submission submission, Problem problem, List<TestCase> testCases) {
		SandboxExecuteRequest request = new SandboxExecuteRequest();
		request.setSourceCode(submission.getSourceCode());
		request.setLanguage(submission.getLanguage());
		request.setTimeLimit(problem.getTimeLimit());
		request.setMemoryLimit(problem.getMemoryLimit());
		request.setSubmissionId(submission.getId());

		// 提取测试用例输入输出
		request.setInputs(testCases.stream().map(TestCase::getInput).toList());
		request.setExpectedOutputs(testCases.stream().map(TestCase::getOutput).toList());

		return request;
	}

	/**
	 * 安全获取提交记录
	 */
	private Submission getSubmissionSafely(Long submissionId) {
		ResponseResult<Submission> response = submissionServiceFeignClient.getSubmissionById(submissionId);
		if (isResponseValid(response)) {
			throw new RuntimeException("提交记录不存在: " + submissionId);
		}
		return response.getData();
	}

	/**
	 * 安全获取题目信息
	 */
	private Problem getProblemSafely(Long problemId) {
		ResponseResult<Problem> response = problemServiceFeignClient.getProblemById(problemId);
		if (isResponseValid(response)) {
			throw new RuntimeException("题目不存在: " + problemId);
		}
		return response.getData();
	}

	/**
	 * 安全获取测试用例
	 */
	private List<TestCase> getTestCasesSafely(Long problemId) {
		ResponseResult<List<TestCase>> response = problemServiceFeignClient.getTestCasesByProblemId(problemId);
		if (isResponseValid(response) || response.getData().isEmpty()) {
			throw new RuntimeException("题目缺少测试用例: " + problemId);
		}
		return response.getData();
	}

	/**
	 * 更新提交状态
	 */
	private void updateSubmissionStatus(Long submissionId) {
		Submission submission = getSubmissionSafely(submissionId);
		submission.setStatus(JudgeStatus.JUDGING);
		submissionServiceFeignClient.updateSubmission(submissionId, submission);
	}

	/**
	 * 更新提交错误信息
	 */
	private void updateSubmissionError(Long submissionId, String errorMessage) {
		try {
			Submission submission = getSubmissionSafely(submissionId);
			submission.setStatus(JudgeStatus.SYSTEM_ERROR);
			submission.setScore(0);
			submission.setTimeUsed(0);
			submission.setMemoryUsed(0);
			submission.setCompileInfo(errorMessage);
			submissionServiceFeignClient.updateSubmission(submissionId, submission);
		} catch (Exception e) {
			log.error("更新错误状态失败: submissionId={}", submissionId, e);
		}
	}

	/**
	 * 检查响应是否有效
	 */
	private boolean isResponseValid(ResponseResult<?> response) {
		return response == null || response.getCode() != 200 || response.getData() == null;
	}
}
