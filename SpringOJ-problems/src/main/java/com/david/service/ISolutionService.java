package com.david.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.david.solution.Solution;
import com.david.solution.enums.SolutionStatus;
import com.david.solution.vo.SolutionCardVo;
import com.david.solution.vo.SolutionDetailVo;
import com.david.solution.vo.SolutionManagementCardVo;

/**
 * <p>
 * 题解服务类
 * </p>
 *
 * @author david
 * @since 2025-07-28
 */
public interface ISolutionService extends IService<Solution> {
	SolutionDetailVo getSolutionDetailVoBy(Long solutionId , Long userId);

	Page<SolutionCardVo> pageSolutionCardVos(Page<SolutionCardVo> p, Long problemId, String keyword);
	Page<SolutionCardVo> pageSolutionCardVosByUserId(Page<SolutionCardVo> p, Long userId);

	Page<SolutionManagementCardVo> pageSolutionManagementCardVo(Page<SolutionManagementCardVo> page, Long problemId, String keyword, Long userId, SolutionStatus status);
}
