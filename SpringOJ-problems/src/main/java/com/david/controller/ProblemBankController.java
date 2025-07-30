package com.david.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.dto.ProblemBankQueryDto;
import com.david.service.IProblemBankService;
import com.david.utils.BaseController;
import com.david.utils.ResponseResult;
import com.david.vo.ProblemBankItemVo;
import com.david.vo.SubmissionCalendarVo;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/problem-bank/api")
public class ProblemBankController extends BaseController {

	private final IProblemBankService questionBankService;

	@GetMapping
	public ResponseResult<Page<ProblemBankItemVo>> getQuestionBank(ProblemBankQueryDto queryDto) {
		Page<ProblemBankItemVo> questionBankPage = questionBankService.getQuestionBankPage(queryDto,
				getCurrentUserId());
		return ResponseResult.success("成功获取题库列表", questionBankPage);
	}

	@GetMapping("/calendar")
	public ResponseResult<List<SubmissionCalendarVo>> getCalendar() {
		return ResponseResult.success("成功获取日历数据", questionBankService.getSubmissionCalendar(getCurrentUserId()));
	}
}
