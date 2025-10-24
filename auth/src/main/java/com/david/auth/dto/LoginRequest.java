package com.david.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String identifier,
        @NotBlank String password,
        String captchaToken,
        String twoFactorCode,
        String sensitiveActionToken) {}
