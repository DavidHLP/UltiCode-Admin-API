package com.david.interaction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("moderation_actions")
public class ModerationAction {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("task_id")
    private Long taskId;

    private String action;

    @TableField("operator_id")
    private Long operatorId;

    private String remarks;

    private String context;

    @TableField("created_at")
    private LocalDateTime createdAt;
}

