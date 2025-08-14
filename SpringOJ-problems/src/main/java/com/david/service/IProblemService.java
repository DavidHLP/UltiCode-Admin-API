package com.david.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.david.enums.CategoryType;
import com.david.enums.LanguageType;
import com.david.problem.Problem;
import com.david.problem.enums.ProblemDifficulty;
import com.david.problem.vo.ProblemCardVo;
import com.david.problem.vo.ProblemDetailVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author david
 * @since 2025-07-21
 */
public interface IProblemService extends IService<Problem> {

    Page<Problem> pageProblems(Page<Problem> page,
                               String keyword,
                               ProblemDifficulty difficulty,
                               CategoryType category,
                               Boolean isVisible);

	Page<ProblemCardVo> pageProblemVos(Page<Problem> page,
	                                   String keyword,
	                                   ProblemDifficulty difficulty,
	                                   CategoryType category);

	ProblemDetailVo getProblemDetailVoById(Long id);

	String getCodeTemplate(Long problemId, LanguageType language);
}
