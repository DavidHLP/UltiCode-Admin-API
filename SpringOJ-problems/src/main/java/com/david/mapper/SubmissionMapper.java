package com.david.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.judge.Submission;
import com.david.judge.enums.JudgeStatus;
import com.david.vo.CalendarVo;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  提交记录Mapper 接口
 * </p>
 *
 * @author david
 * @since 2025-07-22
 */
@Mapper
public interface SubmissionMapper extends BaseMapper<Submission> {
	List<JudgeStatus> getSubmissionsStatusByProblemId(@Param("problemId") Long problemId);
	List<CalendarVo> getSubmissionCalendar(@Param("userId") Long userId);
}
