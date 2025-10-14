package com.david.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenIntrospectRequest(@NotBlank String token) {
}
