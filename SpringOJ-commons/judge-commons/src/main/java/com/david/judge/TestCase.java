package com.david.judge;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 测试用例实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("test_cases")
public class TestCase implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 测试用例ID，主键，自动增长
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联的题目ID，外键
     */
    private Long problemId;

    /**
     * 输入内容
     */
    private String input;

    /**
     * 期望输出内容
     */
    private String output;

    /**
     * 该测试点的分值，默认为10
     */
    private Integer score;

    /**
     * 是否为样例测试用例，默认为FALSE
     */
    @TableField("is_sample")
    private Boolean sample;

    /**
     * 记录创建时间
     */
    private LocalDateTime createdAt;
}