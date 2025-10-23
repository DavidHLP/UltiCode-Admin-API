package com.david.contest.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("problem_statements")
public class ProblemStatement {

    @TableField("problem_id")
    private Long problemId;

    @TableField("lang_code")
    private String langCode;

    private String title;
}
