package com.david.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.mapper.SolutionMapper;
import com.david.mapper.UserContentViewMapper;
import com.david.service.ISolutionService;
import com.david.solution.Solution;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

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
}
