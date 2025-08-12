package com.david.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.judge.Submission;
import com.david.judge.enums.JudgeStatus;
import com.david.mapper.SubmissionMapper;
import com.david.service.ISubmissionService;
import com.david.vo.CalendarVo;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 提交记录服务实现类
 *
 * @author david
 * @since 2025-07-22
 */
@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl extends ServiceImpl<SubmissionMapper, Submission>
        implements ISubmissionService {
    private final SubmissionMapper submissionMapper;

    @Override
    public List<JudgeStatus> getSubmissionsStatusByProblemId(Long problemId) {
        return submissionMapper.getSubmissionsStatusByProblemId(problemId);
    }

    @Override
    public List<CalendarVo> getSubmissionCalendar(Long userId) {
        return submissionMapper.getSubmissionCalendar(userId);
    }
}
