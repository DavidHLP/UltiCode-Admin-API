package com.david.domain.dto;

import com.david.domain.entity.Problem;
import com.david.domain.entity.Submission;
import com.david.domain.entity.TestCase;
import lombok.Data;
import java.util.List;

@Data
public class JudgeContext {
    private Submission submission;
    private Problem problem;
    private List<TestCase> testCases;
}
