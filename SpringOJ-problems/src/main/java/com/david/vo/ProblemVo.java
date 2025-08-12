package com.david.vo;



import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.david.judge.enums.CategoryType;
import com.david.judge.enums.ProblemDifficulty;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProblemVo {
	private Long id;
	private String title;
	private ProblemDifficulty difficulty;
	private CategoryType category;
	@TableField(typeHandler = JacksonTypeHandler.class)
	private List<String> tags;
	private Integer passRate;
}
