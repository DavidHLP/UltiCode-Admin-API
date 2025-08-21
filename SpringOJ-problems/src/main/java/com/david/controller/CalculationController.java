package com.david.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.calendar.vo.CalendarVo;
import com.david.service.impl.CalculationServiceImpl;
import com.david.solution.vo.SolutionCardVo;
import com.david.submission.vo.SubmissionCardVo;
import com.david.utils.BaseController;
import com.david.utils.ResponseResult;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/problems/api/calculate")
public class CalculationController extends BaseController {
    private final CalculationServiceImpl calculationService;

    @GetMapping("/submission/calendar")
    public ResponseResult<List<CalendarVo>> getSubmissionCalendar(Long userId) {
        return ResponseResult.success("获取日历成功", calculationService.getSubmissionCalendar(userId));
    }

    @GetMapping("/submission/userInfo")
    public ResponseResult<Page<SubmissionCardVo>> getSubmissionPassRate(
            @RequestParam long page, @RequestParam long size) {
        Page<SubmissionCardVo> pages = new Page<>(page, size);
        return ResponseResult.success(
                "成功获取提交信息", calculationService.getSubmissionUserInfo(getCurrentUserId(), pages));
    }

    @GetMapping("/solution/userInfo")
    public ResponseResult<Page<SolutionCardVo>> getSolutionPassRate(
            @RequestParam long page, @RequestParam long size) {
        Page<SolutionCardVo> pages = new Page<>(page, size);
        return ResponseResult.success(
                "成功获取提交信息", calculationService.getSolutionUserInfo(getCurrentUserId(), pages));
    }
}
