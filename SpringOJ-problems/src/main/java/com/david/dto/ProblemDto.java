package com.david.dto;

import com.david.judge.Problem;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProblemDto extends Problem {
    private Map<String, String> initialCode;
    private List<TestCaseDto> testCases;
}
