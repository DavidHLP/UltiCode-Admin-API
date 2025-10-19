package com.david.interaction.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("reactions")
public class Reaction {

    @TableField("user_id")
    private Long userId;

    @TableField("entity_type")
    private String entityType;

    @TableField("entity_id")
    private Long entityId;

    private String kind;

    private Integer weight;

    private String source;

    private String metadata;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

