package com.david.service.impl;

import org.springframework.stereotype.Service;

import com.david.chain.JudgeChainManager;
import com.david.constants.JudgeConstants;
import com.david.dto.JudgeContext;
import com.david.dto.SubmitCodeRequest;
import com.david.exception.JudgeException;
import com.david.interfaces.SubmissionServiceFeignClient;
import com.david.judge.Submission;
import com.david.judge.enums.JudgeStatus;
import com.david.service.IJudgeService;
import com.david.utils.ResponseValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 判题服务实现类 - 优雅重构版本，使用责任链模式处理判题流程
 * 
 * @author David
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JudgeServiceImpl implements IJudgeService {

	private final SubmissionServiceFeignClient submissionServiceFeignClient;
	private final JudgeChainManager judgeChainManager;

	@Override
	public Long submitAndJudge(SubmitCodeRequest request, Long userId) {
		log.info("开始处理判题请求: problemId={}, language={}, userId={}", 
				request.getProblemId(), request.getLanguage(), userId);
		
		try {
			// 1. 创建提交记录
			Submission submission = createSubmission(request, userId);
			
			// 2. 使用责任链执行判题流程
			executeJudgeWithChain(submission, userId);
			
			return submission.getId();
			
		} catch (JudgeException e) {
			log.error("判题请求处理失败: {}", e.getMessage(), e);
			// 如果有提交ID，更新错误状态
			if (e.getSubmissionId() != null) {
				updateSubmissionError(e.getSubmissionId(), e.getMessage(), e.getStatus());
			}
			throw e;
		} catch (Exception e) {
			log.error("判题请求处理失败: 系统错误", e);
			throw new JudgeException("系统错误: " + e.getMessage(), e);
		}
	}

	@Override
	public Submission getSubmission(Long submissionId) {
		log.debug("获取提交记录: submissionId={}", submissionId);
		return getSubmissionSafely(submissionId);
	}

	/**
	 * 创建提交记录
	 */
	private Submission createSubmission(SubmitCodeRequest request, Long userId) {
		log.debug("创建提交记录: problemId={}, language={}, userId={}", 
				request.getProblemId(), request.getLanguage(), userId);
		
		Submission submission = Submission.builder()
				.language(request.getLanguage())
				.sourceCode(request.getSourceCode())
				.userId(userId)
				.problemId(request.getProblemId())
				.status(JudgeStatus.PENDING)
				.build();

		var response = submissionServiceFeignClient.createSubmission(submission);
		var createdSubmission = ResponseValidator.getValidatedData(
				response, 
				JudgeConstants.ErrorMessages.CREATE_SUBMISSION_FAILED
		);
		
		log.info("提交记录创建成功: submissionId={}", createdSubmission.getId());
		return createdSubmission;
	}

	/**
	 * 使用责任链执行判题流程 - 优雅且可读的判题处理
	 */
	private void executeJudgeWithChain(Submission submission, Long userId) {
		log.info("开始执行判题责任链: submissionId={}", submission.getId());
		
		// 构建判题上下文
		JudgeContext context = JudgeContext.builder()
				.submission(submission)
				.userId(userId)
				.build();
		
		try {
			// 执行责任链
			judgeChainManager.executeJudgeChain(context);
			
			log.info("判题责任链执行完成: submissionId={}", submission.getId());
			
		} catch (JudgeException e) {
			// 重新抛出判题异常，保持异常信息
			throw e;
		} catch (Exception e) {
			// 包装其他异常为判题异常
			throw new JudgeException(
					"判题执行失败: " + e.getMessage(), 
					e, 
					JudgeStatus.SYSTEM_ERROR, 
					submission.getId(), 
					"EXECUTION_FAILED"
			);
		}
	}



	/**
	 * 安全获取提交记录
	 */
	private Submission getSubmissionSafely(Long submissionId) {
		var response = submissionServiceFeignClient.getSubmissionById(submissionId);
		return ResponseValidator.getValidatedData(
				response, 
				String.format(JudgeConstants.ErrorMessages.SUBMISSION_NOT_FOUND, submissionId),
				submissionId
		);
	}







	/**
	 * 更新提交错误信息
	 */
	private void updateSubmissionError(Long submissionId, String errorMessage, JudgeStatus status) {
		try {
			Submission submission = getSubmissionSafely(submissionId);
			submission.setStatus(status);
			submission.setScore(0);
			submission.setTimeUsed(0);
			submission.setMemoryUsed(0);
			submission.setCompileInfo(errorMessage);
			submissionServiceFeignClient.updateSubmission(submissionId, submission);
			
			log.info("提交错误状态更新成功: submissionId={}, status={}, error={}", 
					submissionId, status, errorMessage);
					
		} catch (Exception e) {
			log.error("更新错误状态失败: submissionId={}", submissionId, e);
		}
	}


}
