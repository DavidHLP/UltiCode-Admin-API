package com.david.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.enums.CategoryType;
import com.david.enums.LanguageType;
import com.david.mapper.ProblemMapper;
import com.david.mapper.TestCaseInputMapper;
import com.david.mapper.TestCaseOutputMapper;
import com.david.problem.Problem;
import com.david.problem.enums.ProblemDifficulty;
import com.david.problem.vo.ProblemCardVo;
import com.david.problem.vo.ProblemDetailVo;
import com.david.service.IProblemService;
import com.david.submission.dto.CompareDescription;
import com.david.testcase.dto.TestCaseInputDto;
import com.david.testcase.dto.TestCaseOutputDto;
import com.david.utils.CodeUtils;
import com.david.utils.SolutionDto;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

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
    private final TestCaseInputMapper testCaseInputMapper;
    private final TestCaseOutputMapper testCaseOutputMapper;
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
    public Page<ProblemCardVo> pageProblemVos(
            Page<Problem> page,
            String keyword,
            ProblemDifficulty difficulty,
            CategoryType category) {
        Page<Problem> problemPage =
                problemMapper.pageProblems(page, keyword, difficulty, category, true);
        Page<ProblemCardVo> result =
                new Page<>(problemPage.getCurrent(), problemPage.getSize(), problemPage.getTotal());
        result.setRecords(
                problemPage.getRecords().stream()
                        .map(
                                problem ->
                                        ProblemCardVo.builder()
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
        String solutionFunctionName = problemMapper.selectSolutionFunctionName(problemId);
        if (solutionFunctionName == null || solutionFunctionName.isEmpty()) {
            throw new IllegalArgumentException("问题不存在");
        }
        TestCaseOutputDto testCaseOutput =
                testCaseOutputMapper.selectTestCaseOutputDtoFirstByProblemId(problemId);
        if (testCaseOutput == null) {
            throw new IllegalArgumentException("问题不存在");
        }
        List<TestCaseInputDto> testCaseInputDtoList =
                testCaseInputMapper.getTestCaseInputDtoByTestCaseId(testCaseOutput.getId());
        if (testCaseInputDtoList == null || testCaseInputDtoList.isEmpty()) {
            throw new IllegalArgumentException("问题不存在");
        }
        SolutionDto solutionDto =
                SolutionDto.builder()
                        .solutionFunctionName(solutionFunctionName)
                        .testCaseOutput(testCaseOutput)
                        .testCaseInputs(testCaseInputDtoList)
                        .build();

        return codeUtils.generateSolutionClass(language, solutionDto);
    }

	@Override
	public CompareDescription getCompareDescription(Long id) {
		CompareDescription compareDescription = problemMapper.selectCompareDescription(id);
		if (compareDescription == null){
			throw new IllegalArgumentException("获取题目信息失败");
		}
		return compareDescription;
	}
}
