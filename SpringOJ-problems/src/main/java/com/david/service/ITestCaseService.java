package com.david.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.david.judge.TestCase;
import com.david.vo.TestCaseVo;

/**
 * <p>
 *  测试用例服务类
 * </p>
 *
 * @author david
 * @since 2025-07-21
 */
public interface ITestCaseService extends IService<TestCase> {
    List<TestCaseVo> getTestCaseDtoById(Long id);
}
