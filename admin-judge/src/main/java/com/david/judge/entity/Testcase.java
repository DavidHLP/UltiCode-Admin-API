package com.david.judge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("testcases")
public class Testcase {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("group_id")
    private Long groupId;

    @TableField("order_index")
    private Integer orderIndex;

    @TableField("input_file_id")
    private Long inputFileId;

    @TableField("output_file_id")
    private Long outputFileId;

    @TableField("score")
    private Integer score;
}
