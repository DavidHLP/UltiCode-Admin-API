package com.david.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.calendar.vo.CalendarVo;
import com.david.enums.JudgeStatus;
import com.david.mapper.SubmissionMapper;
import com.david.service.ISubmissionService;
import com.david.submission.Submission;
import com.david.submission.vo.SubmissionCardVo;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class SubmissionServiceImpl extends ServiceImpl<SubmissionMapper, Submission>
        implements ISubmissionService {

    private final SubmissionMapper submissionMapper;

    @Override
    public Page<SubmissionCardVo> pageSubmissionCardVos(
            Page<SubmissionCardVo> p, Long problemId, Long currentUserId) {
        return submissionMapper.pageSubmissionCardVos(p, problemId, currentUserId);
    }

    @Override
    public List<JudgeStatus> getSubmissionsStatusByProblemId(Long problemId) {
        return this.lambdaQuery()
                .select(Submission::getStatus)
                .eq(Submission::getProblemId, problemId)
                .list()
                .stream()
                .map(Submission::getStatus)
                .toList();
    }

    @Override
    public List<CalendarVo> getSubmissionCalendar(Long userId) {
        return submissionMapper.getSubmissionCalendar(userId);
    }

    @Override
    public Submission getById(Serializable id) {
        return submissionMapper.selectById(id);
    }

    @Override
    public boolean save(Submission entity) {
        return submissionMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateById(Submission entity) {
        return submissionMapper.updateById(entity) > 0;
    }

    @Override
    public long countUserSubmissions(Long userId) {
        return submissionMapper.countUserSubmissions(userId);
    }

    @Override
    public long countUserAcceptedSubmissions(Long userId) {
        return submissionMapper.countUserAcceptedSubmissions(userId);
    }

    @Override
    public long countUserAttemptedProblems(Long userId) {
        return submissionMapper.countUserAttemptedProblems(userId);
    }

    @Override
    public long countUserSolvedProblems(Long userId) {
        return submissionMapper.countUserSolvedProblems(userId);
    }
}
