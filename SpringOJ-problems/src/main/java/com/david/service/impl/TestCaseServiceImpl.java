package com.david.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.judge.TestCase;
import com.david.mapper.TestCaseMapper;
import com.david.service.ITestCaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  测试用例服务实现类
 * </p>
 *
 * @author david
 * @since 2025-07-21
 */
@Slf4j
@Service
public class TestCaseServiceImpl extends ServiceImpl<TestCaseMapper, TestCase> implements ITestCaseService {

    @Override
    public List<TestCase> getTestCasesByProblemId(Long problemId) {
        QueryWrapper<TestCase> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("problem_id", problemId);
        queryWrapper.orderByAsc("id");
        return list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TestCase> batchImportTestCases(Long problemId, List<TestCaseData> testCaseDataList) {
        List<TestCase> createdTestCases = new ArrayList<>();
        
        for (TestCaseData data : testCaseDataList) {
            TestCase testCase = new TestCase();
            testCase.setProblemId(problemId);
            testCase.setInput(data.getInput());
            testCase.setOutput(data.getOutput());
            testCase.setScore(data.getScore() != null ? data.getScore() : 10);
            testCase.setSample(data.getIsSample() != null ? data.getIsSample() : false);
            
            save(testCase);
            createdTestCases.add(testCase);
        }
        
        log.info("批量导入测试用例成功: problemId={}, count={}", problemId, createdTestCases.size());
        return createdTestCases;
    }
}
