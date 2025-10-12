package com.david.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


@Data
@TableName("problem_tags")
public class ProblemTag {
	private Long problemId;
	private Long tagId;
}