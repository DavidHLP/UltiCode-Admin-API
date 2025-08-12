package com.david.service.impl;

import com.david.judge.TestCase;
import com.david.judge.TestCaseOutput;
import com.david.service.ITestCaseInputService;
import com.david.service.ITestCaseOutputService;
import com.david.vo.TestCaseVo;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestCaseServiceImpl {
    private final ITestCaseInputService testCaseInputService;
    private final ITestCaseOutputService ITestCaseOutputService;

    @Transactional
    public Boolean save(TestCase testCase) {
        if (testCase == null
                || testCase.getTestCaseOutput() == null
                || testCase.getTestCaseInput() == null) {
            throw new IllegalArgumentException("测试用例参数不完整");
        }

        // 补全输出的 problemId
        if (testCase.getTestCaseOutput().getProblemId() == null) {
            testCase.getTestCaseOutput().setProblemId(testCase.getProblemId());
        }

        if (!ITestCaseOutputService.save(testCase.getTestCaseOutput())) {
            throw new RuntimeException("保存输出数据失败");
        }
        Long outputId = testCase.getTestCaseOutput().getId();
        testCase.getTestCaseInput().forEach(t -> t.setTestCaseOutputId(outputId));
        if (!testCaseInputService.saveBatch(testCase.getTestCaseInput())) {
            throw new RuntimeException("保存输入数据失败");
        }
        return true;
    }

    @Transactional
    public Boolean update(TestCase testCase) {
        if (!ITestCaseOutputService.updateById(testCase.getTestCaseOutput()))
            throw new RuntimeException("更新输出数据失败");
        if (!testCaseInputService.updateBatchById(testCase.getTestCaseInput()))
            throw new RuntimeException("更新输入数据失败");
        return true;
    }

    @Transactional
    public Boolean delete(Long id) {
        if (!ITestCaseOutputService.removeById(id)) throw new RuntimeException("删除输出数据失败");
        testCaseInputService.deleteByTestCaseOutputId(id);
        return true;
    }

    public List<TestCase> getList(Long problemId) {
        List<TestCaseOutput> testCaseInputs = ITestCaseOutputService.getByProblemId(problemId);
        List<TestCase> testCases = new ArrayList<>();
        for (TestCaseOutput testCaseOutput : testCaseInputs) {
            testCases.add(
                    TestCase.builder()
                            .id(testCaseOutput.getId())
                            .problemId(problemId)
                            .testCaseOutput(testCaseOutput)
                            .testCaseInput(
                                    testCaseInputService.selectByTestCaseOutputId(
                                            testCaseOutput.getId()))
                            .build());
        }
        return testCases;
    }

    public List<TestCaseVo> getTestCaseVoByProblemId(Long problemId) {
        List<TestCaseOutput> testCaseInputs = ITestCaseOutputService.getByProblemId(problemId);
        List<TestCaseVo> testCaseVos = new ArrayList<>();
        for (TestCaseOutput testCaseOutput : testCaseInputs) {
            testCaseVos.add(
                    TestCaseVo.builder()
                            .id(testCaseOutput.getId())
                            .problemId(problemId)
                            .testCaseOutput(testCaseOutput)
                            .testCaseInputs(
                                    testCaseInputService.selectByTestCaseOutputId(
                                            testCaseOutput.getId()))
                            .build());
        }
        return testCaseVos;
    }
}
