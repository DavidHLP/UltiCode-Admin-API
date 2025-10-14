package com.david.auth.dto;

import lombok.Builder;

@Builder
public record AuthResponse(
        String tokenType,
        String accessToken,
        long accessTokenExpiresIn,
        String refreshToken,
        long refreshTokenExpiresIn,
        UserProfileDto user) {}
