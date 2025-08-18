package com.david.submission;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.david.enums.JudgeStatus;
import com.david.enums.LanguageType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 提交记录实体类，对应数据库表 {@code submissions}。
 *
 * <p>记录一次判题提交的完整信息：提交标识、用户与题目、编程语言、 源码、判题状态、错误用例摘要、得分及资源消耗、编译与判题日志。
 *
 * <p>字段约定：
 *
 * <ul>
 *   <li><b>language</b>：与 {@link LanguageType} 对应。
 *   <li><b>status</b>：与 {@link JudgeStatus} 对应。
 *   <li><b>timeUsed</b>：耗时，单位毫秒（ms）。
 *   <li><b>memoryUsed</b>：内存，单位 KB。
 * </ul>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("submissions")
public class Submission implements Serializable {

    /** 序列化版本号。 */
    @Serial private static final long serialVersionUID = 1L;

    /** 提交记录主键 ID，自动增长。 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 提交用户的 ID。 */
    private Long userId;

    /** 题目的 ID。 */
    private Long problemId;

    /** 编程语言类型，取值见 {@link LanguageType}。 */
    private LanguageType language;

    /** 用户提交的源代码 */
    private String sourceCode;

    /** 判题状态，取值见 {@link JudgeStatus}。 */
    private JudgeStatus status;

    /** 首个错误测试用例的 ID（如有）。 */
    private Long errorTestCaseId;

    /** 错误测试用例的实际输出，可读文本。 */
    private String errorTestCaseOutput;

    /** 错误测试用例的期望输出，可读文本。 */
    private String errorTestCaseExpectOutput;

    /** 得分（具体取值范围由题目/评测策略定义）。 */
    private Integer score;

    /** 程序执行耗时（毫秒）。 */
    private Integer timeUsed;

    /** 程序执行内存消耗（KB）。 */
    private Integer memoryUsed;

    /** 编译阶段的详细信息（如错误/警告日志）。 */
    private String compileInfo;

    /** 判题阶段的详细信息（例如各测试点结果、错误信息等），通常为 JSON 字符串。 */
    private String judgeInfo;
}
