package com.david.calendar;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.david.calendar.enums.ActionType;
import com.david.calendar.enums.TargetType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 点赞点踩记录表实体类 对应数据库表 like_dislike_record */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("like_dislike_record")
public class LikeDislikeRecord {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID（谁点的） */
    @NotNull(message = "用户ID不能为空")
    @Min(value = 1, message = "用户ID必须为正数")
    private Long userId;

    /** 目标类型（1:文章 2:评论 3:回复等） */
    @NotNull(message = "目标类型不能为空")
    private TargetType targetType;

    /** 目标ID（被点赞/点踩的内容ID） */
    @NotNull(message = "目标ID不能为空")
    @Min(value = 1, message = "目标ID必须为正数")
    private Long targetId;

    /** 操作类型（1:点赞 2:点踩） */
    @NotNull(message = "操作类型不能为空")
    private ActionType actionType;

    /** 操作时间 */
    @NotNull(message = "创建时间不能为空")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @NotNull(message = "更新时间不能为空")
    private LocalDateTime updatedAt;
}
