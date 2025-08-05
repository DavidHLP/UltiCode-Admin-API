package com.david.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.mapper.SolutionMapper;
import com.david.mapper.UserContentViewMapper;
import com.david.service.ISolutionService;
import com.david.solution.Solution;
import com.david.solution.UserContentView;
import com.david.utils.MarkdownUtils;
import com.david.vo.SolutionCardVo;
import com.david.vo.SolutionVo;

import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 题解服务实现类
 * </p>
 *
 * @author david
 * @since 2025-07-28
 */
@Service
@RequiredArgsConstructor
public class SolutionServiceImpl extends ServiceImpl<SolutionMapper, Solution> implements ISolutionService {
	private final UserContentViewMapper userContentViewMapper;
	private final SolutionMapper solutionMapper;

	@Override
	public Page<SolutionCardVo> getSolutionsByProblemIdWithPage(Page<SolutionCardVo> page, Long problemId, String title,
			String sort) {
		Page<SolutionCardVo> res = solutionMapper.getSolutionsByProblemIdWithPage(page, problemId, title, sort);
		for (SolutionCardVo solutionCardVo : res.getRecords()) {
			solutionCardVo.setProblem(MarkdownUtils.toPlainText(solutionCardVo.getProblem()).substring(0, 50) + "...");
		}
		return res;
	}

	@Override
	@Transactional
	public SolutionVo getSolutionById(Long id, Long userId) {
		SolutionVo solution = solutionMapper.getSolutionById(id);
		if (userContentViewMapper.getUserContentViews(userId, id) == null) {
			userContentViewMapper.insert(UserContentView.builder().userId(userId).contentId(id).build());
			solutionMapper.incrementViews(id);
		}
		return solution;
	}

	@Override
	@Transactional
	public void voteSolution(Long solutionId, String type) {
		if ("up".equals(type)) {
			solutionMapper.incrementUpvotes(solutionId);
		} else if ("down".equals(type)) {
			solutionMapper.incrementDownvotes(solutionId);
		}else throw new RuntimeException("投票转态异常");
	}

}
