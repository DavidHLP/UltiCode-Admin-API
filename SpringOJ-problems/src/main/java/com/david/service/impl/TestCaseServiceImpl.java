package com.david.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.dto.TestCaseDto;
import com.david.judge.TestCase;
import com.david.mapper.TestCaseMapper;
import com.david.service.ITestCaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 测试用例服务实现类
 * </p>
 *
 * @author david
 * @since 2025-07-21
 */
@Slf4j
@Service
public class TestCaseServiceImpl extends ServiceImpl<TestCaseMapper, TestCase> implements ITestCaseService {
	public List<TestCaseDto> getTestCaseDtoById(Long id) {
		List<TestCase> testCases = this.lambdaQuery().eq(TestCase::getProblemId, id).eq(TestCase::getIsSample, true).list();
		if (testCases.isEmpty()) return null;
		List<TestCaseDto> testCaseDos = new ArrayList<>();
		for (TestCase testCase : testCases) {
			testCaseDos.add(TestCaseDto.builder().input(testCase.getInput()).output(testCase.getOutput()).build());
		}
		return testCaseDos;
	}
}
