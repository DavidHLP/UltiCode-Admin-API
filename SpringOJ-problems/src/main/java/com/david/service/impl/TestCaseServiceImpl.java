package com.david.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.judge.TestCase;
import com.david.mapper.TestCaseMapper;
import com.david.service.ITestCaseService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  测试用例服务实现类
 * </p>
 *
 * @author david
 * @since 2025-07-21
 */
@Service
public class TestCaseServiceImpl extends ServiceImpl<TestCaseMapper, TestCase> implements ITestCaseService {

}
