package com.david.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.calendar.vo.CalendarVo;
import com.david.enums.JudgeStatus;
import com.david.service.ISolutionService;
import com.david.service.ISubmissionService;
import com.david.solution.vo.SolutionCardVo;
import com.david.submission.vo.SubmissionCardVo;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CalculationServiceImpl {
    private final ISubmissionService submissionService;
    private final ISolutionService solutionService;

    public Integer submissionPassRate(Long problemId) {
        List<JudgeStatus> judgeStatuses =
                submissionService.getSubmissionsStatusByProblemId(problemId);
        if (judgeStatuses.isEmpty()) {
            return 0;
        }
        long acceptedCount =
                judgeStatuses.stream()
                        .filter(judgeStatus -> judgeStatus.equals(JudgeStatus.ACCEPTED))
                        .count();
        return (int) ((acceptedCount * 100) / judgeStatuses.size());
    }

    public List<CalendarVo> getSubmissionCalendar(Long userId) {
        return submissionService.getSubmissionCalendar(userId);
    }

    public Page<SubmissionCardVo> getSubmissionUserInfo(Long userId, Page<SubmissionCardVo> page) {
        return submissionService.pageSubmissionCardVos(page, null, userId);
    }

    public Page<SolutionCardVo> getSolutionUserInfo(
            Long currentUserId, Page<SolutionCardVo> pages) {
        return solutionService.pageSolutionCardVosByUserId(pages, currentUserId);
    }
}

