package com.david.contest.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ContestProblemView(
        Long contestId,
        Long problemId,
        String problemSlug,
        String problemTitle,
        String alias,
        Integer points,
        Integer orderNo,
        LocalDateTime lastSubmissionAt,
        Integer submissionCount,
        Integer solvedCount,
        BigDecimal acceptanceRate) {}
