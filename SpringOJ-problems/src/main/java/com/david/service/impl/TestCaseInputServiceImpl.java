package com.david.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.judge.TestCaseInput;
import com.david.mapper.TestCaseInputMapper;
import com.david.service.ITestCaseInputService;

@Service
public class TestCaseInputServiceImpl extends ServiceImpl<TestCaseInputMapper, TestCaseInput> implements ITestCaseInputService {
}
