package com.david.problem;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.david.enums.CategoryType;
import com.david.problem.enums.ProblemDifficulty;
import com.david.problem.enums.ProblemType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

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
     * 题目类型，不能为空
     */
	@NotNull(message = "题目类型不能为空")
	private ProblemType problemType;

	/**
     * 题目解决方案函数名，不能为空
     */
	@NotBlank(message = "解决方案函数名不能为空")
	@Size(max = 100, message = "解决方案函数名长度不能超过100")
	private String solutionFunctionName;

    /**
     * 题目标题，不能为空
     */
    @NotBlank(message = "题目标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200")
    private String title;

    /**
     * 题目正文描述，不能为空
     */
    @NotBlank(message = "题目描述不能为空")
    @Size(max = 20000, message = "题目描述长度过长")
    private String description;

    /**
     * 题目难度，枚举类型，默认为EASY'
     */
    @NotNull(message = "题目难度不能为空")
    private ProblemDifficulty difficulty;

    /**
     * 题目类别，枚举类型，默认为'Algorithms'
     */
    @NotNull(message = "题目类别不能为空")
    private CategoryType category;

    /**
     * 题目标签，以JSON数组格式存储
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    /**
     * 成功解答的次数，默认为0
     */
    @Min(value = 0, message = "solvedCount不能为负数")
    private Integer solvedCount;

    /**
     * 总提交次数，默认为0
     */
    @Min(value = 0, message = "submissionCount不能为负数")
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