package com.david.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.david.judge.enums.ProblemDifficulty;
import lombok.Data;

import java.util.List;

@Data
public class QuestionBankQueryDto {
    private int page = 1;
    private int size = 20;
    private ProblemDifficulty difficulty;
    private QuestionStatus status;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;
    private String searchQuery;
}
