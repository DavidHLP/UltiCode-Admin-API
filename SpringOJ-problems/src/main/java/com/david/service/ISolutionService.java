package com.david.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.david.solution.Solution;
import com.david.vo.SolutionCardVo;
import com.david.vo.SolutionVo;

/**
 * <p>
 * 题解服务类
 * </p>
 *
 * @author david
 * @since 2025-07-28
 */
public interface ISolutionService extends IService<Solution> {

	/**
	 * 分页获取指定题目的题解列表
	 * 
	 * @param page
	 *            分页对象
	 * @param problemId
	 *            题目ID
	 * @param title
	 *            题解标题（可选，用于搜索）
	 * @return 分页的题解列表
	 */
	Page<SolutionCardVo> getSolutionsByProblemIdWithPage(Page<SolutionCardVo> page, Long problemId, String title, String sort);

	SolutionVo getSolutionById(Long Id , Long userId);

	void voteSolution(Long solutionId, String type);

}
