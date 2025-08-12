package com.david.vo;

import com.david.judge.TestCaseInput;
import com.david.judge.TestCaseOutput;

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
