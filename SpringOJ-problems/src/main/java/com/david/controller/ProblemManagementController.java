package com.david.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.david.dto.CategoryDto;
import com.david.judge.CodeTemplate;
import com.david.judge.Problem;
import com.david.judge.TestCase;
import com.david.judge.enums.CategoryType;
import com.david.service.IPCodeTemplateService;
import com.david.service.IProblemService;
import com.david.service.ITestCaseService;
import com.david.utils.ResponseResult;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/problems/api/management")
public class ProblemManagementController {
	private final IProblemService problemService;
	private final ITestCaseService testCaseService;
	private final IPCodeTemplateService codeTemplateService;

	@GetMapping
	public ResponseResult<List<Problem>> getAllProblems() {
		return ResponseResult.success("成功获取所有题目", problemService.list());
	}

	@GetMapping("/{id}")
	public ResponseResult<Problem> getProblemById(@PathVariable Long id) {
		Problem problem = problemService.getById(id);
		if (problem == null) {
			return ResponseResult.fail(404, "题目不存在");
		}
		return ResponseResult.success("成功获取题目", problem);
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

	/**
	 * 根据题目ID获取所有测试用例
	 */
	@GetMapping("/testcases/problem/{problemId}")
	public ResponseResult<List<TestCase>> getTestCasesByProblemId(@PathVariable Long problemId) {
		List<TestCase> testCases = testCaseService.getTestCasesByProblemId(problemId);
		return ResponseResult.success("成功获取测试用例", testCases);
	}

	/**
	 * 创建测试用例
	 */
	@PostMapping("/testcases")
	public ResponseResult<TestCase> createTestCase(@RequestBody TestCase testCase) {
		if (testCaseService.saveTestCase(testCase)) {
			return ResponseResult.success("测试用例创建成功", testCase);
		}
		return ResponseResult.fail(500, "测试用例创建失败");
	}

	/**
	 * 更新测试用例
	 */
	@PutMapping("/testcases")
	public ResponseResult<TestCase> updateTestCase(@RequestBody TestCase testCase) {
		if (testCaseService.updateTestCase(testCase)) {
			return ResponseResult.success("测试用例更新成功", testCase);
		}
		return ResponseResult.fail(500, "测试用例更新失败");
	}

	/**
	 * 删除测试用例
	 */
	@DeleteMapping("/testcases/{id}")
	public ResponseResult<Void> deleteTestCase(@PathVariable Long id) {
		if (testCaseService.removeById(id)) {
			return ResponseResult.success("测试用例删除成功");
		}
		return ResponseResult.fail(500, "测试用例删除失败");
	}

	/**
	 * 获取所有题目类别
	 */
	@GetMapping("/categories")
	public ResponseResult<List<CategoryDto>> getAllCategories() {
		List<CategoryDto> categories = new ArrayList<>();
		for (CategoryType type : CategoryType.values()) {
			categories
					.add(CategoryDto.builder().category(type.getCategory()).description(type.getDescription()).build());
		}
		return ResponseResult.success("成功获取所有题目类别", categories);
	}

	@GetMapping("/codetemplates/{problemId}/{language}")
	public ResponseResult<CodeTemplate> getCodeTemplateByProblemIdAndLanguage(@PathVariable("problemId") Long problemId,
			@PathVariable("language") String language) {
		CodeTemplate codeTemplates = codeTemplateService.getCodeTemplateByProblemIdAndLanguage(problemId, language);
		if (codeTemplates == null) {
			return ResponseResult.fail(404, "代码模板不存在");
		}
		return ResponseResult.success("成功获取代码模板", codeTemplates);
	}

	/**
	 * 根据题目ID获取所有代码模板
	 */
	@GetMapping("/codetemplates/problem/{problemId}")
	public ResponseResult<List<CodeTemplate>> getCodeTemplatesByProblemId(@PathVariable Long problemId) {
		List<CodeTemplate> codeTemplates = codeTemplateService.lambdaQuery().eq(CodeTemplate::getProblemId, problemId)
				.list();
		return ResponseResult.success("成功获取代码模板", codeTemplates);
	}

	/**
	 * 创建代码模板
	 */
	@PostMapping("/codetemplates")
	public ResponseResult<CodeTemplate> createCodeTemplate(@RequestBody CodeTemplate codeTemplate) {
		if (codeTemplateService.save(codeTemplate)) {
			return ResponseResult.success("代码模板创建成功", codeTemplate);
		}
		return ResponseResult.fail(500, "代码模板创建失败");
	}

	/**
	 * 更新代码模板
	 */
	@PutMapping("/codetemplates")
	public ResponseResult<CodeTemplate> updateCodeTemplate(@RequestBody CodeTemplate codeTemplate) {
		if (codeTemplateService.updateById(codeTemplate)) {
			return ResponseResult.success("代码模板更新成功", codeTemplate);
		}
		return ResponseResult.fail(500, "代码模板更新失败");
	}

	/**
	 * 删除代码模板
	 */
	@DeleteMapping("/codetemplates/{id}")
	public ResponseResult<Void> deleteCodeTemplate(@PathVariable Long id) {
		if (codeTemplateService.removeById(id)) {
			return ResponseResult.success("代码模板删除成功");
		}
		return ResponseResult.fail(500, "代码模板删除失败");
	}
}
