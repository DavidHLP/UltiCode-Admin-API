package com.david.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.exception.BizException;
import com.david.mapper.TestCaseInputMapper;
import com.david.redis.commons.annotation.RedisCacheable;
import com.david.redis.commons.annotation.RedisEvict;
import com.david.service.ITestCaseInputService;
import com.david.testcase.TestCaseInput;
import com.david.utils.enums.ResponseCode;

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
    @RedisEvict(
            keys = {"'testCaseInput:selectByTestCaseOutputId:' + #testCaseOutputId"},
            keyPrefix = "springoj:cache:")
    public Boolean deleteByTestCaseOutputId(Long testCaseOutputId) {
        validateRequiredId("测试用例输出ID", testCaseOutputId);
        Boolean deleted = testCaseInputMapper.deleteByTestCaseOutputId(testCaseOutputId);
        // 输出阶段：保证返回值非空
        return Boolean.TRUE.equals(deleted);
    }

    @Override
    @RedisCacheable(
            key = "'testCaseInput:selectByTestCaseOutputId:' + #testCaseOutputId",
            keyPrefix = "springoj:cache:",
            ttl = 1800,
            type = List.class)
    public List<TestCaseInput> selectByTestCaseOutputId(Long testCaseOutputId) {
        validateRequiredId("测试用例输出ID", testCaseOutputId);
        List<TestCaseInput> list = testCaseInputMapper.getByTestCaseOutputId(testCaseOutputId);
        return list == null ? Collections.emptyList() : list;
    }

    @Override
    @Transactional
    @RedisEvict(
            keys = {"'testCaseInput:selectByTestCaseOutputId:*'"},
            keyPrefix = "springoj:cache:")
    public boolean saveBatch(Collection<TestCaseInput> entityList) {
        return !testCaseInputMapper.insert(entityList, entityList.size()).isEmpty();
    }

    @Override
    @Transactional
    @RedisEvict(
            keys = {"'testCaseInput:selectByTestCaseOutputId:*'"},
            keyPrefix = "springoj:cache:")
    public boolean updateBatchById(Collection<TestCaseInput> entityList) {
        return !testCaseInputMapper.updateById(entityList, entityList.size()).isEmpty();
    }

    private void validateRequiredId(String fieldName, Long id) {
        if (id == null) {
            throw BizException.of(ResponseCode.RC400.getCode(), fieldName + "不能为空");
        }
        if (id < 1) {
            throw BizException.of(ResponseCode.RC400.getCode(), fieldName + "必须>=1，当前值：" + id);
        }
    }
}
