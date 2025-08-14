package com.david.submission.dto;

import com.david.enums.LanguageType;
import com.david.testcase.vo.TestCaseVo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmitToSandboxRequest {
    private Long problemId;
	private Long submissionId;
    private LanguageType language;
    private String sourceCode;
	private List<TestCaseVo> testCaseVos;
}
