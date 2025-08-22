package com.david.testcase;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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

    /** 校验分组 */
    public interface Create {}
    public interface Update {}
    public interface Delete {}

    /**
     * 输入记录的ID，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    @NotNull(message = "输入记录ID不能为空", groups = {Update.class, Delete.class})
    @Min(value = 1, message = "输入记录ID必须>=1", groups = {Update.class, Delete.class})
    private Long id;

    /**
     * 关联的测试用例ID，外键
     */
    @TableField("test_case_output_id")
    @NotNull(message = "测试用例输出ID不能为空", groups = {Create.class, Update.class})
    @Min(value = 1, message = "测试用例输出ID必须>=1", groups = {Create.class, Update.class})
    private Long testCaseOutputId;

    /**
     * 输入内容名称
     */
    @TableField("test_case_name")
    @Size(max = 50, message = "输入内容名称长度不能超过50字符", groups = {Create.class, Update.class})
    private String testCaseName;

    /**
     * 输入类型
     */
    @TableField("input_type")
    @Size(max = 50, message = "输入类型长度不能超过50字符", groups = {Create.class, Update.class})
    private String inputType;

    /**
     * 单个输入的内容
     */
    @TableField("input_content")
    @NotBlank(message = "输入内容不能为空", groups = {Create.class, Update.class})
    private String inputContent;

    /**
     * 输入的顺序，从0开始，用于保证多次输入的先后次序
     */
    @TableField("order_index")
    @NotNull(message = "输入顺序不能为空", groups = {Create.class, Update.class})
    @Min(value = 0, message = "输入顺序必须>=0", groups = {Create.class, Update.class})
    private Integer orderIndex;
}
