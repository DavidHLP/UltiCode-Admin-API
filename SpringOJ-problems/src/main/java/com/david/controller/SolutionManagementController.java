package com.david.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.service.ISolutionService;
import com.david.solution.Solution;
import com.david.solution.enums.SolutionStatus;
import com.david.solution.vo.SolutionManagementCardVo;
import com.david.utils.ResponseResult;
import com.david.utils.enums.ResponseCode;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/problems/api/management/solution")
public class SolutionManagementController {
    private final ISolutionService solutionService;

    @GetMapping("/pages")
    public ResponseResult<Page<SolutionManagementCardVo>> getSolutionCardVos(
            @RequestParam long page,
            @RequestParam long size,
            @RequestParam(required = false) Long problemId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) SolutionStatus status) {
        long p = Math.max(1, page);
        long s = size <= 0 ? 10 : Math.min(size, 100);
        return ResponseResult.success(
                "成功获取",
                solutionService.pageSolutionManagementCardVo(
                        new Page<>(p, s), problemId, keyword, userId, status));
    }

    @GetMapping("/detail")
    public ResponseResult<Solution> getSolutionDetail(@RequestParam Long solutionId) {
        return ResponseResult.success("成功获取", solutionService.getById(solutionId));
    }

    @PostMapping("/accept")
    public ResponseResult<Boolean> acceptSolution(
            @RequestBody SolutionManagementCardVo solutionManagementCardVo) {
        Long id = solutionManagementCardVo.getId();
        if (id == null) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR.getCode(), "参数无效：id 不能为空");
        }
        var solution = solutionService.getById(id);
        if (solution == null) {
            return ResponseResult.fail(ResponseCode.RC404.getCode(), "题解不存在");
        }
        // 允许 PENDING -> APPROVED, REJECTED -> APPROVED；若已是 APPROVED 直接返回成功
        SolutionStatus current = solution.getStatus();
        if (current == SolutionStatus.APPROVED) {
            return ResponseResult.success("已是通过状态", true);
        }
        if (current == null || current == SolutionStatus.PENDING || current == SolutionStatus.REJECTED) {
            solution.setStatus(SolutionStatus.APPROVED);
            boolean ok = solutionService.updateById(solution);
            return ok
                    ? ResponseResult.success("审核通过", true)
                    : ResponseResult.fail(ResponseCode.RC999.getCode(), "审核通过失败");
        }
        return ResponseResult.fail(ResponseCode.BUSINESS_ERROR.getCode(), "当前状态不允许通过操作");
    }

    @PostMapping("/reject")
    public ResponseResult<Boolean> rejectSolution(
            @RequestBody SolutionManagementCardVo solutionManagementCardVo) {
        Long id = solutionManagementCardVo.getId();
        if (id == null) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR.getCode(), "参数无效：id 不能为空");
        }
        var solution = solutionService.getById(id);
        if (solution == null) {
            return ResponseResult.fail(ResponseCode.RC404.getCode(), "题解不存在");
        }
        // 允许 PENDING -> REJECTED, APPROVED -> REJECTED；若已是 REJECTED 直接返回成功
        SolutionStatus current = solution.getStatus();
        if (current == SolutionStatus.REJECTED) {
            return ResponseResult.success("已是拒绝状态", true);
        }
        if (current == null || current == SolutionStatus.PENDING || current == SolutionStatus.APPROVED) {
            solution.setStatus(SolutionStatus.REJECTED);
            boolean ok = solutionService.updateById(solution);
            return ok
                    ? ResponseResult.success("已拒绝", true)
                    : ResponseResult.fail(ResponseCode.RC999.getCode(), "拒绝失败");
        }
        return ResponseResult.fail(ResponseCode.BUSINESS_ERROR.getCode(), "当前状态不允许拒绝操作");
    }
}
