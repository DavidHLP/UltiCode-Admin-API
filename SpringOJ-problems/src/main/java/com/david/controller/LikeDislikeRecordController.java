package com.david.controller;

import com.david.calendar.enums.ActionType;
import com.david.calendar.enums.TargetType;
import com.david.calendar.vo.LikeDislikeRecordVo;
import com.david.service.ILikeDislikeRecordService;
import com.david.utils.BaseController;
import com.david.utils.ResponseResult;

import lombok.RequiredArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

/** 点赞点踩记录控制器 处理用户对内容的点赞/点踩操作 */
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/problems/api/likeDislike")
public class LikeDislikeRecordController extends BaseController {
    private final ILikeDislikeRecordService likeDislikeRecordService;

    /**
     * 获取用户对指定内容的操作状态
     *
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 用户操作状态和统计数据
     */
    @GetMapping("/status")
    public ResponseResult<LikeDislikeRecordVo> getUserActionStatus(
            @RequestParam @NotNull TargetType targetType, @RequestParam @NotNull @Min(1) Long targetId) {
        return ResponseResult.success(
                "获取用户操作状态成功",
                likeDislikeRecordService.getUserActionStatus(
                        getCurrentUserId(), targetType, targetId));
    }

    /**
     * 执行点赞/点踩操作
     *
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @param actionType 操作类型
     * @return 操作结果和更新后的统计数据
     */
    @PostMapping("/action")
    public ResponseResult<LikeDislikeRecordVo> performAction(
            @RequestParam @NotNull TargetType targetType,
            @RequestParam @NotNull @Min(1) Long targetId,
            @RequestParam @NotNull ActionType actionType) {
        return ResponseResult.success(
                "操作执行成功",
                likeDislikeRecordService.performAction(
                        getCurrentUserId(), targetType, targetId, actionType));
    }

    /**
     * 取消操作（取消点赞或点踩）
     *
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 操作结果和更新后的统计数据
     */
    @DeleteMapping("/cancel")
    public ResponseResult<LikeDislikeRecordVo> cancelAction(
            @RequestParam @NotNull TargetType targetType, @RequestParam @NotNull @Min(1) Long targetId) {
        return ResponseResult.success(
                "取消操作成功",
                likeDislikeRecordService.cancelAction(getCurrentUserId(), targetType, targetId));
    }

    /**
     * 获取内容的点赞点踩统计
     *
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 统计数据
     */
    @GetMapping("/stats")
    public ResponseResult<LikeDislikeRecordVo> getStats(
            @RequestParam @NotNull TargetType targetType, @RequestParam @NotNull @Min(1) Long targetId) {
        return ResponseResult.success(
                "获取统计数据成功", likeDislikeRecordService.getStats(targetType, targetId));
    }
}
