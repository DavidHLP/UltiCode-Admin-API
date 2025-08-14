package com.david.chain.utils;

import com.david.enums.LanguageType;
import com.david.testcase.TestCase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JudgmentContext {
    private Long problemId;
    private Long submissionId;
    private Long userId;
    private LanguageType language;
    private String sourceCode;
    private List<TestCase> testCases;
    private Boolean isFinalJudge;
}
