package com.david.vo;

import java.util.List;
import java.util.Map;

import com.david.judge.Problem;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProblemVo extends Problem {
    private Map<String, String> initialCode;
    private List<TestCaseVo> testCases;
}
