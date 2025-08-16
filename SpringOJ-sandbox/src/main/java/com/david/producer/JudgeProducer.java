package com.david.producer;

import com.david.submission.Submission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JudgeProducer {
	private final KafkaTemplate<String, Submission> kafkaTemplate;
	public void updateSubmission(Submission submission) {
		kafkaTemplate.send("update_to_submission", submission);
	}
}
