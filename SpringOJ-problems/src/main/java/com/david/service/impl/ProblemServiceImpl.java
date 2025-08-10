package com.david.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.judge.Problem;
import com.david.judge.enums.CategoryType;
import com.david.judge.enums.ProblemDifficulty;
import com.david.mapper.ProblemMapper;
import com.david.service.IProblemService;
import com.david.vo.ProblemVo;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author david
 * @since 2025-07-21
 */
@Service
public class ProblemServiceImpl extends ServiceImpl<ProblemMapper, Problem> implements IProblemService {
	public ProblemVo getProblemDtoById(Long id) {
		Problem problem = this.getById(id);
		if (problem == null)
			return null;
		ProblemVo problemVo = new ProblemVo();
		BeanUtils.copyProperties(problem, problemVo);
		return problemVo;
	}

    @Override
    public Page<Problem> pageProblems(Page<Problem> page,
                                      String keyword,
                                      ProblemDifficulty difficulty,
                                      CategoryType category,
                                      Boolean isVisible) {
        LambdaQueryWrapper<Problem> qw = Wrappers.lambdaQuery();
        if (StringUtils.hasText(keyword)) {
            qw.like(Problem::getTitle, keyword);
        }
        if (difficulty != null) {
            qw.eq(Problem::getDifficulty, difficulty);
        }
        if (category != null) {
            qw.eq(Problem::getCategory, category);
        }
        if (isVisible != null) {
            qw.eq(Problem::getIsVisible, isVisible);
        }
        qw.orderByAsc(Problem::getId);
        return this.page(page, qw);
    }
}
