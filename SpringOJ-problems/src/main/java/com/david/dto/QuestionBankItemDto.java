package com.david.dto;

import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.david.judge.enums.ProblemDifficulty;

import lombok.Data;

@Data
public class QuestionBankItemDto {
	private Long id;
	private String title;
	private ProblemDifficulty difficulty;
	@TableField(typeHandler = JacksonTypeHandler.class)
	private List<String> tags;
	private Double passRate;
	private QuestionStatus status;
	private Long submissionCount;
}
