package com.david.problem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("datasets")
public class Dataset {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("problem_id")
    private Long problemId;

    private String name;

    @TableField("is_active")
    private Integer isActive;

    @TableField("checker_type")
    private String checkerType;

    @TableField("checker_file_id")
    private Long checkerFileId;

    @TableField("float_abs_tol")
    private Double floatAbsTol;

    @TableField("float_rel_tol")
    private Double floatRelTol;

    @TableField("created_by")
    private Long createdBy;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
