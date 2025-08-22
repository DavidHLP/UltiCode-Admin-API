package com.david.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.mapper.TestCaseInputMapper;
import com.david.service.ITestCaseInputService;
import com.david.testcase.TestCaseInput;
import com.david.exception.BizException;
import com.david.utils.enums.ResponseCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Validated
public class TestCaseInputServiceImpl extends ServiceImpl<TestCaseInputMapper, TestCaseInput> implements ITestCaseInputService {
	private final TestCaseInputMapper testCaseInputMapper;
	@Override
	public Boolean deleteByTestCaseOutputId(Long testCaseOutputId) {
		validateRequiredId("测试用例输出ID", testCaseOutputId);
		Boolean deleted = testCaseInputMapper.deleteByTestCaseOutputId(testCaseOutputId);
		// 输出阶段：保证返回值非空
		return Boolean.TRUE.equals(deleted);
	}

	@Override
	public List<TestCaseInput> selectByTestCaseOutputId(Long testCaseOutputId) {
		validateRequiredId("测试用例输出ID", testCaseOutputId);
		List<TestCaseInput> list = testCaseInputMapper.getByTestCaseOutputId(testCaseOutputId);
		return list == null ? Collections.emptyList() : list;
	}

	// --------------------------- 内部校验工具方法 ---------------------------
	private void validateRequiredId(String fieldName, Long id) {
		if (id == null) {
			throw BizException.of(ResponseCode.RC400.getCode(), fieldName + "不能为空");
		}
		if (id < 1) {
			throw BizException.of(ResponseCode.RC400.getCode(), fieldName + "必须>=1，当前值：" + id);
		}
	}
}
