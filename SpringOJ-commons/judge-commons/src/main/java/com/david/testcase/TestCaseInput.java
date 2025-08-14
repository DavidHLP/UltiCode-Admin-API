package com.david.testcase;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 测试用例的多个输入表实体类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("test_case_inputs")
public class TestCaseInput implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 输入记录的ID，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联的测试用例ID，外键
     */
    @TableField("test_case_output_id")
    private Long testCaseOutputId;

    /**
     * 输入内容名称
     */
    @TableField("test_case_name")
    private String testCaseName;

    /**
     * 输入类型
     */
    @TableField("input_type")
    private String inputType;

    /**
     * 单个输入的内容
     */
    @TableField("input_content")
    private String inputContent;

    /**
     * 输入的顺序，从0开始，用于保证多次输入的先后次序
     */
    @TableField("order_index")
    private Integer orderIndex;
}
