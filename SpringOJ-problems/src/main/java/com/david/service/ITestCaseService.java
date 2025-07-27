package com.david.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.david.dto.TestCaseDto;
import com.david.judge.TestCase;

import java.util.List;

/**
 * <p>
 *  测试用例服务类
 * </p>
 *
 * @author david
 * @since 2025-07-21
 */
public interface ITestCaseService extends IService<TestCase> {
    List<TestCaseDto> getTestCaseDtoById(Long id);
}
