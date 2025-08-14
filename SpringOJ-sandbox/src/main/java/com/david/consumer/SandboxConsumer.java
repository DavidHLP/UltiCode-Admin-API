package com.david.consumer;

import com.david.submission.dto.SubmitToSandboxRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SandboxConsumer {
    @KafkaListener(topics = "submit_to_sandbox_request", groupId = "sandbox-consumer-group")
    public void executionJudgment(SubmitToSandboxRequest request) {}
}