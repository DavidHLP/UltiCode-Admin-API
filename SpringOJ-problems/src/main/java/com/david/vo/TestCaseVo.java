package com.david.vo;

import java.util.List;

import com.david.judge.TestCaseInput;
import com.david.judge.TestCaseOutput;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestCaseVo {
	/**
     * 测试用例id-testCaseOutput的id
     */
    private Long id;
    private Long problemId;
    private List<TestCaseInput> testCaseInputs;
	private TestCaseOutput testCaseOutput;
}
