package com.david.solution.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SolutionCardVo {
	private Long id;

	private Long problemId;

	private Long userId;

	private String authorUsername;

	private String authorAvatar;

	@TableField(typeHandler = JacksonTypeHandler.class)
	private List<String> tags;

	private String contentView;

	private String title;

	private String language;

	private Integer upvotes;

	private Integer downvotes;

	private Integer comments;

	private Integer views;
}