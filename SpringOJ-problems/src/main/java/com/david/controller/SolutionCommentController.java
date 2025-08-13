package com.david.controller;

import com.david.service.ISolutionCommentService;
import com.david.solution.SolutionComments;
import com.david.utils.BaseController;
import com.david.utils.ResponseResult;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/problems/api/view/solution/comment")
public class SolutionCommentController extends BaseController {
	private final ISolutionCommentService solutionCommentService;
	@PostMapping
	public ResponseResult<Void> createComment(@RequestBody  SolutionComments solutionComments) {
		solutionComments.setUserId(getCurrentUserId());
		if (!solutionCommentService.save(solutionComments)){
			throw new RuntimeException("创建评论失败");
		}
		return ResponseResult.success("成功创建评论");
	}

	@PutMapping
	public ResponseResult<Void> updateComment(@RequestBody SolutionComments solutionComments) {
		if (!solutionCommentService.updateById(solutionComments)){
			throw new RuntimeException("更新评论失败");
		}
		return ResponseResult.success("成功更新评论");
	}

	@DeleteMapping
	public ResponseResult<Void> deleteComment(@RequestBody SolutionComments solutionComments) {
		if (!solutionCommentService.removeById(solutionComments)){
			throw new RuntimeException("删除评论失败");
		}
		return ResponseResult.success("成功删除评论");
	}
}
