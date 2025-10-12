package com.david.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.testcase.TestCaseOutput;
import com.david.testcase.dto.TestCaseOutputDto;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TestCaseOutputMapper extends BaseMapper<TestCaseOutput> {
    List<TestCaseOutput> selectByProblemId(Long problemId);

    TestCaseOutputDto selectTestCaseOutputDtoFirstByProblemId(@Param("problemId") Long problemId);

	Page<TestCaseOutput> getPage(Long problemId, Page<TestCaseOutput> page);
}
