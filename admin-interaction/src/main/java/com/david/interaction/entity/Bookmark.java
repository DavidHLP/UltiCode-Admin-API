package com.david.interaction.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("bookmarks")
public class Bookmark {

    @TableField("user_id")
    private Long userId;

    @TableField("entity_type")
    private String entityType;

    @TableField("entity_id")
    private Long entityId;

    private String visibility;

    private String note;

    private String tags;

    private String source;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

