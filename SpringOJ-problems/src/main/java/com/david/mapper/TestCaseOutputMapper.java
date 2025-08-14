package com.david.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.testcase.TestCaseOutput;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TestCaseOutputMapper extends BaseMapper<TestCaseOutput> {
	List<TestCaseOutput> selectByProblemId(Long problemId);
}
