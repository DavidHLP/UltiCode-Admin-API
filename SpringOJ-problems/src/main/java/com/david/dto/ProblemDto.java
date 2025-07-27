package com.david.dto;

import com.david.judge.Problem;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProblemDto extends Problem {
    private Map<String, String> initialCode;
    private List<TestCaseDto> testCases;
}
