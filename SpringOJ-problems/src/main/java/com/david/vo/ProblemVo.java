package com.david.vo;

import java.util.List;

import com.david.judge.Problem;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProblemVo extends Problem {
    private List<CodeTemplateVo> initialCode;
    private List<TestCaseVo> testCases;
}
