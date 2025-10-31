package com.david.problem.dto;

public record ProblemStatementView(
        Long id,
        String langCode,
        String title,
        String descriptionMd,
        String constraintsMd,
        String examplesMd) {}
