package com.david.service.impl;

import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.dto.ProblemDto;
import com.david.judge.CodeTemplate;
import com.david.judge.Problem;
import com.david.mapper.ProblemMapper;
import com.david.service.IProblemService;

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
	public ProblemDto getProblemDtoById(Long id) {
		Problem problem = this.getById(id);
		if (problem == null)
			return null;
		ProblemDto problemDto = new ProblemDto();
		BeanUtils.copyProperties(problem, problemDto);
		problemDto.setInitialCode(Map.of("java", CodeTemplate.JAVA_CODE_TEMPLATE));
		return problemDto;
	}
}
