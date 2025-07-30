package com.david.judge;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import com.david.dto.InputDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @TableField(exist = false)
    private List<InputDto> inputs;

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
    private Boolean isSample;

    /**
     * 记录创建时间
     */
    private LocalDateTime createdAt;
}
