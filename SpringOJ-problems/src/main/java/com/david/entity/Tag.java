package com.david.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tags")
public class Tag extends BaseAuditEntity {
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;
	private String slug;
	private String name;
}