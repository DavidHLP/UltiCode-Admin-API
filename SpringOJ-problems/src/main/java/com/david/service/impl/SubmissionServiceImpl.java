package com.david.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.calendar.vo.CalendarVo;
import com.david.enums.JudgeStatus;
import com.david.exception.BizException;
import com.david.mapper.SubmissionMapper;
import com.david.redis.commons.annotation.RedisCacheable;
import com.david.redis.commons.annotation.RedisEvict;
import com.david.service.ISubmissionService;
import com.david.submission.Submission;
import com.david.submission.vo.SubmissionCardVo;
import com.david.utils.enums.ResponseCode;

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
    @RedisCacheable(
            key = "'solution:pageSubmissionCardVos:' + #problemId + ':' + #currentUserId",
            keyPrefix = "springoj:cache:",
            ttl = 1800,
            type = Page.class)
    public Page<SubmissionCardVo> pageSubmissionCardVos(
            Page<SubmissionCardVo> p, Long problemId, Long currentUserId) {
        // 分页与参数校验
        validateAndNormalizePage(p);
        validateRequiredId("题目ID", problemId);
        validateRequiredId("当前用户ID", currentUserId);
        return submissionMapper.pageSubmissionCardVos(p, problemId, currentUserId);
    }

    @Override
    @RedisCacheable(
            key = "'solution:getSubmissionsStatusByProblemId:' + #problemId",
            keyPrefix = "springoj:cache:",
            ttl = 1800,
            type = List.class)
    public List<JudgeStatus> getSubmissionsStatusByProblemId(Long problemId) {
        validateRequiredId("题目ID", problemId);
        return this.lambdaQuery()
                .select(Submission::getStatus)
                .eq(Submission::getProblemId, problemId)
                .list()
                .stream()
                .map(Submission::getStatus)
                .toList();
    }

    @Override
    @RedisCacheable(
            key = "'solution:getSubmissionCalendar:' + #userId",
            keyPrefix = "springoj:cache:",
            ttl = 1800,
            type = List.class)
    public List<CalendarVo> getSubmissionCalendar(Long userId) {
        validateRequiredId("用户ID", userId);
        return submissionMapper.getSubmissionCalendar(userId);
    }

    @Override
    @RedisCacheable(
            key = "'solution:getById:' + #id",
            keyPrefix = "springoj:cache:",
            ttl = 1800,
            type = Submission.class)
    public Submission getById(Serializable id) {
        return submissionMapper.selectById(id);
    }

    @Override
    @RedisEvict(
            keys = {
                "'solution:getSubmissionCalendar:' + #entity.getUserId()",
                "'solution:pageSubmissionCardVos:' + #entity.getProblemId() + ':' + #entity.getUserId()",
                "'solution:getSubmissionsStatusByProblemId:' + #entity.getProblemId()"
            },
            keyPrefix = "springoj:cache:")
    public boolean save(Submission entity) {
        validateRequiredId("用户ID", entity.getUserId());
        validateRequiredId("题目ID", entity.getProblemId());
        return submissionMapper.insert(entity) > 0;
    }

    @Override
    @RedisEvict(
            keys = {
                "'solution:getSubmissionCalendar:' + #entity.getUserId()",
                "'solution:pageSubmissionCardVos:' + #entity.getProblemId() + ':' + #entity.getUserId()",
                "'solution:getSubmissionsStatusByProblemId:' + #entity.getProblemId()",
                "'solution:getById:' + #entity.getId()"
            },
            keyPrefix = "springoj:cache:")
    public boolean updateById(Submission entity) {
        validateRequiredId("用户ID", entity.getUserId());
        validateRequiredId("题目ID", entity.getProblemId());
        return submissionMapper.updateById(entity) > 0;
    }

    @Override
    public long countUserSubmissions(Long userId) {
        validateRequiredId("用户ID", userId);
        return submissionMapper.countUserSubmissions(userId);
    }

    @Override
    public long countUserAcceptedSubmissions(Long userId) {
        validateRequiredId("用户ID", userId);
        return submissionMapper.countUserAcceptedSubmissions(userId);
    }

    @Override
    public long countUserAttemptedProblems(Long userId) {
        validateRequiredId("用户ID", userId);
        return submissionMapper.countUserAttemptedProblems(userId);
    }

    @Override
    public long countUserSolvedProblems(Long userId) {
        validateRequiredId("用户ID", userId);
        return submissionMapper.countUserSolvedProblems(userId);
    }

    private void validateAndNormalizePage(Page<?> page) {
        if (page == null) {
            throw BizException.of(ResponseCode.RC400.getCode(), "分页对象不能为空");
        }
        if (page.getCurrent() < 1) {
            throw BizException.of(
                    ResponseCode.RC400.getCode(), "分页参数无效：current必须>=1，当前值：" + page.getCurrent());
        }
        if (page.getSize() < 1) {
            throw BizException.of(
                    ResponseCode.RC400.getCode(), "分页参数无效：size必须>=1，当前值：" + page.getSize());
        }
        if (page.getSize() > 100) {
            page.setSize(100);
        }
    }

    private void validateRequiredId(String fieldName, Long id) {
        if (id == null) {
            throw BizException.of(ResponseCode.RC400.getCode(), fieldName + "不能为空");
        }
        if (id < 1) {
            throw BizException.of(ResponseCode.RC400.getCode(), fieldName + "必须>=1，当前值：" + id);
        }
    }
}
