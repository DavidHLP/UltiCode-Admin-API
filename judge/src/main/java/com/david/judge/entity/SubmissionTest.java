package com.david.judge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("submission_tests")
public class SubmissionTest {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("submission_id")
    private Long submissionId;

    @TableField("testcase_id")
    private Long testcaseId;

    @TableField("group_id")
    private Long groupId;

    private String verdict;

    @TableField("time_ms")
    private Integer timeMs;

    @TableField("memory_kb")
    private Integer memoryKb;

    private Integer score;

    private String message;
}
