package com.david.judge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("judge_jobs")
public class JudgeJob {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("submission_id")
    private Long submissionId;

    @TableField("node_id")
    private Long nodeId;

    private String status;

    private Integer priority;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("started_at")
    private LocalDateTime startedAt;

    @TableField("finished_at")
    private LocalDateTime finishedAt;
}
