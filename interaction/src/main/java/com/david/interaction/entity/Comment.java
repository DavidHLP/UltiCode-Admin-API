package com.david.interaction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("comments")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("entity_type")
    private String entityType;

    @TableField("entity_id")
    private Long entityId;

    @TableField("user_id")
    private Long userId;

    @TableField("parent_id")
    private Long parentId;

    private String status;

    private String visibility;

    @TableField("content_md")
    private String contentMd;

    @TableField("content_rendered")
    private String contentRendered;

    @TableField("sensitive_flag")
    private Boolean sensitiveFlag;

    @TableField("sensitive_hits")
    private String sensitiveHits;

    @TableField("moderation_level")
    private String moderationLevel;

    @TableField("moderation_notes")
    private String moderationNotes;

    @TableField("last_moderated_by")
    private Long lastModeratedBy;

    @TableField("last_moderated_at")
    private LocalDateTime lastModeratedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

