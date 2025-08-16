package com.david.chain.utils;

import com.david.enums.JudgeStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JudgmentResult {
    /** 测试用例ID，主键 */
    private Long testCaseId;

    /** 判题状态：详见 {@link JudgeStatus} */
    private JudgeStatus judgeStatus;

    /** 运行阶段统计的内存使用（可选）。 存储为无单位的数字字符串，按约定单位 MB 解释。例如："128" 表示 128MB。 */
    private String memoryUsed;

    /** 编译/运行阶段统计的时间消耗（可选）。 存储为无单位的数字字符串，按约定单位 ms 解释。例如："1000" 表示 1000ms。 */
    private String timeUsed;

    /** 编译诊断信息（编译阶段产出），如：OK 或 错误堆栈 */
    private String compileInfo;

    /** 判题结论（对比阶段产出），包含是否通过与原因 */
    private String judgeInfo;
}
