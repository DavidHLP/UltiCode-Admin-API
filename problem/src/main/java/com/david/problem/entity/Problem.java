package com.david.problem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("problems")
public class Problem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String slug;

    @TableField("problem_type")
    private String problemType;

    @TableField("difficulty_id")
    private Integer difficultyId;

    @TableField("category_id")
    private Integer categoryId;

    @TableField("creator_id")
    private Long creatorId;

    @TableField("solution_entry")
    private String solutionEntry;

    @TableField("time_limit_ms")
    private Integer timeLimitMs;

    @TableField("memory_limit_kb")
    private Integer memoryLimitKb;

    @TableField("is_public")
    private Integer isPublic;

    @TableField("meta_json")
    private String metaJson;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
