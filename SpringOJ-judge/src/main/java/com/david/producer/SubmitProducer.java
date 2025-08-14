package com.david.producer;

import com.david.submission.dto.SubmitToSandboxRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubmitProducer {

	private final KafkaTemplate<String, SubmitToSandboxRequest> kafkaTemplate;

	public void submitToSandbox(SubmitToSandboxRequest request) {
		kafkaTemplate.send("submit_to_sandbox_request", request);
	}
}
