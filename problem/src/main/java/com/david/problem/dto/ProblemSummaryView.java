package com.david.problem.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ProblemSummaryView(
        Long id,
        String slug,
        String title,
        String problemType,
        Integer difficultyId,
        String difficultyCode,
        Integer categoryId,
        String categoryName,
        Boolean isPublic,
        Integer timeLimitMs,
        Integer memoryLimitKb,
        LocalDateTime updatedAt,
        List<ProblemTagDto> tags,
        Map<String, Object> meta) {}
