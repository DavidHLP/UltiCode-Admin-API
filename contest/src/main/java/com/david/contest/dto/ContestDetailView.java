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
        Long createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Integer problemCount,
        Integer participantCount,
        List<ContestProblemView> problems,
        List<ContestParticipantView> participants) {}
