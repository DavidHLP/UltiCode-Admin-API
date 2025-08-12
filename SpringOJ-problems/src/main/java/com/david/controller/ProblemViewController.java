package com.david.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.judge.enums.CategoryType;
import com.david.judge.enums.ProblemDifficulty;
import com.david.service.IProblemService;
import com.david.utils.ResponseResult;
import com.david.vo.ProblemVo;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/problems/api/view/problem")
public class ProblemViewController {
	private final IProblemService problemService;
    public ResponseResult<Page<ProblemVo>> pageProblemVos(
            @RequestParam long page,
            @RequestParam long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ProblemDifficulty difficulty,
            @RequestParam(required = false) CategoryType category,
            @RequestParam(required = false) Boolean isVisible) {
        return null;
    }
}
