package com.david.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.david.solution.Solution;
import com.david.vo.SolutionCardVo;
import com.david.vo.SolutionDetailVo;

/**
 * <p>
 * 题解服务类
 * </p>
 *
 * @author david
 * @since 2025-07-28
 */
public interface ISolutionService extends IService<Solution> {
	SolutionDetailVo getSolutionDetailVoBy(Long solutionId);

	Page<SolutionCardVo> pageSolutionCardVos(Page<SolutionCardVo> p, Long problemId, String keyword);
}
