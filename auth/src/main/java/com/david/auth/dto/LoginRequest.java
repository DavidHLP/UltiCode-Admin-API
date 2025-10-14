package com.david.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    /**
     * Username or email.
     */
    @NotBlank
    private String identifier;

    @NotBlank
    private String password;
}
