package com.david.interaction.dto;

import java.time.LocalDateTime;

public record ReactionView(
        Long userId,
        String entityType,
        Long entityId,
        String kind,
        Integer weight,
        String source,
        String metadata,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}

