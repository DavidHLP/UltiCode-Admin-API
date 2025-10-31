package com.david.contest.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ContestScoreboardParticipantView(
        Long userId,
        String username,
        String displayName,
        Integer rank,
        Integer solvedCount,
        Integer totalScore,
        Long penalty,
        LocalDateTime lastAcceptedAt,
        LocalDateTime lastSubmissionAt,
        Integer pendingCount,
        List<ContestScoreboardRecordView> records) {}
