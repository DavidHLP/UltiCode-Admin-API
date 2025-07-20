package com.david.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.david.entity.enums.ProblemDifficulty;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author david
 * @since 2025-07-21
 */
@Data
@TableName("problems")
public class Problem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private String inputFormat;

    private String outputFormat;

    private String sampleInput;

    private String sampleOutput;

    private String hint;

    private Integer timeLimit;

    private Integer memoryLimit;

    private ProblemDifficulty difficulty;

    private String tags;

    private Integer solvedCount;

    private Integer submissionCount;

    private Long createdBy;

    private Boolean isVisible;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    @TableField(exist = false)
    private List<TestCase> testCases;
}
