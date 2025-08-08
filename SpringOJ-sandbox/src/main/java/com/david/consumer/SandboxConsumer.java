package com.david.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.david.chain.UniversalJudgeSandbox;
import com.david.dto.JudgeResult;
import com.david.dto.SandboxExecuteRequest;
import com.david.producer.JudgeResultProducer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SandboxConsumer {

    private final JudgeResultProducer judgeResultProducer;
    private final UniversalJudgeSandbox judgeSandbox;

    @KafkaListener(topics = "sandbox_execute_request_topic", groupId = "sandbox-consumer-group")
    public void consume(SandboxExecuteRequest request) {
        JudgeResult result = judgeSandbox.execute(request);
        judgeResultProducer.sendResult(result);
    }
}