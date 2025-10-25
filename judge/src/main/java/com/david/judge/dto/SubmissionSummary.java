package com.david.judge.dto;

import java.time.LocalDateTime;

public record SubmissionSummary(
        Long id,
        String verdict,
        Integer score,
        Integer timeMs,
        Integer memoryKb,
        Integer codeBytes,
        LocalDateTime createdAt,
        UserSummary user,
        ProblemSummary problem,
        LanguageSummary language) {}
