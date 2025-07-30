package com.david.vo;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SolutionCardVo {
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
	 * 作者用户名
	 */
	private String authorUsername;

	/**
	 * 作者头像
	 */
	private String authorAvatar;

	/**
	 * 标签列表
	 */
	@TableField(typeHandler = JacksonTypeHandler.class)
	private List<String> tags;

	/**
	 * 题解内容预览部分
	 */
	private String problem;

	/**
	 * 题解标题
	 */
	private String title;

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
	 * 评论数
	 */
	private Integer comments;

	/**
	 * 浏览量
	 */
	private Integer views;

	/**
	 * 创建时间
	 */
	private LocalDateTime createdAt;

	/**
	 * 更新时间
	 */
	private LocalDateTime updatedAt;
}
