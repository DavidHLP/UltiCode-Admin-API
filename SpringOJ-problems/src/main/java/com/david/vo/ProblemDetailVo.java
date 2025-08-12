package com.david.vo;

import com.david.judge.enums.ProblemDifficulty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProblemDetailVo {
	private Long id;
	private String title;
	private String description;
	private ProblemDifficulty difficulty;
}
