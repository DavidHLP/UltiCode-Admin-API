package com.david.solution;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.david.solution.enums.SolutionStatus;

import lombok.*;

import java.util.List;

/** 题解实体类 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("solutions")
public class Solution {

    /** 题解ID，主键，自动增长 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 对应的题目ID，关联到problems表 */
    @TableField("problem_id")
    private Long problemId;

    /** 题解作者的用户ID，关联到user表 */
    @TableField("user_id")
    private Long userId;

    /** 题解标题 */
    @TableField("title")
    private String title;

    /** 题解内容，使用Markdown格式存储 */
    @TableField("content")
    private String content;

    /** 标签 */
    @TableField(typeHandler = JacksonTypeHandler.class, value = "tags")
    private List<String> tags;

    /** 题解中代码示例所用的编程语言 */
    @TableField("language")
    private String language;

    /** 浏览量 */
    @TableField("views")
    private Integer views;

    /** 点赞数 */
	@TableField(exist = false)
    private Integer upvotes;

    /** 点踩数 */
    @TableField(exist = false)
    private Integer downvotes;

    /** 评论数 */
    @TableField("comments")
    private Integer comments;

    /** 题解状态 */
    @TableField("status")
    private SolutionStatus status;
}
