package com.david.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.david.judge.Problem;
import com.david.judge.enums.CategoryType;
import com.david.judge.enums.LanguageType;
import com.david.judge.enums.ProblemDifficulty;
import com.david.vo.ProblemDetailVo;
import com.david.vo.ProblemVo;

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

	Page<ProblemVo> pageProblemVos(Page<Problem> page,
	                               String keyword,
	                               ProblemDifficulty difficulty,
	                               CategoryType category);

	ProblemDetailVo getProblemDetailVoById(Long id);

	String getCodeTemplate(Long problemId, LanguageType language);
}
