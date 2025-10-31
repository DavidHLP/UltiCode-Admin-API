package com.david.contest.dto;

import com.david.contest.enums.ContestStatus;

import java.time.LocalDateTime;

public record ContestSummaryView(
        Long id,
        String title,
        String kind,
        boolean visible,
        ContestStatus status,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String registrationMode,
        LocalDateTime registrationStartTime,
        LocalDateTime registrationEndTime,
        Integer maxParticipants,
        Integer penaltyPerWrong,
        Integer scoreboardFreezeMinutes,
        Boolean hideScoreDuringFreeze,
        Integer problemCount,
        Integer participantCount,
        LocalDateTime lastSubmissionAt,
        LocalDateTime updatedAt) {}
