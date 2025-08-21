package com.david.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.mapper.TestCaseOutputMapper;
import com.david.service.ITestCaseOutputService;
import com.david.testcase.TestCaseOutput;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class TestCaseOutputServiceImpl extends ServiceImpl<TestCaseOutputMapper, TestCaseOutput> implements ITestCaseOutputService {
	private final TestCaseOutputMapper testCaseOutputMapper;
	@Override
	public List<TestCaseOutput> getByProblemId(Long problemId) {
		return testCaseOutputMapper.selectByProblemId(problemId);
	}
}
