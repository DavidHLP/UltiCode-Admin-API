package com.david.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.calendar.vo.CalendarVo;
import com.david.submission.Submission;
import com.david.submission.vo.SubmissionCardVo;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SubmissionMapper extends BaseMapper<Submission> {
    Page<SubmissionCardVo> pageSubmissionCardVos(
            Page<SubmissionCardVo> p,
            @Param("problemId") Long problemId,
            @Param("userId") Long currentUserId);

    List<CalendarVo> getSubmissionCalendar(@Param("userId") Long userId);

    // 用户统计相关
    long countUserSubmissions(@Param("userId") Long userId);

    long countUserAcceptedSubmissions(@Param("userId") Long userId);

    long countUserAttemptedProblems(@Param("userId") Long userId);

    long countUserSolvedProblems(@Param("userId") Long userId);
}
