package com.david.judge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("submission_artifacts")
public class SubmissionArtifact {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("submission_id")
    private Long submissionId;

    private String kind;

    @TableField("file_id")
    private Long fileId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
