package com.david.calculate.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOverviewVo {
    private Long userId;
    private Long totalSubmissions;
    private Long acceptedSubmissions;
    private Long attemptedProblems;
    private Long solvedProblems;
    private Integer passRate; // 0-100
}
