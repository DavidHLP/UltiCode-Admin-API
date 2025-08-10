package com.david.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.david.judge.Problem;
import com.david.vo.ProblemVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.judge.enums.ProblemDifficulty;
import com.david.judge.enums.CategoryType;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author david
 * @since 2025-07-21
 */
public interface IProblemService extends IService<Problem> {
    ProblemVo getProblemDtoById(Long id);

    Page<Problem> pageProblems(Page<Problem> page,
                               String keyword,
                               ProblemDifficulty difficulty,
                               CategoryType category,
                               Boolean isVisible);
}
