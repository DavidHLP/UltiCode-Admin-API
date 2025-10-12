package com.david.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "problems", autoResultMap = true)
public class Problem extends BaseAuditEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String slug;
    private String problemType;

    private Integer difficultyId;
    private Integer categoryId;

    private String solutionEntry;
    private Integer timeLimitMs;
    private Integer memoryLimitKb;

    private Long createdBy;
    private Boolean isVisible;

    @TableField(value = "meta_json", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> metaJson; // 任意扩展信息
}
