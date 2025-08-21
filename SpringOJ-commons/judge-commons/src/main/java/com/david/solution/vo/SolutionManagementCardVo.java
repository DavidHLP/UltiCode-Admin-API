package com.david.solution.vo;

import com.david.solution.enums.SolutionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SolutionManagementCardVo {
	private Long id;
	private Long problemId;
	private Long userId;
	private String authorUsername;
	private String authorAvatar;
	private String title;
	private String language;
	private SolutionStatus status;
}
