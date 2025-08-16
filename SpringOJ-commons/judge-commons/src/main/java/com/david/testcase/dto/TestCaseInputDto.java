package com.david.testcase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestCaseInputDto {
    private Long id;
    private String testCaseName;
    private String inputType;
    private Integer orderIndex;
}
