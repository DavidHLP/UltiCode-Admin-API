package com.david.problem.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DatasetDetailView(
        Long id,
        Long problemId,
        String name,
        boolean isActive,
        String checkerType,
        Long checkerFileId,
        Double floatAbsTol,
        Double floatRelTol,
        Long createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<TestcaseGroupView> groups) {}
