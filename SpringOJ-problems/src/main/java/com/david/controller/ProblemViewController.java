package com.david.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.judge.Problem;
import com.david.judge.enums.CategoryType;
import com.david.judge.enums.LanguageType;
import com.david.judge.enums.ProblemDifficulty;
import com.david.service.IProblemService;
import com.david.utils.ResponseResult;
import com.david.vo.ProblemDetailVo;
import com.david.vo.ProblemVo;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/problems/api/view/problem")
public class ProblemViewController {
    private final IProblemService problemService;

    @GetMapping("/page")
    public ResponseResult<Page<ProblemVo>> pageProblemVos(
            @RequestParam long page,
            @RequestParam long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ProblemDifficulty difficulty,
            @RequestParam(required = false) CategoryType category) {
        Page<Problem> p = new Page<>(page, size);
        Page<ProblemVo> result = problemService.pageProblemVos(p, keyword, difficulty, category);
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
