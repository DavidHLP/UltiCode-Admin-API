package com.david.service.impl;

import com.david.calendar.vo.CalendarVo;
import com.david.enums.JudgeStatus;
import com.david.service.ISubmissionService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CalculationServiceImpl {
    private final ISubmissionService submissionService;

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
}
