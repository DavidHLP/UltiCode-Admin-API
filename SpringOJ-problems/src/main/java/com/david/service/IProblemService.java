package com.david.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.david.judge.Problem;
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
    ProblemVo getProblemDtoById(Long id);
}
