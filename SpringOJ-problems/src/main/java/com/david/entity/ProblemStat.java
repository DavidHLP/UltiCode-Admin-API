package com.david.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("problem_stats")
public class ProblemStat {
    @TableId(value = "problem_id")
    private Long problemId;

    private Integer solvedCount;
    private Integer submissionCount;
    private Integer likesCount;
    private Integer dislikesCount;

    // 生成列：可为 null
    private BigDecimal acceptanceRate;

    private java.time.LocalDateTime updatedAt;
}
