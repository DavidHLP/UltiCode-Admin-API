package com.david.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.david.sandbox.dto.JudgeResult;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JudgeResultProducer {

    private final KafkaTemplate<String, JudgeResult> kafkaTemplate;

    public void sendResult(JudgeResult result) {
        kafkaTemplate.send("judge_result_topic", result);
    }
}
