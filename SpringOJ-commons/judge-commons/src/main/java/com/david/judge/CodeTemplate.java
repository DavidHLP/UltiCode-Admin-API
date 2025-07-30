package com.david.judge;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 题目代码模板实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("codetemplate")
public class CodeTemplate implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模板ID，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联的题目ID
     */
    @TableField("problem_id")
    private Long problemId;

    /**
     * 编程语言, 例如: C++, Java, Python
     */
    @TableField("language")
    private String language;

    /**
     * 主函数或核心逻辑外的包装代码模板
     */
    @TableField("main_wrapper_template")
    private String mainWrapperTemplate;

    /**
     * 提供给用户的解题函数/类模板
     */
    @TableField("solution_template")
    private String solutionTemplate;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Date createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;
}