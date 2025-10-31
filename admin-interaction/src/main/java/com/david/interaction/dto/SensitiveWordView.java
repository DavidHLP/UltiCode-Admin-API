package com.david.interaction.dto;

import java.time.LocalDateTime;

public record SensitiveWordView(
        Long id,
        String word,
        String category,
        String level,
        String replacement,
        String description,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}

