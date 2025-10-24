package com.david.auth.dto;

import lombok.Builder;

@Builder
public record TwoFactorSetupResponse(
        boolean enabled, String secret, String provisioningUri, long userId) {}
