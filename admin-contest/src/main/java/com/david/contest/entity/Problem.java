package com.david.contest.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("problems")
public class Problem {

    @TableId
    private Long id;

    private String slug;

    @TableField("problem_type")
    private String problemType;
}
