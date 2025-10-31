package com.david.problem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("testcase_groups")
public class TestcaseGroup {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("dataset_id")
    private Long datasetId;

    private String name;

    @TableField("is_sample")
    private Integer isSample;

    private Integer weight;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
