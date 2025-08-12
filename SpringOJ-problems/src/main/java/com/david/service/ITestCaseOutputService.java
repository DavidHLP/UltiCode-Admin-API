package com.david.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.david.judge.TestCaseOutput;

import java.util.List;

public interface ITestCaseOutputService extends IService<TestCaseOutput> {
	List<TestCaseOutput> getByProblemId(Long problemId);
}
