package com.david.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.david.testcase.TestCaseInput;

import java.util.List;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Validated
public interface ITestCaseInputService extends IService<TestCaseInput> {
    Boolean deleteByTestCaseOutputId(@NotNull(message = "测试用例输出ID不能为空") @Min(value = 1, message = "测试用例输出ID必须>=1") Long testCaseOutputId);

    List<TestCaseInput> selectByTestCaseOutputId(@NotNull(message = "测试用例输出ID不能为空") @Min(value = 1, message = "测试用例输出ID必须>=1") Long testCaseOutputId);
}