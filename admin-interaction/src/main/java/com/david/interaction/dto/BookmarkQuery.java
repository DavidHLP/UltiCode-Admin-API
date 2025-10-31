package com.david.interaction.dto;

public record BookmarkQuery(
        Integer page,
        Integer size,
        Long userId,
        String entityType,
        Long entityId,
        String visibility,
        String source) {}

