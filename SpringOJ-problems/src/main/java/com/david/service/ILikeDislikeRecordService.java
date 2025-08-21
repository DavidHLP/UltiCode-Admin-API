package com.david.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.david.calendar.LikeDislikeRecord;
import com.david.calendar.enums.ActionType;
import com.david.calendar.enums.TargetType;
import com.david.calendar.vo.LikeDislikeRecordVo;
import com.david.solution.UpDownCounts;

import java.util.List;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

/**
 * 点赞点踩记录服务接口
 */
@Validated
public interface ILikeDislikeRecordService extends IService<LikeDislikeRecord> {

    /**
     * 获取用户对指定内容的操作状态和统计数据
     * @param userId 用户ID
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 包含用户操作状态和统计数据的VO
     */
    LikeDislikeRecordVo getUserActionStatus(@NotNull @Min(1) Long userId,
                                            @NotNull TargetType targetType,
                                            @NotNull @Min(1) Long targetId);

    /**
     * 执行点赞/点踩操作
     * 如果用户已有反向操作（如已点赞却点踩），则先取消原操作
     * @param userId 用户ID
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @param actionType 操作类型
     * @return 操作结果和更新后的统计数据
     */
    LikeDislikeRecordVo performAction(@NotNull @Min(1) Long userId,
                                      @NotNull TargetType targetType,
                                      @NotNull @Min(1) Long targetId,
                                      @NotNull ActionType actionType);

    /**
     * 取消操作（取消点赞或点踩）
     * @param userId 用户ID
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 操作结果和更新后的统计数据
     */
    LikeDislikeRecordVo cancelAction(@NotNull @Min(1) Long userId,
                                     @NotNull TargetType targetType,
                                     @NotNull @Min(1) Long targetId);

    /**
     * 获取内容的点赞点踩统计
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 统计数据
     */
    LikeDislikeRecordVo getStats(@NotNull TargetType targetType,
                                 @NotNull @Min(1) Long targetId);

	List<UpDownCounts> getUpDownCountsByTargetTypeAndTargetIds(@NotNull TargetType targetType,
                                                             @NotNull @NotEmpty List<@NotNull @Min(1) Long> targetIds);
}
