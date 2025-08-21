package com.david.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.david.calendar.vo.CalendarVo;
import com.david.enums.JudgeStatus;
import com.david.submission.Submission;
import com.david.submission.vo.SubmissionCardVo;

import java.util.List;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Validated
public interface ISubmissionService extends IService<Submission> {

    Page<SubmissionCardVo> pageSubmissionCardVos(@NotNull Page<SubmissionCardVo> p,
                                                 @Min(1) Long problemId,
                                                 @NotNull @Min(1) Long currentUserId);

    List<JudgeStatus> getSubmissionsStatusByProblemId(@NotNull @Min(1) Long problemId);

    List<CalendarVo> getSubmissionCalendar(@NotNull @Min(1) Long userId);

    // 用户统计相关
    long countUserSubmissions(@NotNull @Min(1) Long userId);

    long countUserAcceptedSubmissions(@NotNull @Min(1) Long userId);

    long countUserAttemptedProblems(@NotNull @Min(1) Long userId);

    long countUserSolvedProblems(@NotNull @Min(1) Long userId);
}
