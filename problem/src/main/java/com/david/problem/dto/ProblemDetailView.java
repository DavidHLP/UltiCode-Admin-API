package com.david.problem.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ProblemDetailView(
        Long id,
        String slug,
        String problemType,
        Integer difficultyId,
        String difficultyCode,
        Integer categoryId,
        String categoryName,
        Long creatorId,
        String solutionEntry,
        Integer timeLimitMs,
        Integer memoryLimitKb,
        Boolean isPublic,
        Map<String, Object> meta,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ProblemStatementView> statements,
        List<ProblemLanguageConfigView> languageConfigs,
        List<ProblemTagDto> tags) {}
