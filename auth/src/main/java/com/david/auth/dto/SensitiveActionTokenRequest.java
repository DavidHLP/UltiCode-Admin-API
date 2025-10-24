package com.david.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record SensitiveActionTokenRequest(@NotBlank String twoFactorCode) {}
