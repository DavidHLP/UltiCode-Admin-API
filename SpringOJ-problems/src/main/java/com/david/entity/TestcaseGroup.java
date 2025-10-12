package com.david.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("testcase_groups")
public class TestcaseGroup extends BaseAuditEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long problemId;
    private String name;
    private Boolean isSample;
    private Integer weight;
}
