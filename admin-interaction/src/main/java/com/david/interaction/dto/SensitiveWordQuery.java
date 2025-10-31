package com.david.interaction.dto;

public record SensitiveWordQuery(
        Integer page,
        Integer size,
        String keyword,
        String category,
        String level,
        Boolean active) {}

