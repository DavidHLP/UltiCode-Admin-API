package com.david.interaction.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ModerationActionView(
        Long id,
        Long taskId,
        String action,
        Long operatorId,
        String remarks,
        Map<String, Object> context,
        LocalDateTime createdAt) {}

