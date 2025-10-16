package com.david.admin.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UserView(
        Long id,
        String username,
        String email,
        Integer status,
        String avatarUrl,
        String bio,
        LocalDateTime lastLoginAt,
        String lastLoginIp,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<RoleDto> roles) {}

