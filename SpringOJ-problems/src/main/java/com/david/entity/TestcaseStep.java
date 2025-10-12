package com.david.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("testcase_steps")
public class TestcaseStep extends BaseAuditEntity {
	@TableId(type = IdType.AUTO)
	private Long id;
	private Long testcaseId;
	private Integer stepIndex;
	private String inputContent;
	private String expectedOutput;
}