package com.david.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SolutionQueryDto {
	private int page;
	private int size;
	private Long problemId;
	private String title;
	private String sort;
}
