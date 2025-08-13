package com.david.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.david.solution.Solution;

import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 题解Mapper 接口
 * </p>
 *
 * @author david
 * @since 2025-07-28
 */
@Mapper
public interface SolutionMapper extends BaseMapper<Solution> {
}
