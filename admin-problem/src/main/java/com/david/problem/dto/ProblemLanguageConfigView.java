package com.david.problem.dto;

public record ProblemLanguageConfigView(
        Long id,
        Integer languageId,
        String languageCode,
        String languageName,
        String functionName,
        String starterCode) {}
