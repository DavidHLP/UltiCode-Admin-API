package com.david.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author david
 * @since 2025-07-21
 */
@Data
@TableName("test_cases")
public class TestCase implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long problemId;

    private String inputFile;

    private String outputFile;

    private Integer score;

    private Boolean isSample;

    private Timestamp createdAt;
}
