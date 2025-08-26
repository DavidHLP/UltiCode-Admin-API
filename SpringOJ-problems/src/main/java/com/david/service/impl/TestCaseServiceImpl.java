package com.david.service.impl;

import com.david.exception.BizException;
import com.david.service.ITestCaseInputService;
import com.david.service.ITestCaseOutputService;
import com.david.testcase.TestCase;
import com.david.testcase.TestCaseOutput;
import com.david.testcase.vo.TestCaseVo;
import com.david.utils.enums.ResponseCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class TestCaseServiceImpl {
    private final ITestCaseInputService testCaseInputService;
    private final ITestCaseOutputService testCaseOutputService;

    @Transactional
    public Boolean save(TestCase testCase) {
        if (testCase == null
                || testCase.getTestCaseOutput() == null
                || testCase.getTestCaseInput() == null) {
            throw BizException.of(ResponseCode.RC400);
        }

        // 补全输出的 problemId
        if (testCase.getTestCaseOutput().getProblemId() == null) {
            testCase.getTestCaseOutput().setProblemId(testCase.getProblemId());
        }

        if (!testCaseOutputService.save(testCase.getTestCaseOutput())) {
            throw BizException.of(ResponseCode.RC500);
        }
        Long outputId = testCase.getTestCaseOutput().getId();
        testCase.getTestCaseInput().forEach(t -> t.setTestCaseOutputId(outputId));
        if (!testCaseInputService.saveBatch(testCase.getTestCaseInput())) {
            throw BizException.of(ResponseCode.RC500);
        }
        return true;
    }

    @Transactional
    public Boolean update(TestCase testCase) {
        testCase.getTestCaseOutput()
                .setProblemId(
                        testCaseOutputService
                                .getById(testCase.getTestCaseOutput().getId())
                                .getProblemId());
        if (!testCaseOutputService.updateById(testCase.getTestCaseOutput()))
            throw BizException.of(ResponseCode.RC500);
        if (!testCaseInputService.updateBatchById(testCase.getTestCaseInput()))
            throw BizException.of(ResponseCode.RC500);
        return true;
    }

    @Transactional
    public Boolean delete(Long id) {
        if (!testCaseOutputService.removeById(id, testCaseOutputService.getById(id).getProblemId()))
            throw BizException.of(ResponseCode.RC500);
        if (testCaseInputService.deleteByTestCaseOutputId(id))
            throw BizException.of(ResponseCode.RC500);
        return true;
    }

    public List<TestCase> getList(Long problemId) {
        List<TestCaseOutput> testCaseInputs = testCaseOutputService.getByProblemId(problemId);
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
        List<TestCaseOutput> testCaseInputs =
                testCaseOutputService.getByProblemId(problemId).stream()
                        .filter(TestCaseOutput::getIsSample)
                        .toList();
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
