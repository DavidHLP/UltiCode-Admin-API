package com.david.contest.dto;

import com.david.contest.enums.ContestRegistrationStatus;

import java.time.LocalDateTime;

public record ContestRegistrationView(
        Long id,
        Long contestId,
        Long userId,
        String username,
        String displayName,
        ContestRegistrationStatus status,
        String source,
        String note,
        Long reviewedBy,
        String reviewerName,
        LocalDateTime reviewedAt,
        LocalDateTime createdAt) {}
