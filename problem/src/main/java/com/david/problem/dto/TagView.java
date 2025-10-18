package com.david.problem.dto;

import java.time.LocalDateTime;

public record TagView(Long id, String slug, String name, LocalDateTime createdAt, LocalDateTime updatedAt) {}
