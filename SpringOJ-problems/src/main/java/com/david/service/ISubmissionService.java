package com.david.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.david.calendar.vo.CalendarVo;
import com.david.enums.JudgeStatus;
import com.david.submission.Submission;
import com.david.submission.vo.SubmissionCardVo;

import java.util.List;

public interface ISubmissionService extends IService<Submission> {

	Page<SubmissionCardVo> pageSubmissionCardVos(Page<SubmissionCardVo> p, Long problemId, Long currentUserId);

	List<JudgeStatus> getSubmissionsStatusByProblemId(Long problemId);

	List<CalendarVo> getSubmissionCalendar(Long userId);

	// 用户统计相关
	long countUserSubmissions(Long userId);

	long countUserAcceptedSubmissions(Long userId);

	long countUserAttemptedProblems(Long userId);

	long countUserSolvedProblems(Long userId);
}
