package com.david.vo;

import java.time.LocalDateTime;

import com.david.solution.enums.SolutionStatus;

import lombok.Data;

/**
 * 题解DTO，包含用户信息
 */
@Data
public class SolutionVo {

	/**
	 * 题解ID
	 */
	private Long id;

	/**
	 * 题目ID
	 */
	private Long problemId;

	/**
	 * 用户ID
	 */
	private Long userId;

	/**
	 * 题解标题
	 */
	private String title;

	/**
	 * 题解内容
	 */
	private String content;

	/**
	 * 编程语言
	 */
	private String language;

	/**
	 * 点赞数
	 */
	private Integer upvotes;

	/**
	 * 点踩数
	 */
	private Integer downvotes;

	/**
	 * 浏览量
	 */
	private Integer views;

	/**
	 * 题解状态
	 */
	private SolutionStatus status;

	/**
	 * 创建时间
	 */
	private LocalDateTime createdAt;

	/**
	 * 更新时间
	 */
	private LocalDateTime updatedAt;

	/**
	 * 作者用户名
	 */
	private String authorUsername;

	/**
	 * 作者头像
	 */
	private String authorAvatar;
}
