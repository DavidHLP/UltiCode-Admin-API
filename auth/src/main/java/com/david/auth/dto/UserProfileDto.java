package com.david.auth.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record UserProfileDto(
        Long id,
        String username,
        String email,
        String avatarUrl,
        String bio,
        Integer status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<String> roles) {}
