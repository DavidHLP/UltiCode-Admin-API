package com.david.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.david.testcase.TestCaseInput;

import java.util.List;

public interface ITestCaseInputService extends IService<TestCaseInput> {
	Boolean deleteByTestCaseOutputId(Long testCaseOutputId);

	List<TestCaseInput> selectByTestCaseOutputId(Long testCaseOutputId);
}