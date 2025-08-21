package com.david.solution;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.david.solution.enums.SolutionStatus;

import lombok.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** 题解实体类 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("solutions")
public class Solution {

    /** 校验分组 */
    public interface Create {}
    public interface Update {}
    public interface Delete {}

    /** 题解ID，主键，自动增长 */
    @TableId(value = "id", type = IdType.AUTO)
    @NotNull(message = "题解ID不能为空", groups = {Update.class, Delete.class})
    @Min(value = 1, message = "题解ID必须大于等于1", groups = {Update.class, Delete.class})
    private Long id;

    /** 对应的题目ID，关联到problems表 */
    @TableField("problem_id")
    @NotNull(message = "题目ID不能为空", groups = {Create.class, Update.class})
    @Min(value = 1, message = "题目ID必须大于等于1", groups = {Create.class, Update.class})
    private Long problemId;

    /** 题解作者的用户ID，关联到user表 */
    @TableField("user_id")
    @NotNull(message = "用户ID不能为空", groups = {Create.class, Update.class})
    @Min(value = 1, message = "用户ID必须大于等于1", groups = {Create.class, Update.class})
    private Long userId;

    /** 题解标题 */
    @TableField("title")
    @NotBlank(message = "标题不能为空", groups = {Create.class, Update.class})
    @Size(max = 100, message = "标题长度不能超过100字符", groups = {Create.class, Update.class})
    private String title;

    /** 题解内容，使用Markdown格式存储 */
    @TableField("content")
    @NotBlank(message = "内容不能为空", groups = {Create.class, Update.class})
    @Size(max = 20000, message = "内容长度不能超过20000字符", groups = {Create.class, Update.class})
    private String content;

    /** 标签 */
    @TableField(typeHandler = JacksonTypeHandler.class, value = "tags")
    @Size(max = 20, message = "标签数量不能超过20个", groups = {Create.class, Update.class})
    private List<String> tags;

    /** 题解中代码示例所用的编程语言 */
    @TableField("language")
    @NotBlank(message = "语言不能为空", groups = {Create.class, Update.class})
    @Size(max = 50, message = "语言长度不能超过50字符", groups = {Create.class, Update.class})
    private String language;

    /** 浏览量 */
    @TableField("views")
    @Min(value = 0, message = "浏览量不能为负数", groups = {Create.class, Update.class})
    private Integer views;

    /** 点赞数 */
	@TableField(exist = false)
    private Integer upvotes;

    /** 点踩数 */
    @TableField(exist = false)
    private Integer downvotes;

    /** 评论数 */
    @TableField("comments")
    @Min(value = 0, message = "评论数不能为负数", groups = {Create.class, Update.class})
    private Integer comments;

    /** 题解状态 */
    @TableField("status")
    private SolutionStatus status;
}
