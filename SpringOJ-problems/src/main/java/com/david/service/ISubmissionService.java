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

    Page<SubmissionCardVo> pageSubmissionCardVos(@NotNull(message = "分页对象不能为空") Page<SubmissionCardVo> p,
                                                 @NotNull(message = "题目ID不能为空") @Min(value = 1, message = "题目ID必须>=1") Long problemId,
                                                 @NotNull(message = "当前用户ID不能为空") @Min(value = 1, message = "当前用户ID必须>=1") Long currentUserId);

    List<JudgeStatus> getSubmissionsStatusByProblemId(@NotNull(message = "题目ID不能为空") @Min(value = 1, message = "题目ID必须>=1") Long problemId);

    List<CalendarVo> getSubmissionCalendar(@NotNull(message = "用户ID不能为空") @Min(value = 1, message = "用户ID必须>=1") Long userId);

    // 用户统计相关
    long countUserSubmissions(@NotNull(message = "用户ID不能为空") @Min(value = 1, message = "用户ID必须>=1") Long userId);

    long countUserAcceptedSubmissions(@NotNull(message = "用户ID不能为空") @Min(value = 1, message = "用户ID必须>=1") Long userId);

    long countUserAttemptedProblems(@NotNull(message = "用户ID不能为空") @Min(value = 1, message = "用户ID必须>=1") Long userId);

    long countUserSolvedProblems(@NotNull(message = "用户ID不能为空") @Min(value = 1, message = "用户ID必须>=1") Long userId);
}
