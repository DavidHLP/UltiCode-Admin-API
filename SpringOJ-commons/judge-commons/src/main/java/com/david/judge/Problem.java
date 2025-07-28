package com.david.judge;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.david.judge.enums.CategoryType;
import com.david.judge.enums.ProblemDifficulty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 题目实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "problems", autoResultMap = true)
public class Problem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 题目ID，主键，自动增长
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 题目标题，不能为空
     */
    private String title;

    /**
     * 题目正文描述，不能为空
     */
    private String description;

    /**
     * 时间限制，单位为毫秒，默认为1000ms
     */
    private Integer timeLimit;

    /**
     * 内存限制，单位为MB，默认为128MB
     */
    private Integer memoryLimit;

    /**
     * 题目难度，枚举类型，默认为'Easy'
     */
    private ProblemDifficulty difficulty;

    /**
     * 题目类别，枚举类型，默认为'Algorithms'
     */
    private CategoryType category;

    /**
     * 题目标签，以JSON数组格式存储
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    /**
     * 成功解答的次数，默认为0
     */
    private Integer solvedCount;

    /**
     * 总提交次数，默认为0
     */
    private Integer submissionCount;

    /**
     * 题目创建者的用户ID
     */
    private Long createdBy;

    /**
     * 题目是否对普通用户可见，默认为TRUE
     */
    private Boolean isVisible;

    /**
     * 记录创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 记录更新时间
     */
    private LocalDateTime updatedAt;
}