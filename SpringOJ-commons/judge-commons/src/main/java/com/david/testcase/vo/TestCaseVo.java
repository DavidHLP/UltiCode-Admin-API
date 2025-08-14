package com.david.testcase.vo;

import com.david.testcase.TestCaseInput;
import com.david.testcase.TestCaseOutput;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestCaseVo {
    private Long id;
    private Long problemId;
    private List<TestCaseInput> testCaseInputs;
	private TestCaseOutput testCaseOutput;
}
