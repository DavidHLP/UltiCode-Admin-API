package com.david.judge;

import java.io.Serial;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 测试用例输入实体类
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
    private Long testCaseId;

    /**
     * 测试用例的名称
     */
    private String testCaseName;

    /**
     * 单个输入的内容
     */
    private String inputContent;

    /**
     * 输入的顺序，从0开始
     */
    private Integer orderIndex;
}
