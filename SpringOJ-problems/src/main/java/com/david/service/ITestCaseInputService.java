package com.david.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.david.testcase.TestCaseInput;

import java.util.List;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Validated
public interface ITestCaseInputService extends IService<TestCaseInput> {
    Boolean deleteByTestCaseOutputId(@NotNull @Min(1) Long testCaseOutputId);

    List<TestCaseInput> selectByTestCaseOutputId(@NotNull @Min(1) Long testCaseOutputId);
}