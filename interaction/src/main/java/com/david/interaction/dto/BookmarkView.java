package com.david.interaction.dto;

import java.time.LocalDateTime;
import java.util.List;

public record BookmarkView(
        Long userId,
        String entityType,
        Long entityId,
        String visibility,
        String note,
        List<String> tags,
        String source,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}

