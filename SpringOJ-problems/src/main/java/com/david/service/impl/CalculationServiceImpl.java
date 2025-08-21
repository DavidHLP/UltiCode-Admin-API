package com.david.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.calendar.vo.CalendarVo;
import com.david.enums.JudgeStatus;
import com.david.service.ISolutionService;
import com.david.service.ISubmissionService;
import com.david.solution.vo.SolutionCardVo;
import com.david.submission.vo.SubmissionCardVo;
import com.david.exception.BizException;
import com.david.utils.enums.ResponseCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class CalculationServiceImpl {
    private final ISubmissionService submissionService;
    private final ISolutionService solutionService;

    public Integer submissionPassRate(@NotNull @Min(1) Long problemId) {
        List<JudgeStatus> judgeStatuses =
                submissionService.getSubmissionsStatusByProblemId(problemId);
        if (judgeStatuses == null) {
            throw BizException.of(ResponseCode.RC500.getCode(), "提交状态数据为空");
        }
        if (judgeStatuses.isEmpty()) {
            return 0; // 无提交，默认通过率0%
        }
        for (int i = 0; i < judgeStatuses.size(); i++) {
            if (judgeStatuses.get(i) == null) {
                throw BizException.of(ResponseCode.RC500.getCode(), "提交状态包含空元素，索引=" + i);
            }
        }
        long acceptedCount =
                judgeStatuses.stream()
                        .filter(judgeStatus -> judgeStatus == JudgeStatus.ACCEPTED)
                        .count();
        int size = judgeStatuses.size();
        if (size <= 0) {
            throw BizException.of(ResponseCode.RC500.getCode(), "提交状态统计异常");
        }
        return (int) ((acceptedCount * 100) / size);
    }

    public List<CalendarVo> getSubmissionCalendar(@NotNull @Min(1) Long userId) {
        List<CalendarVo> list = submissionService.getSubmissionCalendar(userId);
        if (list == null) {
            throw BizException.of(ResponseCode.RC500.getCode(), "用户提交日历数据为空");
        }
        return list;
    }

    public Page<SubmissionCardVo> getSubmissionUserInfo(@NotNull @Min(1) Long userId, @NotNull Page<SubmissionCardVo> page) {
        if (page.getSize() <= 0) {
            throw BizException.of(ResponseCode.RC400.getCode(), "分页大小必须>0");
        }
        if (page.getCurrent() <= 0) {
            throw BizException.of(ResponseCode.RC400.getCode(), "页码必须>0");
        }
        Page<SubmissionCardVo> result = submissionService.pageSubmissionCardVos(page, null, userId);
        if (result == null) {
            throw BizException.of(ResponseCode.RC500.getCode(), "用户提交分页数据为空");
        }
        return result;
    }

    public Page<SolutionCardVo> getSolutionUserInfo(
            @NotNull @Min(1) Long currentUserId, @NotNull Page<SolutionCardVo> pages) {
        if (pages.getSize() <= 0) {
            throw BizException.of(ResponseCode.RC400.getCode(), "分页大小必须>0");
        }
        if (pages.getCurrent() <= 0) {
            throw BizException.of(ResponseCode.RC400.getCode(), "页码必须>0");
        }
        Page<SolutionCardVo> result = solutionService.pageSolutionCardVosByUserId(pages, currentUserId);
        if (result == null) {
            throw BizException.of(ResponseCode.RC500.getCode(), "用户题解分页数据为空");
        }
        return result;
    }
}

