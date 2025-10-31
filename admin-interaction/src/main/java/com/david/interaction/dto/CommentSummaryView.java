package com.david.interaction.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record CommentSummaryView(
        Long id,
        String entityType,
        Long entityId,
        Long userId,
        Long parentId,
        String status,
        String visibility,
        String contentPreview,
        Boolean sensitiveFlag,
        String moderationLevel,
        String moderationNotes,
        Long lastModeratedBy,
        LocalDateTime lastModeratedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Map<String, Long> reactionStats,
        ModerationTaskSummaryView moderationTask) {}

