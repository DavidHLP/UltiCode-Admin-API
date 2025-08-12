package com.david.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.david.judge.Submission;
import com.david.judge.enums.JudgeStatus;
import com.david.vo.CalendarVo;

import java.util.List;

/**
 * <p>
 *  提交记录服务类
 * </p>
 *
 * @author david
 * @since 2025-07-22
 */
public interface ISubmissionService extends IService<Submission> {

	List<JudgeStatus> getSubmissionsStatusByProblemId(Long problemId);
	List<CalendarVo> getSubmissionCalendar(Long userId);
}
