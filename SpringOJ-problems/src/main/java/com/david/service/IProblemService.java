package com.david.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.david.dto.ProblemDto;
import com.david.judge.Problem;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author david
 * @since 2025-07-21
 */
public interface IProblemService extends IService<Problem> {
    ProblemDto getProblemDtoById(Long id);
}
