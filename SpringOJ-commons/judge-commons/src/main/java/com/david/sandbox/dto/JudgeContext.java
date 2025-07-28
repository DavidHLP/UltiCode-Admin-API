package com.david.sandbox.dto;

import java.util.List;

import com.david.judge.Problem;
import com.david.judge.Submission;
import com.david.judge.TestCase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author david
 * @since 2023/12/5
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JudgeContext {
    private JudgeResult judgeResult;
    private Submission submission;
    private Problem problem;
    private List<TestCase> testCases;
}
