package com.david.problem.dto;

import java.util.List;

public record ProblemOptionsResponse(
        List<DictionaryOption> difficulties,
        List<DictionaryOption> categories,
        List<TagOption> tags,
        List<LanguageOption> languages,
        List<String> problemTypes) {}
