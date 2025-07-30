package com.david.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.solution.Solution;
import com.david.vo.SolutionCardVo;
import com.david.vo.SolutionVo;

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

	/**
	 * 分页获取指定题目的题解列表（包含用户信息）
	 * 
	 * @param page
	 *            分页对象
	 * @param problemId
	 *            题目ID
	 * @param title
	 *            题解标题（可选，用于搜索）
	 * @return 分页的题解列表
	 */
	Page<SolutionCardVo> getSolutionsByProblemIdWithPage(Page<SolutionCardVo> page, @Param("problemId") Long problemId,
			@Param("title") String title, @Param("sort") String sort);

	SolutionVo getSolutionById(@Param("Id") Long Id);

	void incrementViews(@Param("id") Long id);

	void incrementUpvotes(@Param("id") Long id);

	void incrementDownvotes(@Param("id") Long id);
}
