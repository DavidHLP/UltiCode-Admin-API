package com.david.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.judge.TestCaseOutput;
import com.david.mapper.TestCaseOutputMapper;
import com.david.service.ITestCaseOutputService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ITestCaseOutputServiceImpl extends ServiceImpl<TestCaseOutputMapper, TestCaseOutput> implements ITestCaseOutputService {
	private final TestCaseOutputMapper testCaseOutputMapper;
	@Override
	public List<TestCaseOutput> getByProblemId(Long problemId) {
		return testCaseOutputMapper.selectByProblemId(problemId);
	}
}
