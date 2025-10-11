package com.david.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.mapper.TestCaseInputMapper;
import com.david.service.ITestCaseInputService;
import com.david.testcase.TestCaseInput;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class TestCaseInputServiceImpl extends ServiceImpl<TestCaseInputMapper, TestCaseInput>
        implements ITestCaseInputService {
    private final TestCaseInputMapper testCaseInputMapper;

    @Override
    @Transactional
    public Boolean deleteByTestCaseOutputId(Long testCaseOutputId) {
        Boolean deleted = testCaseInputMapper.deleteByTestCaseOutputId(testCaseOutputId);
        // 输出阶段：保证返回值非空
        return Boolean.TRUE.equals(deleted);
    }

    @Override
    public List<TestCaseInput> selectByTestCaseOutputId(Long testCaseOutputId) {
        List<TestCaseInput> list = testCaseInputMapper.getByTestCaseOutputId(testCaseOutputId);
        return list == null ? Collections.emptyList() : list;
    }

    @Override
    @Transactional
    public boolean saveBatch(Collection<TestCaseInput> entityList) {
        return !testCaseInputMapper.insert(entityList, entityList.size()).isEmpty();
    }

    @Override
    @Transactional
    public boolean updateBatchById(Collection<TestCaseInput> entityList) {
        return !testCaseInputMapper.updateById(entityList, entityList.size()).isEmpty();
    }
}
