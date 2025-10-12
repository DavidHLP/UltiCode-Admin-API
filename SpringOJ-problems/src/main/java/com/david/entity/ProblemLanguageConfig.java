package com.david.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("problem_language_configs")
public class ProblemLanguageConfig {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long problemId;
    private String language; // python3/cpp/java/sql
    private String functionName; // 语言级入口名
    private String starterCode; // 模板代码
}
