package com.david.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record SensitiveActionTokenVerifyRequest(@NotBlank String token) {}
