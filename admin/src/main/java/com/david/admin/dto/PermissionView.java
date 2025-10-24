package com.david.admin.dto;

import java.time.LocalDateTime;

public record PermissionView(Long id, String code, String name, LocalDateTime createdAt) {}
