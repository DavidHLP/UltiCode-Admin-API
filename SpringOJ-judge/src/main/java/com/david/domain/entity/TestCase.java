package com.david.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.sql.Timestamp;

@TableName("test_cases")
@Data
public class TestCase {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("problem_id")
    private Long problemId;

    @TableField("input_file")
    private String inputFile;

    @TableField("output_file")
    private String outputFile;

    private Integer score;

    @TableField("is_sample")
    private Boolean isSample;

    @TableField(value = "created_at", update = "false")
    private Timestamp createdAt;
}
