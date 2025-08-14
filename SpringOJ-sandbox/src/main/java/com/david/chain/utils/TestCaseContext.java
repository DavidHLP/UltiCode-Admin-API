package com.david.chain.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestCaseContext {
	private Long id;
	private TestCaseOutputContext testCaseOutputContext;
	private List<TestCaseInputContext> testCaseInputContexts;
}
