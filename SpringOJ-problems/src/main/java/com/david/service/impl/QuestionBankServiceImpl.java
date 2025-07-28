package com.david.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.dto.QuestionBankItemDto;
import com.david.dto.QuestionBankQueryDto;
import com.david.judge.Submission;
import com.david.judge.enums.JudgeStatus;
import com.david.mapper.ProblemMapper;
import com.david.mapper.SubmissionMapper;
import com.david.service.IQuestionBankService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionBankServiceImpl implements IQuestionBankService {

	private final ProblemMapper problemMapper;
	private final SubmissionMapper submissionMapper;

	@Override
	public Page<QuestionBankItemDto> getQuestionBankPage(QuestionBankQueryDto queryDto, Long userId) {
		Page<QuestionBankItemDto> page = new Page<>(queryDto.getPage(), queryDto.getSize());
		Page<QuestionBankItemDto> res = problemMapper.findProblemsForQuestionBank(page, queryDto, userId);
		for (QuestionBankItemDto item : res.getRecords()) {
			Long SubmissionCount = submissionMapper
					.selectCount(new QueryWrapper<Submission>().eq("problem_id", item.getId()));
			Long AcceptedCount = submissionMapper.selectCount(
					new QueryWrapper<Submission>().eq("problem_id", item.getId()).eq("status", JudgeStatus.ACCEPTED));
			item.setSubmissionCount(SubmissionCount);
			item.setPassRate(SubmissionCount > 0
					? Double.parseDouble(
							String.format("%.1f", (AcceptedCount.doubleValue() / SubmissionCount.doubleValue() * 100)))
					: 0.0);
		}
		return res;
	}
}
