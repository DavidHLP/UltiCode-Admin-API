package com.david.auth.dto;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record SsoSessionResponse(
        Long userId, String clientId, String token, String state, LocalDateTime expiresAt) {}
