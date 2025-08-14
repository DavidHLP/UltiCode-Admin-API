package com.david.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.enums.CategoryType;
import com.david.problem.Problem;
import com.david.problem.enums.ProblemDifficulty;
import com.david.utils.SolutionDto;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author david
 * @since 2025-07-21
 */
@Mapper
public interface ProblemMapper extends BaseMapper<Problem> {
	SolutionDto selectSolutionFunctionNameAndOutputType(Long problemId);

	Page<Problem> pageProblems(
        Page<Problem> page,
        @Param("keyword") String keyword,
        @Param("difficulty") ProblemDifficulty difficulty,
        @Param("category") CategoryType category,
        @Param("isVisible") Boolean isVisible
    );
}
