package com.david.contest.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("vw_problem_stats")
public class ProblemStatsView {

    @TableField("problem_id")
    private Long problemId;

    @TableField("submission_count")
    private Integer submissionCount;

    @TableField("solved_count")
    private Integer solvedCount;

    @TableField("acceptance_rate")
    private BigDecimal acceptanceRate;

    @TableField("last_submission_at")
    private LocalDateTime lastSubmissionAt;
}
