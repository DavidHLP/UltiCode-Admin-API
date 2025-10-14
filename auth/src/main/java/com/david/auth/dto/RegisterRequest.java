package com.david.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
                @Size(min = 3, max = 32)
                @Pattern(
                        regexp = "^[A-Za-z0-9_]+$",
                        message = "Username can only contain letters, numbers, and underscores")
                String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 64) String password) {}
