package com.david.usercontent;

import com.baomidou.mybatisplus.annotation.*;
import com.david.solution.enums.ContentType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户对内容（题目/题解）的独立浏览记录实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_content_views")
public class UserContentView implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 浏览记录ID，主键
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	/**
	 * 浏览用户的ID，关联到user.user_id
	 */
	@TableField("user_id")
	private Long userId;

	/**
	 * 被浏览内容的ID (可能是题目ID或题解ID)
	 */
	@TableField("content_id")
	private Long contentId;

	@TableField("content_type")
	private ContentType contentType;
}
