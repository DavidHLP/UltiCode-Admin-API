package com.david.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@TableName("difficulties")
@EqualsAndHashCode(callSuper = true)
public class Difficulty extends BaseAuditEntity {
	@TableId(value = "id", type = IdType.INPUT)
	private Integer id; // 1/2/3
	private String code; // EASY/MEDIUM/HARD
	private Integer sortKey; // 1/2/3
}