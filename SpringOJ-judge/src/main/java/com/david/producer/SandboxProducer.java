package com.david.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SandboxProducer {

	private final KafkaTemplate<String, SandboxExecuteRequest> kafkaTemplate;

}
