package com.david.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.sql.Timestamp;

@TableName("problems")
@Data
public class Problem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    @TableField("input_format")
    private String inputFormat;

    @TableField("output_format")
    private String outputFormat;

    @TableField("sample_input")
    private String sampleInput;

    @TableField("sample_output")
    private String sampleOutput;

    private String hint;

    @TableField("time_limit")
    private Integer timeLimit;

    @TableField("memory_limit")
    private Integer memoryLimit;

    private String difficulty;

    private String tags;

    @TableField("solved_count")
    private Integer solvedCount;

    @TableField("submission_count")
    private Integer submissionCount;

    @TableField("created_by")
    private Long createdBy;

    @TableField("is_visible")
    private Boolean isVisible;

    @TableField(value = "created_at", update = "false")
    private Timestamp createdAt;

    @TableField("updated_at")
    private Timestamp updatedAt;
}
