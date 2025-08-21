package com.david.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.enums.CategoryType;
import com.david.problem.Problem;
import com.david.problem.enums.ProblemDifficulty;
import com.david.service.IProblemService;
import com.david.submission.dto.CompareDescription;
import com.david.utils.ResponseResult;
import com.david.exception.BizException;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/problems/api/management/problem")
public class ProblemManagementController {
    private final IProblemService problemService;

    /** 分页获取题目列表（管理端） */
    @GetMapping("/page")
    public ResponseResult<Page<Problem>> pageProblems(
            @RequestParam @Min(1) long page,
            @RequestParam @Min(1) long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ProblemDifficulty difficulty,
            @RequestParam(required = false) CategoryType category,
            @RequestParam(required = false) Boolean isVisible,
            @RequestParam(required = false) String sort) {
        Page<Problem> p = new Page<>(page, size);
        Page<Problem> result =
                problemService.pageProblems(p, keyword, difficulty, category, isVisible, sort);
        return ResponseResult.success("成功获取题目分页", result);
    }

    @PostMapping
    public ResponseResult<Void> createProblem(@RequestBody @Valid Problem problem) {
        if (problemService.save(problem)) {
            return ResponseResult.success("题目创建成功");
        }
        throw BizException.of(500, "题目创建失败");
    }

    @PutMapping
    public ResponseResult<Void> updateProblem(@RequestBody @Valid Problem problem) {
        if (problemService.updateById(problem)) {
            return ResponseResult.success("题目更新成功");
        }
        throw BizException.of(500, "题目更新失败");
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteProblem(@PathVariable @NotNull @Min(1) Long id) {
        if (problemService.removeById(id)) {
            return ResponseResult.success("题目删除成功");
        }
        throw BizException.of(500, "题目删除失败");
    }

    @GetMapping("/{id}")
    public ResponseResult<Problem> getProblemById(@PathVariable @NotNull @Min(1) Long id) {
        Problem problem = problemService.getById(id);
        if (problem == null) {
            throw BizException.of(404, "题目不存在");
        }
        return ResponseResult.success("成功获取题目", problem);
    }

    @GetMapping("/compareDescription")
    public ResponseResult<CompareDescription> getCompareDescription(@RequestParam @NotNull @Min(1) Long id) {
        return ResponseResult.success("成功获取题目函数名", problemService.getCompareDescription(id));
    }
}
