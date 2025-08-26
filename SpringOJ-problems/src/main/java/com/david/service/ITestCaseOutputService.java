package com.david.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.david.testcase.TestCaseOutput;

import java.util.List;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Validated
public interface ITestCaseOutputService extends IService<TestCaseOutput> {
    List<TestCaseOutput> getByProblemId(@NotNull @Min(1) Long problemId);
    boolean removeById(Long id , Long problemId);
}
