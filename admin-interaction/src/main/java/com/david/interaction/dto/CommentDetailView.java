package com.david.interaction.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record CommentDetailView(
        Long id,
        String entityType,
        Long entityId,
        Long userId,
        Long parentId,
        String status,
        String visibility,
        String contentMd,
        String contentRendered,
        Boolean sensitiveFlag,
        List<String> sensitiveHits,
        String moderationLevel,
        String moderationNotes,
        Long lastModeratedBy,
        LocalDateTime lastModeratedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Map<String, Long> reactionStats,
        ModerationTaskSummaryView moderationTask) {}

