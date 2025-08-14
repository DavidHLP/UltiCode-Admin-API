package com.david.consumer;

import com.david.chain.utils.JudgmentContext;
import com.david.chain.utils.TestCaseContext;
import com.david.chain.utils.TestCaseInputContext;
import com.david.chain.utils.TestCaseOutputContext;
import com.david.submission.dto.SubmitToSandboxRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SandboxConsumer {
    @KafkaListener(topics = "submit_to_sandbox_request", groupId = "sandbox-consumer-group")
    public void executionJudgment(SubmitToSandboxRequest request) {
		JudgmentContext judgmentContext = formateJudgmentContext(request);
    }

    private JudgmentContext formateJudgmentContext(SubmitToSandboxRequest request) {
        return JudgmentContext.builder()
                .problemId(request.getProblemId())
                .submissionId(request.getSubmissionId())
                .userId(request.getUserId())
                .language(request.getLanguage())
                .solutionCode(request.getSourceCode())
                .testCaseContexts(
                        request.getTestCases().stream()
                                .map(
                                        testCase ->
                                                TestCaseContext.builder()
                                                        .id(testCase.getId())
                                                        .testCaseOutputContext(
                                                                TestCaseOutputContext.builder()
                                                                        .output(
                                                                                testCase.getTestCaseOutput()
                                                                                        .getOutput())
                                                                        .outputType(
                                                                                testCase.getTestCaseOutput()
                                                                                        .getOutputType())
                                                                        .id(
                                                                                testCase.getTestCaseOutput()
                                                                                        .getId())
                                                                        .build())
                                                        .testCaseInputContexts(
                                                                testCase.getTestCaseInput().stream()
                                                                        .map(
                                                                                testCaseInput ->
                                                                                        TestCaseInputContext
                                                                                                .builder()
                                                                                                .inputType(
                                                                                                        testCaseInput
                                                                                                                .getInputType())
                                                                                                .input(
                                                                                                        testCaseInput
                                                                                                                .getInputContent())
                                                                                                .id(
                                                                                                        testCaseInput
                                                                                                                .getId())
                                                                                                .orderIndex(
                                                                                                        testCaseInput
                                                                                                                .getOrderIndex())
                                                                                                .testCaseName(
                                                                                                        testCaseInput
                                                                                                                .getTestCaseName())
                                                                                                .build())
                                                                        .toList())
                                                        .build())
                                .toList())
                .build();
    }
}
