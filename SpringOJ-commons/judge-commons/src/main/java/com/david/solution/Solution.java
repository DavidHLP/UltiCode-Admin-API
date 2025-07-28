package com.david.solution;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import com.david.solution.enums.SolutionStatus;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 题解实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("solutions")
public class Solution {

	/**
	 * 题解ID，主键，自动增长
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	/**
	 * 对应的题目ID，关联到problems表
	 */
	@TableField("problem_id")
	private Long problemId;

	/**
	 * 题解作者的用户ID，关联到user表
	 */
	@TableField("user_id")
	private Long userId;

	/**
	 * 题解标题
	 */
	@TableField("title")
	private String title;

	/**
	 * 题解内容，使用Markdown格式存储
	 */
	@TableField("content")
	private String content;

	/**
	 * 题解中代码示例所用的编程语言
	 */
	@TableField("language")
	private String language;

	/**
	 * 点赞数
	 */
	@TableField("upvotes")
	private Integer upvotes;

	/**
	 * 点踩数
	 */
	@TableField("downvotes")
	private Integer downvotes;

	/**
	 * 题解状态
	 */
	@TableField("status")
	private SolutionStatus status;

	/**
	 * 记录创建时间
	 */
	@TableField(value = "created_at", fill = FieldFill.INSERT)
	private LocalDateTime createdAt;

	/**
	 * 记录更新时间
	 */
	@TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updatedAt;

}