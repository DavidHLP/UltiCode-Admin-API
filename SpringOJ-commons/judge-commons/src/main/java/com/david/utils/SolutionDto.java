package com.david.utils;

import com.david.testcase.dto.TestCaseInputDto;
import com.david.testcase.dto.TestCaseOutputDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SolutionDto {
	String solutionFunctionName;
	TestCaseOutputDto testCaseOutput;
	List<TestCaseInputDto> testCaseInputs;
}
