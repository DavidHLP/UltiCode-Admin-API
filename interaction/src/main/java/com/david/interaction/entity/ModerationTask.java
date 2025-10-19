package com.david.interaction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("moderation_tasks")
public class ModerationTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("entity_type")
    private String entityType;

    @TableField("entity_id")
    private Long entityId;

    private String status;

    private Integer priority;

    private String source;

    @TableField("risk_level")
    private String riskLevel;

    @TableField("reviewer_id")
    private Long reviewerId;

    private String metadata;

    private String notes;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("reviewed_at")
    private LocalDateTime reviewedAt;
}

