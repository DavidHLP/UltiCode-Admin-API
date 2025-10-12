package com.david.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("categories")
@EqualsAndHashCode(callSuper = true)
public class Category extends BaseAuditEntity {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String code;
    private String name;
}
