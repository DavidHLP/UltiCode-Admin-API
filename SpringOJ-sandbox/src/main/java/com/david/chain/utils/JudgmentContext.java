package com.david.chain.utils;

import com.david.enums.LanguageType;

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
	private String compileInfo;
	private String judgeInfo;
    private LanguageType language;
    private String solutionCode;
	private List<TestCaseContext> testCaseContexts;
}
