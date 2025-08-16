package com.david.consumer;

import com.david.service.ISubmissionService;
import com.david.submission.Submission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JudgeConsumer {
    private final ISubmissionService submissionService;

    @KafkaListener(topics = "update_to_submission", groupId = "problems-consumer-group")
    public void updateSubmission(Submission request) {
        submissionService.updateById(request);
    }
}
