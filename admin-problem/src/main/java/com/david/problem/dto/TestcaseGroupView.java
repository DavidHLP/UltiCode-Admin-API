package com.david.problem.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TestcaseGroupView(
        Long id,
        Long datasetId,
        String name,
        boolean isSample,
        Integer weight,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<TestcaseView> testcases) {}
