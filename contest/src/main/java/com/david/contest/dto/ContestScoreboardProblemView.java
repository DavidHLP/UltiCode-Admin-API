package com.david.contest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ContestScoreboardProblemView(
        Long problemId,
        String alias,
        String title,
        Integer orderNo,
        Integer points,
        Integer submissionCount,
        Integer solvedCount,
        BigDecimal acceptanceRate,
        LocalDateTime lastSubmissionAt) {}
