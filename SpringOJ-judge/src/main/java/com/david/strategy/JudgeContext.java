package com.david.strategy;

import com.david.dto.JudgeResult;
import com.david.judge.Problem;
import com.david.judge.Submission;
import com.david.judge.TestCase;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author david
 * @since 2023/12/5
 */
@Data
@Builder
public class JudgeContext {
    private JudgeResult judgeResult;
    private Submission submission;
    private Problem problem;
    private List<TestCase> testCases;
}
