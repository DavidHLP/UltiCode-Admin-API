package com.david.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.service.ISolutionService;
import com.david.solution.Solution;
import com.david.solution.enums.SolutionStatus;
import com.david.solution.vo.SolutionManagementCardVo;
import com.david.utils.ResponseResult;
import com.david.utils.enums.ResponseCode;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/problems/api/management/solution")
public class SolutionManagementController {
    private final ISolutionService solutionService;

    @GetMapping("/pages")
    public ResponseResult<Page<SolutionManagementCardVo>> getSolutionCardVos(
            @RequestParam @Min(value = 1, message = "页码必须>=1") long page,
            @RequestParam @Min(value = 1, message = "分页大小必须>=1") long size,
            @RequestParam(required = false) @Min(value = 1, message = "题目ID必须>=1") Long problemId,
            @RequestParam(required = false) @Size(max = 100, message = "关键词长度不能超过100字符")
                    String keyword,
            @RequestParam(required = false) @Min(value = 1, message = "用户ID必须>=1") Long userId,
            @RequestParam(required = false) SolutionStatus status) {
        long s = Math.min(size, 100);
        return ResponseResult.success(
                "成功获取",
                solutionService.pageSolutionManagementCardVo(
                        new Page<>(page, s), problemId, keyword, userId, status));
    }

    @GetMapping("/detail")
    public ResponseResult<Solution> getSolutionDetail(
            @RequestParam @NotNull(message = "题解ID不能为空") @Min(value = 1, message = "题解ID必须>=1")
                    Long solutionId) {
        return ResponseResult.success("成功获取", solutionService.getById(solutionId));
    }

    @PostMapping("/accept")
    public ResponseResult<Boolean> acceptSolution(
            @RequestBody SolutionManagementCardVo solutionManagementCardVo) {
        return updateSolutionStatus(
                solutionManagementCardVo,
                SolutionStatus.APPROVED,
                "已是通过状态",
                "审核通过",
                "审核通过失败",
                "当前状态不允许通过操作");
    }

    @PostMapping("/reject")
    public ResponseResult<Boolean> rejectSolution(
            @RequestBody SolutionManagementCardVo solutionManagementCardVo) {
        return updateSolutionStatus(
                solutionManagementCardVo,
                SolutionStatus.REJECTED,
                "已是拒绝状态",
                "已拒绝",
                "拒绝失败",
                "当前状态不允许拒绝操作");
    }

    /**
     * 通用的题解状态更新方法
     *
     * @param solutionManagementCardVo 题解管理卡片VO
     * @param targetStatus 目标状态
     * @param alreadyInStatusMessage 已经是目标状态时的消息
     * @param successMessage 更新成功时的消息
     * @param failureMessage 更新失败时的消息
     * @param invalidStatusMessage 状态不允许更新时的消息
     * @return 响应结果
     */
    private ResponseResult<Boolean> updateSolutionStatus(
            SolutionManagementCardVo solutionManagementCardVo,
            SolutionStatus targetStatus,
            String alreadyInStatusMessage,
            String successMessage,
            String failureMessage,
            String invalidStatusMessage) {

        Long id = solutionManagementCardVo.getId();
        if (id == null) {
            return ResponseResult.fail(ResponseCode.BUSINESS_ERROR.getCode(), "参数无效：id 不能为空");
        }

        var solution = solutionService.getById(id);
        if (solution == null) {
            return ResponseResult.fail(ResponseCode.RC404.getCode(), "题解不存在");
        }

        SolutionStatus current = solution.getStatus();

        // 如果已经是目标状态，直接返回成功
        if (current == targetStatus) {
            return ResponseResult.success(alreadyInStatusMessage, true);
        }

        // 检查是否允许状态转换
        if (isStatusTransitionAllowed(current, targetStatus)) {
            solution.setStatus(targetStatus);
            boolean ok = solutionService.updateById(solution);
            return ok
                    ? ResponseResult.success(successMessage, true)
                    : ResponseResult.fail(ResponseCode.RC999.getCode(), failureMessage);
        }

        return ResponseResult.fail(ResponseCode.BUSINESS_ERROR.getCode(), invalidStatusMessage);
    }

    /**
     * 检查状态转换是否被允许
     *
     * @param currentStatus 当前状态
     * @param targetStatus 目标状态
     * @return 是否允许转换
     */
    private boolean isStatusTransitionAllowed(
            SolutionStatus currentStatus, SolutionStatus targetStatus) {
        // null状态、PENDING状态可以转换为任何状态
        // APPROVED和REJECTED状态之间可以相互转换
        return currentStatus == null
                || currentStatus == SolutionStatus.PENDING
                || (targetStatus == SolutionStatus.APPROVED
                        && currentStatus == SolutionStatus.REJECTED)
                || (targetStatus == SolutionStatus.REJECTED
                        && currentStatus == SolutionStatus.APPROVED);
    }
}
