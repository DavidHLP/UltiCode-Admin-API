package com.david.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.judge.TestCaseInput;
import com.david.mapper.TestCaseInputMapper;
import com.david.service.ITestCaseInputService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ITestCaseInputServiceImpl extends ServiceImpl<TestCaseInputMapper, TestCaseInput> implements ITestCaseInputService {
	private final TestCaseInputMapper testCaseInputMapper;
	@Override
	public Boolean deleteByTestCaseOutputId(Long testCaseOutputId) {
		return testCaseInputMapper.deleteByTestCaseOutputId(testCaseOutputId);
	}

	@Override
	public List<TestCaseInput> selectByTestCaseOutputId(Long testCaseOutputId) {
		return testCaseInputMapper.getByTestCaseOutputId(testCaseOutputId);
	}
}
