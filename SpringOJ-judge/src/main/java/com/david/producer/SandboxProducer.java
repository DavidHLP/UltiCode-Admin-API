package com.david.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.david.sandbox.dto.SandboxExecuteRequest;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SandboxProducer {

    private final KafkaTemplate<String, SandboxExecuteRequest> kafkaTemplate;

    public void executeInSandbox(SandboxExecuteRequest request) {
        kafkaTemplate.send("sandbox_execute_request_topic", request);
    }
}
