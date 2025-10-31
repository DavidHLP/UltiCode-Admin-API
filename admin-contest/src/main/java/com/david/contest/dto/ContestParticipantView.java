package com.david.contest.dto;

import java.time.LocalDateTime;

public record ContestParticipantView(
        Long contestId,
        Long userId,
        String username,
        String displayName,
        LocalDateTime registeredAt) {}
