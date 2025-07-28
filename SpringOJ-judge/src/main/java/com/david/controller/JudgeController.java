package com.david.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.david.judge.Submission;
import com.david.sandbox.dto.SubmitCodeRequest;
import com.david.service.IJudgeService;
import com.david.utils.BaseController;
import com.david.utils.ResponseResult;

import lombok.RequiredArgsConstructor;

/**
 * 判题控制器
 */
@RestController
@RequestMapping("/judge/api")
@RequiredArgsConstructor
public class JudgeController extends BaseController {

	private final IJudgeService judgeService;

	/**
	 * 提交代码进行判题
	 */
	@PostMapping("/submit")
	public ResponseResult<Long> submitCode(@RequestBody @Validated SubmitCodeRequest request) {
		Long submissionId = judgeService.submitAndJudge(request, getCurrentUserId());
		return ResponseResult.success("代码提交成功", submissionId);
	}

	/**
	 * 查询提交记录
	 */
	@GetMapping("/submission/{submissionId}")
	public ResponseResult<Submission> getSubmission(@PathVariable Long submissionId) {
		Submission submission = judgeService.getSubmission(submissionId);
		return ResponseResult.success("提交记录获取成功", submission);
	}
}
