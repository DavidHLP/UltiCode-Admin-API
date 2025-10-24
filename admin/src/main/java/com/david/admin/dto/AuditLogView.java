package com.david.admin.dto;

import java.time.LocalDateTime;

public record AuditLogView(
        Long id,
        Long actorId,
        String actorUsername,
        String action,
        String objectType,
        String objectId,
        String description,
        String ipAddress,
        LocalDateTime createdAt) {}
