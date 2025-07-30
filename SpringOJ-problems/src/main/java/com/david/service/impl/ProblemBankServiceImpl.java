package com.david.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.dto.ProblemBankQueryDto;
import com.david.judge.Submission;
import com.david.judge.enums.JudgeStatus;
import com.david.mapper.ProblemMapper;
import com.david.mapper.SubmissionMapper;
import com.david.service.IProblemBankService;
import com.david.vo.ProblemBankItemVo;
import com.david.vo.SubmissionCalendarVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProblemBankServiceImpl implements IProblemBankService {

	private final ProblemMapper problemMapper;
	private final SubmissionMapper submissionMapper;

	@Override
	public Page<ProblemBankItemVo> getQuestionBankPage(ProblemBankQueryDto queryDto, Long userId) {
		Page<ProblemBankItemVo> page = new Page<>(queryDto.getPage(), queryDto.getSize());
		Page<ProblemBankItemVo> res = problemMapper.findProblemsForQuestionBank(page, queryDto, userId);
		for (ProblemBankItemVo item : res.getRecords()) {
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

	@Override
	public List<SubmissionCalendarVo> getSubmissionCalendar(Long currentUserId) {
		return submissionMapper.getSubmissionCalendar(currentUserId);
	}
}