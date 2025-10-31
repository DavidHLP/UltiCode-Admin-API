package com.david.contest.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ContestScoreboardView(
        Long contestId,
        String kind,
        LocalDateTime generatedAt,
        Integer penaltyPerWrong,
        boolean freezeActive,
        boolean freezeHideScore,
        LocalDateTime freezeStartTime,
        Integer freezeMinutes,
        Integer pendingSubmissionCount,
        List<ContestScoreboardProblemView> problems,
        List<ContestScoreboardParticipantView> participants) {}
