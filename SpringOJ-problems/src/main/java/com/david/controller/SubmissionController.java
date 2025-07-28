package com.david.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.david.judge.Submission;
import com.david.service.ISubmissionService;
import com.david.utils.ResponseResult;

import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 提交记录前端控制器
 * </p>
 *
 * @author david
 * @since 2025-07-22
 */
@RestController
@RequestMapping("/submissions/api")
@RequiredArgsConstructor
public class SubmissionController {

	private final ISubmissionService submissionService;

	@GetMapping
	public ResponseResult<List<Submission>> getAllSubmissions() {
		return ResponseResult.success("成功获取所有提交记录", submissionService.list());
	}

	@GetMapping("/{id}")
	public ResponseResult<Submission> getSubmissionById(@PathVariable Long id) {
		Submission submission = submissionService.getById(id);
		return ResponseResult.success("成功获取提交记录", submission);
	}

	@PostMapping
	public ResponseResult<Submission> createSubmission(@RequestBody Submission submission) {
		if (submissionService.save(submission)) {
			return ResponseResult.success("提交记录创建成功", submission);
		}
		return ResponseResult.fail(500, "提交记录创建失败");
	}

	@PutMapping("/{id}")
	public ResponseResult<Void> updateSubmission(@PathVariable Long id, @RequestBody Submission submission) {
		submission.setId(id);
		if (submissionService.updateById(submission)) {
			return ResponseResult.success("提交记录更新成功");
		}
		return ResponseResult.fail(500, "提交记录更新失败");
	}

	@DeleteMapping("/{id}")
	public ResponseResult<Void> deleteSubmission(@PathVariable Long id) {
		if (submissionService.removeById(id)) {
			return ResponseResult.success("提交记录删除成功");
		}
		return ResponseResult.fail(500, "提交记录删除失败");
	}

	@GetMapping("/problem/{problemId}")
	public ResponseResult<List<Submission>> getSubmissionsByProblemId(@PathVariable Long problemId) {
		List<Submission> submissions = submissionService.lambdaQuery().eq(Submission::getProblemId, problemId).list();
		return ResponseResult.success("成功获取题目的提交记录", submissions);
	}

	@GetMapping("/user/{userId}")
	public ResponseResult<List<Submission>> getSubmissionsByUserId(@PathVariable Long userId) {
		List<Submission> submissions = submissionService.lambdaQuery().eq(Submission::getUserId, userId).list();
		return ResponseResult.success("成功获取用户的提交记录", submissions);
	}
}
