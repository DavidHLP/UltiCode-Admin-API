package com.david.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.judge.Problem;
import com.david.judge.enums.CategoryType;
import com.david.judge.enums.LanguageType;
import com.david.judge.enums.ProblemDifficulty;
import com.david.mapper.ProblemMapper;
import com.david.service.IProblemService;
import com.david.utils.CodeUtils;
import com.david.utils.SolutionDto;
import com.david.vo.ProblemDetailVo;
import com.david.vo.ProblemVo;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

/**
 * 服务实现类
 *
 * @author david
 * @since 2025-07-21
 */
@Service
@RequiredArgsConstructor
public class ProblemServiceImpl extends ServiceImpl<ProblemMapper, Problem>
        implements IProblemService {
    private final ProblemMapper problemMapper;
    private final CalculationServiceImpl calculationService;
	private final CodeUtils codeUtils;

    @Override
    public Page<Problem> pageProblems(
            Page<Problem> page,
            String keyword,
            ProblemDifficulty difficulty,
            CategoryType category,
            Boolean isVisible) {
        return problemMapper.pageProblems(page, keyword, difficulty, category, isVisible);
    }

    @Override
    public Page<ProblemVo> pageProblemVos(
            Page<Problem> page,
            String keyword,
            ProblemDifficulty difficulty,
            CategoryType category) {
        Page<Problem> problemPage =
                problemMapper.pageProblems(page, keyword, difficulty, category, true);
        Page<ProblemVo> result =
                new Page<>(problemPage.getCurrent(), problemPage.getSize(), problemPage.getTotal());
        result.setRecords(
                problemPage.getRecords().stream()
                        .map(
                                problem ->
                                        ProblemVo.builder()
                                                .id(problem.getId())
                                                .category(problem.getCategory())
                                                .difficulty(problem.getDifficulty())
                                                .title(problem.getTitle())
                                                .tags(problem.getTags())
                                                .passRate(
                                                        calculationService.submissionPassRate(
                                                                problem.getId()))
                                                .build())
                        .toList());
        return result;
    }

    @Override
    public ProblemDetailVo getProblemDetailVoById(Long id) {
        Problem problem = this.getById(id);
        if (problem == null) {
            throw new RuntimeException("题目不存在");
        }
        return ProblemDetailVo.builder()
                .id(problem.getId())
                .description(problem.getDescription())
                .title(problem.getTitle())
                .difficulty(problem.getDifficulty())
                .build();
    }

	@Override
	public String getCodeTemplate(Long problemId, LanguageType language) {
		SolutionDto solutionDto  = problemMapper.selectSolutionFunctionNameAndOutputType(problemId);
		if (solutionDto == null || solutionDto.getSolutionFunctionName() == null || solutionDto.getOutputType() == null){
			throw new IllegalArgumentException("题目不存在或题目未设置函数名和返回值类型");
		}
		return codeUtils.generateSolutionClass(language, solutionDto);
	}
}
