package com.david.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.david.judge.Submission;
import com.david.judge.enums.JudgeStatus;
import com.david.vo.CalendarVo;
import com.david.vo.SubmissionCardVo;

import java.util.List;

public interface ISubmissionService extends IService<Submission> {

	Page<SubmissionCardVo> pageSubmissionCardVos(Page<SubmissionCardVo> p, Long problemId, Long currentUserId);

	List<JudgeStatus> getSubmissionsStatusByProblemId(Long problemId);

	List<CalendarVo> getSubmissionCalendar(Long userId);
}
