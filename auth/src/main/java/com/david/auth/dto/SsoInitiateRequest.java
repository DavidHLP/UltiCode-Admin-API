package com.david.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record SsoInitiateRequest(
        @NotBlank String clientId, String state, @Positive(message = "TTL 必须为正数") Long ttlSeconds) {}
