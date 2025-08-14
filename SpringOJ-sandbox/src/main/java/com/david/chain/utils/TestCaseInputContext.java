package com.david.chain.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestCaseInputContext {
	private Long id;
	private Integer orderIndex;
	private String input;
	private String testCaseName;
	private String inputType;
}
