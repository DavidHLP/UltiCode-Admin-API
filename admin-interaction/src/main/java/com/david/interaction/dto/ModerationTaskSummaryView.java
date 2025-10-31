package com.david.interaction.dto;

import java.time.LocalDateTime;

public record ModerationTaskSummaryView(
        Long id,
        String entityType,
        Long entityId,
        String status,
        Integer priority,
        String source,
        String riskLevel,
        Long reviewerId,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime reviewedAt) {}

