package com.david.solution.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SolutionDetailVo {
	private Long id;

	private Long problemId;

	private Long userId;

	private String authorUsername;

	private String authorAvatar;

	private String content;

	private String title;

	private String language;

	private Integer upvotes;

	private Integer downvotes;

	private Integer comments;

	private Integer views;

	private List<SolutionCommentVo> solutionComments;
}
