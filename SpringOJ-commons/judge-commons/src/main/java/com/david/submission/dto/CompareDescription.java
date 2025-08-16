package com.david.submission.dto;

import com.david.problem.enums.ProblemDifficulty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompareDescription {
	private String solutionFunctionName;
	private ProblemDifficulty difficulty;
}
