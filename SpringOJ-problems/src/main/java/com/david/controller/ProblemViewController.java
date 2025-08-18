package com.david.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.enums.CategoryType;
import com.david.enums.LanguageType;
import com.david.problem.Problem;
import com.david.problem.enums.ProblemDifficulty;
import com.david.problem.vo.ProblemCardVo;
import com.david.problem.vo.ProblemDetailVo;
import com.david.service.IProblemService;
import com.david.utils.ResponseResult;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/problems/api/view/problem")
public class ProblemViewController {
    private final IProblemService problemService;

    @GetMapping("/page")
    public ResponseResult<Page<ProblemCardVo>> pageProblemVos(
            @RequestParam long page,
            @RequestParam long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ProblemDifficulty difficulty,
            @RequestParam(required = false) CategoryType category,
            @RequestParam(required = false) String sort) {
        Page<Problem> p = new Page<>(page, size);
        Page<ProblemCardVo> result = problemService.pageProblemVos(p, keyword, difficulty, category, sort);
        return ResponseResult.success("成功获取题目分页", result);
    }

    @GetMapping("/detail")
    public ResponseResult<ProblemDetailVo> getProblemDetailVoById(@RequestParam long id) {
        ProblemDetailVo problemDetailVo = problemService.getProblemDetailVoById(id);
        if (problemDetailVo == null) {
            return ResponseResult.fail(404, "题目不存在");
        }
        return ResponseResult.success("成功获取题目详情", problemDetailVo);
    }

	@GetMapping("/codetemplate")
	public ResponseResult<String> getCodeTemplate(@RequestParam Long problemId, @RequestParam LanguageType language) {
		String codeTemplate = problemService.getCodeTemplate(problemId, language);
		if (codeTemplate == null) {
			return ResponseResult.fail(404, "代码模板不存在");
		}
		return ResponseResult.success("成功获取代码模板", codeTemplate);
	}
}
