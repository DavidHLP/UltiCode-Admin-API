package com.david.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.david.judge.enums.CategoryType;
import com.david.judge.enums.ProblemDifficulty;
import lombok.Data;

import java.util.List;

@Data
public class QuestionBankItemDto {
    private Long id;
    private String title;
    private CategoryType category;
    private ProblemDifficulty difficulty;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;
    private Double passRate;
    private QuestionStatus status;
    private Integer submissionCount;
}
