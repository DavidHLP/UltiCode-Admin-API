package com.david.interaction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("sensitive_words")
public class SensitiveWord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String word;

    private String category;

    private String level;

    private String replacement;

    private String description;

    @TableField("is_active")
    private Boolean active;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

