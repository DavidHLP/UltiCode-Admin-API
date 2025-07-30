package com.david.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.david.dto.JudgeResult;
import com.david.judge.Submission;
import com.david.service.ISubmissionService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JudgeResultConsumer {
	private final ISubmissionService submissionService;
	@KafkaListener(topics = "judge_result_topic", groupId = "judge-result-consumer-group")
	public void consume(JudgeResult judgeResult) {
		Submission submission = submissionService.getById(judgeResult.getSubmissionId());
		if (submission == null) {
			return;
		}
		submission.setStatus(judgeResult.getStatus());
		submission.setScore(judgeResult.getScore());
		submission.setTimeUsed(judgeResult.getTimeUsed());
		submission.setMemoryUsed(judgeResult.getMemoryUsed());
		submission.setCompileInfo(judgeResult.getCompileInfo());
		submissionService.updateById(submission);
	}
}