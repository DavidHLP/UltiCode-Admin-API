package com.david.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.judge.Problem;
import com.david.judge.enums.CategoryType;
import com.david.judge.enums.ProblemDifficulty;
import com.david.service.IProblemService;
import com.david.utils.ResponseResult;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/problems/api/management/problem")
public class ProblemManagementController {
	private final IProblemService problemService;

	/**
	 * 分页获取题目列表（管理端）
	 */
	@GetMapping("/page")
	public ResponseResult<Page<Problem>> pageProblems(@RequestParam long page, @RequestParam long size,
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) ProblemDifficulty difficulty,
			@RequestParam(required = false) CategoryType category, @RequestParam(required = false) Boolean isVisible) {
		Page<Problem> p = new Page<>(page, size);
		Page<Problem> result = problemService.pageProblems(p, keyword, difficulty, category, isVisible);
		return ResponseResult.success("成功获取题目分页", result);
	}

	@PostMapping
	public ResponseResult<Void> createProblem(@RequestBody Problem problem) {
		if (problemService.save(problem)) {
			return ResponseResult.success("题目创建成功");
		}
		return ResponseResult.fail(500, "题目创建失败");
	}

	@PutMapping
	public ResponseResult<Void> updateProblem(@RequestBody Problem problem) {
		if (problemService.updateById(problem)) {
			return ResponseResult.success("题目更新成功");
		}
		return ResponseResult.fail(500, "题目更新失败");
	}

	@DeleteMapping("/{id}")
	public ResponseResult<Void> deleteProblem(@PathVariable Long id) {
		if (problemService.removeById(id)) {
			return ResponseResult.success("题目删除成功");
		}
		return ResponseResult.fail(500, "题目删除失败");
	}

	@GetMapping("/{id}")
	public ResponseResult<Problem> getProblemById(@PathVariable Long id) {
		Problem problem = problemService.getById(id);
		if (problem == null) {
			return ResponseResult.fail(404, "题目不存在");
		}
		return ResponseResult.success("成功获取题目", problem);
	}
}
