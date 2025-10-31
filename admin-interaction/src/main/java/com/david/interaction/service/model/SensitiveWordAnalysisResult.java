package com.david.interaction.service.model;

import java.util.Collections;
import java.util.List;

public record SensitiveWordAnalysisResult(
        boolean hasSensitive,
        boolean blocked,
        boolean needReview,
        String riskLevel,
        List<String> hits) {

    public SensitiveWordAnalysisResult {
        hits = hits == null ? List.of() : List.copyOf(hits);
    }

    public static SensitiveWordAnalysisResult empty() {
        return new SensitiveWordAnalysisResult(false, false, false, "low", Collections.emptyList());
    }
}
