package com.david.admin.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RoleView(
        Long id,
        String code,
        String name,
        String remark,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<PermissionDto> permissions) {}
