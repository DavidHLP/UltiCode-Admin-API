package com.david.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.david.service.IPCodeTemplateService;
import com.david.service.IProblemService;
import com.david.service.ITestCaseService;
import com.david.utils.ResponseResult;
import com.david.vo.CodeTemplateVo;
import com.david.vo.ProblemVo;
import com.david.vo.TestCaseVo;

import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 题目前端控制器
 * </p>
 *
 * @author david
 * @since 2025-07-21
 */
@RestController
@RequestMapping("/problems/api/view")
@RequiredArgsConstructor
public class ProblemViewController {

	private final IProblemService problemService;
	private final ITestCaseService testCaseService;
	private final IPCodeTemplateService codeTemplateService;

	@GetMapping("/{id}")
	public ResponseResult<ProblemVo> getProblemById(@PathVariable Long id) {
		ProblemVo problemVo = problemService.getProblemDtoById(id);
		if (problemVo == null) {
			return ResponseResult.fail(404, "题目不存在或已被删除");
		}
		List<CodeTemplateVo> codeTemplateVos = codeTemplateService.getCodeTemplateVosByProblemId(id);
		if (codeTemplateVos.isEmpty()){
			return ResponseResult.fail(404, "代码模板不存在");
		}
		List<TestCaseVo> testCaseVos = testCaseService.getTestCaseVoByProblemId(id);
		if (testCaseVos == null) {
			return ResponseResult.fail(404, "测试用例样例不存在");
		}
		problemVo.setTestCases(testCaseVos);
		problemVo.setInitialCode(codeTemplateVos);
		return ResponseResult.success("成功获取题目", problemVo);
	}
}
