package com.david.contest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("submissions")
public class Submission {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("problem_id")
    private Long problemId;

    @TableField("contest_id")
    private Long contestId;

    @TableField("verdict")
    private String verdict;

    private Integer score;

    @TableField("time_ms")
    private Integer timeMs;

    @TableField("memory_kb")
    private Integer memoryKb;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
