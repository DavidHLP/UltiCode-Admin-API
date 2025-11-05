package com.david.interaction.dto;

public record ReactionQuery(
        Integer page,
        Integer size,
        Long userId,
        String entityType,
        Long entityId,
        String kind,
        String source,
        String keyword) {}
