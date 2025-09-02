package com.david.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.commons.redis.cache.annotation.RedisCacheable;
import com.david.commons.redis.cache.annotation.RedisEvict;
import com.david.mapper.TestCaseOutputMapper;
import com.david.service.ITestCaseOutputService;
import com.david.testcase.TestCaseOutput;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class TestCaseOutputServiceImpl extends ServiceImpl<TestCaseOutputMapper, TestCaseOutput>
        implements ITestCaseOutputService {
    private final TestCaseOutputMapper testCaseOutputMapper;

    @Override
    @RedisCacheable(
            key = "'testCaseOutput:getByProblemId:' + #problemId",
            keyPrefix = "springoj:cache:",
            ttl = 1800,
            type = List.class)
    public List<TestCaseOutput> getByProblemId(Long problemId) {
        return testCaseOutputMapper.selectByProblemId(problemId);
    }

    @Override
    @RedisCacheable(
            key = "'testCaseOutput:getById:' + #id",
            keyPrefix = "springoj:cache:",
            ttl = 1800,
            type = TestCaseOutput.class)
    public TestCaseOutput getById(Serializable id) {
        return getBaseMapper().selectById(id);
    }

    @Override
    @Transactional
    @RedisEvict(
            keys = {"'testCaseOutput:getByProblemId:' + #entity.getProblemId()"},
            keyPrefix = "springoj:cache:")
    public boolean save(TestCaseOutput entity) {
        return testCaseOutputMapper.insert(entity) > 0;
    }

    @Override
    @Transactional
    @RedisEvict(
            keys = {
                "'testCaseOutput:getById:' + #entity.getId()",
                "'testCaseOutput:getByProblemId:' + #entity.getProblemId()"
            },
            keyPrefix = "springoj:cache:")
    public boolean updateById(TestCaseOutput entity) {
        return testCaseOutputMapper.updateById(entity) > 0;
    }

    @Override
    @Transactional
    @RedisEvict(
            keys = {
                "'testCaseOutput:getById:' + #id",
                "'testCaseOutput:getByProblemId:' + #problemId"
            },
            keyPrefix = "springoj:cache:")
    public boolean removeById(Long id, Long problemId) {
        return testCaseOutputMapper.deleteById(id) > 0;
    }
}
