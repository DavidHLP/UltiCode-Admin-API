package com.david.judge;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.david.judge.enums.JudgeStatus;
import com.david.judge.enums.LanguageType;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 提交记录实体类
 */
@Data
@TableName("submissions")
public class Submission implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 提交记录ID，主键，自动增长
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 提交用户的ID
     */
    private Long userId;

    /**
     * 题目的ID
     */
    private Long problemId;

    /**
     * 编程语言
     */
    private LanguageType language;

    /**
     * 用户提交的源代码
     */
    private String sourceCode;

    /**
     * 判题状态
     */
    private JudgeStatus status;

    /**
     * 得分
     */
    private Integer score;

    /**
     * 程序执行耗时，单位为毫秒
     */
    private Integer timeUsed;

    /**
     * 程序执行内存消耗，单位为KB
     */
    private Integer memoryUsed;

    /**
     * 编译错误时的详细信息
     */
    private String compileInfo;

    /**
     * 判题的详细信息，建议使用JSON格式存储
     */
    private Map<String, Object> judgeInfo;

    /**
     * 记录创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}