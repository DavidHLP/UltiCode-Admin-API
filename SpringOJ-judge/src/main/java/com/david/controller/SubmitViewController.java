package com.david.controller;

import com.david.service.ISubmitViewService;
import com.david.submission.dto.SubmitCodeRequest;
import com.david.utils.BaseController;
import com.david.utils.ResponseResult;

import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 判题控制器
 */
@RestController
@RequestMapping("/judge/api/view/submit")
@RequiredArgsConstructor
public class SubmitViewController extends BaseController {

	private final ISubmitViewService submitViewService;

	/**
	 * 提交代码进行判题
	 */
	@PostMapping
	public ResponseResult<Long> submitCode(@RequestBody @Validated SubmitCodeRequest request) {
		Long submissionId = submitViewService.submitAndJudge(request, getCurrentUserId());
		return ResponseResult.success("代码提交成功", submissionId);
	}
}
