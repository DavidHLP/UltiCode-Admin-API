package com.david.interaction.dto;

public record CommentQuery(
        Integer page,
        Integer size,
        String status,
        String entityType,
        Long entityId,
        Long userId,
        Boolean sensitiveOnly,
        String keyword,
        String moderationLevel) {}

