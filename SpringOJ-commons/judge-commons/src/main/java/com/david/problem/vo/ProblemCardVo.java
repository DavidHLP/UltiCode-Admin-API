package com.david.problem.vo;



import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.david.enums.CategoryType;
import com.david.problem.enums.ProblemDifficulty;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProblemCardVo {
	private Long id;
	private String title;
	private ProblemDifficulty difficulty;
	private CategoryType category;
	@TableField(typeHandler = JacksonTypeHandler.class)
	private List<String> tags;
	private Integer passRate;
}
