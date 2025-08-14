package com.david.testcase;

import com.baomidou.mybatisplus.annotation.*;
import com.david.submission.enums.OutputType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("test_cases_outputs")
public class TestCaseOutput implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 测试用例ID，主键，自动增长
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	/**
	 * 关联的题目ID，外键，关联到problems表
	 */
	@TableField("problem_id")
	private Long problemId;

	/**
	 * 期望的输出 (对应数据库中的output字段，类型为text)
	 */
	@TableField("output")
	private String output;

	/**
	 * 输出类型 (varchar(50)，可为空)
	 */
	@TableField("output_type")
	private OutputType outputType;

	/**
	 * 该测试点的分值，默认为10
	 */
	@TableField(value = "score", fill = FieldFill.INSERT)
	@Builder.Default
	private Integer score = 10;

	/**
	 * 是否为样例测试用例，默认为FALSE (tinyint(1))
	 */
	@TableField(value = "is_sample", fill = FieldFill.INSERT)
	@Builder.Default
	private Boolean isSample = false;
}