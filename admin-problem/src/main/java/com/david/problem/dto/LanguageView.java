package com.david.problem.dto;

public record LanguageView(
        Integer id, String code, String displayName, String runtimeImage, boolean isActive) {}
