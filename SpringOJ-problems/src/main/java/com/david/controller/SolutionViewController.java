package com.david.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.exception.BizException;
import com.david.service.ISolutionService;
import com.david.service.IUserContentViewService;
import com.david.solution.Solution;
import com.david.solution.enums.ContentType;
import com.david.solution.vo.SolutionCardVo;
import com.david.solution.vo.SolutionDetailVo;
import com.david.utils.BaseController;
import com.david.utils.ResponseResult;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/problems/api/view/solution")
public class SolutionViewController extends BaseController {

    private final ISolutionService solutionService;
    private final IUserContentViewService userContentViewService;

    @PostMapping
    public ResponseResult<Void> createSolution(
            @RequestBody @Validated(Solution.Create.class) Solution solution) {
        solution.setUserId(getCurrentUserId());
        if (solutionService.save(solution)) {
            return ResponseResult.success("题解创建成功");
        }
        throw BizException.of(500, "题解创建失败");
    }

    @PutMapping
    public ResponseResult<Void> updateSolution(
            @RequestBody @Validated(Solution.Update.class) Solution solution) {
        if (solutionService.updateById(solution)) {
            return ResponseResult.success("题解更新成功");
        }
        throw BizException.of(500, "题解更新失败");
    }

    @DeleteMapping
    public ResponseResult<Void> deleteSolution(
            @RequestBody @Validated(Solution.Delete.class) Solution solution) {
        if (solutionService.removeById(solution)) {
            return ResponseResult.success("题解删除成功");
        }
        throw BizException.of(500, "题解删除失败");
    }

    @GetMapping
    public ResponseResult<SolutionDetailVo> getSolution(
            @RequestParam @NotNull @Min(1) Long solutionId) {
        SolutionDetailVo solutionDetailVo =
                solutionService.getSolutionDetailVoBy(solutionId, getCurrentUserId());
        if (solutionDetailVo == null) {
            throw BizException.of(404, "题解不存在");
        }
        Long views =
                userContentViewService.saveOrPassAndGetViewsNumber(
                        getCurrentUserId(), solutionId, ContentType.SOLUTION);
        solutionDetailVo.setViews(Math.toIntExact(views));
        return ResponseResult.success("获取题解成功", solutionDetailVo);
    }

    @GetMapping("/page")
    public ResponseResult<Page<SolutionCardVo>> pageSolutionCardVos(
            @RequestParam @Min(1) long page,
            @RequestParam @Min(1) long size,
            @RequestParam @NotNull @Min(1) Long problemId,
            @RequestParam(required = false) String keyword) {
        Page<SolutionCardVo> p = new Page<>(page, size);
        Page<SolutionCardVo> result = solutionService.pageSolutionCardVos(p, problemId, keyword);
        result.getRecords()
                .forEach(
                        solutionCardVo ->
                                solutionCardVo.setViews(
                                        Math.toIntExact(
                                                userContentViewService.getViewsNumber(
                                                        getCurrentUserId(),
                                                        solutionCardVo.getId(),
                                                        ContentType.SOLUTION))));
        return ResponseResult.success("获取题解成功", result);
    }
}
