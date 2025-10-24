package com.david.admin.dto;

import java.time.LocalDateTime;

public record AuthTokenView(
        Long id,
        Long userId,
        String kind,
        boolean revoked,
        LocalDateTime createdAt,
        LocalDateTime expiresAt) {}
