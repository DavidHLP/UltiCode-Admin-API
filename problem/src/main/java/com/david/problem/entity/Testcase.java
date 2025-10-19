package com.david.problem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
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

    @TableField("input_json")
    private String inputJson;

    @TableField("output_json")
    private String outputJson;

    @TableField("output_type")
    private String outputType;

    private Integer score;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
