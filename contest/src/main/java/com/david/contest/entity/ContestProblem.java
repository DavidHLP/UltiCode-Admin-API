package com.david.contest.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("contest_problems")
public class ContestProblem {

    @TableField("contest_id")
    private Long contestId;

    @TableField("problem_id")
    private Long problemId;

    private String alias;

    private Integer points;

    @TableField("order_no")
    private Integer orderNo;
}
