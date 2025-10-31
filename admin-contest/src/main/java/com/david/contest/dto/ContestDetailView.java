package com.david.contest.dto;

import com.david.contest.enums.ContestStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ContestDetailView(
        Long id,
        String title,
        String descriptionMd,
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
        Long createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Integer problemCount,
        Integer participantCount,
        Integer pendingRegistrationCount,
        List<ContestProblemView> problems,
        List<ContestParticipantView> participants) {}
