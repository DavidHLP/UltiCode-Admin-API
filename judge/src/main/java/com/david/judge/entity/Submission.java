package com.david.judge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("submissions")
public class Submission {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("problem_id")
    private Long problemId;

    @TableField("dataset_id")
    private Long datasetId;

    @TableField("language_id")
    private Integer languageId;

    @TableField("source_file_id")
    private Long sourceFileId;

    @TableField("code_bytes")
    private Integer codeBytes;

    private String verdict;

    private Integer score;

    @TableField("time_ms")
    private Integer timeMs;

    @TableField("memory_kb")
    private Integer memoryKb;

    @TableField("judge_msg")
    private String judgeMsg;

    @TableField("ip_addr")
    private String ipAddr;

    @TableField("contest_id")
    private Long contestId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
