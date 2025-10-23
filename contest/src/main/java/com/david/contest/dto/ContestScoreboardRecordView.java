package com.david.contest.dto;

import java.time.LocalDateTime;

public record ContestScoreboardRecordView(
        Long problemId,
        String alias,
        Integer attempts,
        Integer wrongAttempts,
        Integer bestScore,
        Integer contestPoints,
        String lastVerdict,
        LocalDateTime firstAcceptedAt,
        LocalDateTime lastSubmissionAt,
        Integer globalBestScore) {}
