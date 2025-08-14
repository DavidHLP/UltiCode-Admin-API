package com.david.controller;

import com.david.calendar.vo.CalendarVo;
import com.david.service.impl.CalculationServiceImpl;
import com.david.utils.ResponseResult;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/problems/api/calculate")
public class CalculationController {
	private final CalculationServiceImpl calculationService;
	@GetMapping("/submission/calendar")
	public ResponseResult<List<CalendarVo>> getSubmissionCalendar(Long userId) {
		return ResponseResult.success("获取日历成功",calculationService.getSubmissionCalendar(userId));
	}
}
