package com.david.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.judge.TestCase;
import com.david.mapper.TestCaseMapper;
import com.david.service.ITestCaseService;
import com.david.vo.TestCaseVo;

import lombok.extern.slf4j.Slf4j;

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
	public List<TestCaseVo> getTestCaseDtoById(Long id) {
		List<TestCase> testCases = this.lambdaQuery().eq(TestCase::getProblemId, id).eq(TestCase::getIsSample, true)
				.list();
		if (testCases.isEmpty())
			return null;
		List<TestCaseVo> testCaseDos = new ArrayList<>();
		for (TestCase testCase : testCases) {
			testCaseDos.add(TestCaseVo.builder().input(testCase.getInput()).output(testCase.getOutput()).build());
		}
		return testCaseDos;
	}
}
