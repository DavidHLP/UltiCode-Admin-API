package com.david.entity;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "testcases", autoResultMap = true)
public class Testcase extends BaseAuditEntity {
	@TableId(type = IdType.AUTO)
	private Long id;
	private Long groupId;
	private Integer orderIndex;


	@TableField(value = "input_json", typeHandler = JacksonTypeHandler.class)
	private Object inputJson; // Map / List / 标量，按实际写入


	@TableField(value = "output_json", typeHandler = JacksonTypeHandler.class)
	private Object outputJson; // Map / List / 标量


	private String outputType; // 可选：int/string/array<int>/custom
	private Integer score;
}