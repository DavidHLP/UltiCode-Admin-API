package com.david.judge;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.david.judge.enums.ProblemDifficulty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目实体类
 */
@Data
@TableName("problems")
public class Problem implements Serializable {

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
     * 输入格式说明
     */
    private String inputFormat;

    /**
     * 输出格式说明
     */
    private String outputFormat;

    /**
     * 样例输入
     */
    private String sampleInput;

    /**
     * 样例输出
     */
    private String sampleOutput;

    /**
     * 题目提示
     */
    private String hint;

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
     * 题目标签，建议使用JSON格式的字符串存储
     */
    private String tags;

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

    /**
     * 关联的测试用例列表 (非数据库字段)
     */
    @TableField(exist = false)
    private List<TestCase> testCases;
}