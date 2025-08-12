package com.david.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.judge.TestCaseOutput;

@Mapper
public interface TestCaseOutputMapper extends BaseMapper<TestCaseOutput> {
	List<TestCaseOutput> selectByProblemId(Long problemId);
}
