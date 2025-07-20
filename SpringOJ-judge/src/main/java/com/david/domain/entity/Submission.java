package com.david.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.david.domain.enums.SubmissionStatus;
import lombok.Data;
import java.sql.Timestamp;

@TableName("submissions")
@Data
public class Submission {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("problem_id")
    private Long problemId;

    private String language;

    @TableField("source_code")
    private String sourceCode;

    private SubmissionStatus status;

    private Integer score;

    @TableField("time_used")
    private Integer timeUsed;

    @TableField("memory_used")
    private Integer memoryUsed;

    @TableField("compile_info")
    private String compileInfo;

    @TableField("judge_info")
    private String judgeInfo;

    @TableField(value = "created_at", update = "false")
    private Timestamp createdAt;
}
