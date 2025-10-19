package com.david.interaction.dto;

public record ModerationTaskQuery(
        Integer page,
        Integer size,
        String status,
        String entityType,
        Long reviewerId,
        String riskLevel,
        String source) {}

