package com.david.service.impl;

import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.enums.JudgeStatus;
import com.david.exception.BizException;
import com.david.service.ISolutionService;
import com.david.service.ISubmissionService;
import com.david.solution.vo.SolutionCardVo;
import com.david.submission.vo.SubmissionCardVo;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** 使用 Spring Test 的 CalculationServiceImpl 单元测试。 通过 @MockBean 桩掉依赖服务，避免触发真实数据库访问，从而不改变数据库数据。 */
@ExtendWith(SpringExtension.class)
@Import(CalculationServiceImpl.class)
class CalculationServiceImplTest {

    @MockBean private ISubmissionService submissionService;
    @MockBean private ISolutionService solutionService;

    @Resource private CalculationServiceImpl calculationService;

    @Test
    void submissionPassRate_shouldThrow_whenSubmissionStatusIsNull() {
        Long problemId = 1L;
        when(submissionService.getSubmissionsStatusByProblemId(problemId)).thenReturn(null);
        Assertions.assertThrows(
                BizException.class, () -> calculationService.submissionPassRate(problemId));
    }

    @Test
    void submissionPassRate_shouldReturnZero_whenNoSubmission() {
        Long problemId = 2L;
        when(submissionService.getSubmissionsStatusByProblemId(problemId))
                .thenReturn(Collections.emptyList());
        Integer rate = calculationService.submissionPassRate(problemId);
        Assertions.assertEquals(0, rate);
    }

    @Test
    void submissionPassRate_shouldComputeAcceptedRate() {
        Long problemId = 3L;
        List<JudgeStatus> statuses =
                Arrays.asList(JudgeStatus.ACCEPTED, JudgeStatus.WRONG_ANSWER, JudgeStatus.ACCEPTED);
        when(submissionService.getSubmissionsStatusByProblemId(problemId)).thenReturn(statuses);
        Integer rate = calculationService.submissionPassRate(problemId);
        // 2/3 = 66%
        Assertions.assertEquals(66, rate);
    }

    @Test
    void getSubmissionCalendar_shouldThrow_whenNullList() {
        Long userId = 10L;
        when(submissionService.getSubmissionCalendar(userId)).thenReturn(null);
        Assertions.assertThrows(
                BizException.class, () -> calculationService.getSubmissionCalendar(userId));
    }

    @Test
    void getSubmissionUserInfo_shouldThrow_whenInvalidPageSizeOrCurrent() {
        Long userId = 20L;
        // size <= 0
        Page<SubmissionCardVo> p1 = new Page<>(1, 0);
        Assertions.assertThrows(
                BizException.class, () -> calculationService.getSubmissionUserInfo(userId, p1));

        // current <= 0
        Page<SubmissionCardVo> p2 = new Page<>(0, 10);
        Assertions.assertThrows(
                BizException.class, () -> calculationService.getSubmissionUserInfo(userId, p2));
    }

    @Test
    void getSubmissionUserInfo_shouldReturnPage_whenValid() {
        Long userId = 21L;
        Page<SubmissionCardVo> page = new Page<>(1, 10);
        Page<SubmissionCardVo> mocked = new Page<>(1, 10);
        when(submissionService.pageSubmissionCardVos(page, null, userId)).thenReturn(mocked);
        Page<SubmissionCardVo> result = calculationService.getSubmissionUserInfo(userId, page);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getCurrent());
        Assertions.assertEquals(10, result.getSize());
    }

    @Test
    void getSolutionUserInfo_shouldThrow_whenInvalidPageParams() {
        Long currentUserId = 30L;
        Page<SolutionCardVo> p1 = new Page<>(1, 0);
        Assertions.assertThrows(
                BizException.class,
                () -> calculationService.getSolutionUserInfo(currentUserId, p1));

        Page<SolutionCardVo> p2 = new Page<>(0, 10);
        Assertions.assertThrows(
                BizException.class,
                () -> calculationService.getSolutionUserInfo(currentUserId, p2));
    }

    @Test
    void getSolutionUserInfo_shouldReturnPage_whenValid() {
        Long currentUserId = 31L;
        Page<SolutionCardVo> page = new Page<>(1, 10);
        Page<SolutionCardVo> mocked = new Page<>(1, 10);
        when(solutionService.pageSolutionCardVosByUserId(page, currentUserId)).thenReturn(mocked);
        Page<SolutionCardVo> result = calculationService.getSolutionUserInfo(currentUserId, page);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getCurrent());
        Assertions.assertEquals(10, result.getSize());
    }
}
