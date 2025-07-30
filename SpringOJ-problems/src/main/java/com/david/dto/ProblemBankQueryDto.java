package com.david.dto;

import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.david.enums.ProblemStatus;
import com.david.judge.enums.CategoryType;
import com.david.judge.enums.ProblemDifficulty;

import lombok.Data;

@Data
public class ProblemBankQueryDto {
	private int page;
	private int size;
	private CategoryType category;
	private ProblemDifficulty difficulty;
	private ProblemStatus status;
	@TableField(typeHandler = JacksonTypeHandler.class)
	private List<String> tags;
	private String title;
}
