package com.david.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.testcase.TestCaseInput;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TestCaseInputMapper extends BaseMapper<TestCaseInput> {
	Boolean deleteByTestCaseOutputId(@Param("testCaseOutputId") Long testCaseOutputId);
	List<TestCaseInput> getByTestCaseOutputId(@Param("testCaseOutputId") Long testCaseOutputId);
}
