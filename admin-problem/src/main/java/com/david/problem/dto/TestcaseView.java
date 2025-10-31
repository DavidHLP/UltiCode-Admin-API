package com.david.problem.dto;

import java.time.LocalDateTime;

public record TestcaseView(
        Long id,
        Long groupId,
        Integer orderIndex,
        Long inputFileId,
        Long outputFileId,
        String inputJson,
        String outputJson,
        String outputType,
        Integer score,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
