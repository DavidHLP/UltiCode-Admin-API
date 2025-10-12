package com.david.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("problem_locales")
public class ProblemLocale extends BaseAuditEntity {
	@TableId(type = IdType.AUTO)
	private Long id;


	private Long problemId;
	private String langCode; // zh-CN/en/zh-TW
	private String title;
	private String descriptionMd;
	private String constraintsMd;
	private String examplesMd;
}