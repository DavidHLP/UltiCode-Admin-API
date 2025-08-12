package com.david.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.judge.TestCaseInput;

@Mapper
public interface TestCaseInputMapper extends BaseMapper<TestCaseInput> {
	Boolean deleteByTestCaseOutputId(@Param("testCaseOutputId") Long testCaseOutputId);
	List<TestCaseInput> getByTestCaseOutputId(@Param("testCaseOutputId") Long testCaseOutputId);
}
