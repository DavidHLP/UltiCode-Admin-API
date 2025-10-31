package com.david.contest.dto;

import java.time.LocalDateTime;

public record ContestSubmissionView(
        Long id,
        Long userId,
        String username,
        Long problemId,
        String alias,
        String verdict,
        Integer score,
        Integer timeMs,
        Integer memoryKb,
        LocalDateTime submittedAt) {}
