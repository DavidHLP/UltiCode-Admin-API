package com.david.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.david.commons.redis.cache.annotation.RedisCacheable;
import com.david.commons.redis.cache.annotation.RedisEvict;
import com.david.enums.CategoryType;
import com.david.enums.LanguageType;
import com.david.exception.BizException;
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
import com.david.utils.enums.ResponseCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;

/**
 * 服务实现类
 *
 * @author david
 * @since 2025-07-21
 */
@Service
@RequiredArgsConstructor
@Validated
public class ProblemServiceImpl extends ServiceImpl<ProblemMapper, Problem>
        implements IProblemService {
    private final ProblemMapper problemMapper;
    private final CalculationServiceImpl calculationService;
    private final TestCaseInputMapper testCaseInputMapper;
    private final TestCaseOutputMapper testCaseOutputMapper;
    private final CodeUtils codeUtils;

    @Override
    @RedisCacheable(
            key =
                    "'problem:pageProblems:' + #page.current + ':' + #page.size  + ':' + (#difficulty != null ? #difficulty : '') + ':' + (#sort != null ? #sort : '')",
            keyPrefix = "springoj:cache:",
            unless =
                    "#hasText(#keyword) || #page.current > 3 || #category != null || #isVisible != null",
            ttl = 1800, // 30分钟缓存
            type = Page.class)
    public Page<Problem> pageProblems(
            Page<Problem> page,
            String keyword,
            ProblemDifficulty difficulty,
            CategoryType category,
            Boolean isVisible,
            String sort) {
        if (page.getCurrent() < 1 || page.getSize() < 1) {
            throw BizException.of(
                    ResponseCode.RC400.getCode(),
                    "分页参数不合法：current和size必须≥1，当前current="
                            + page.getCurrent()
                            + ", size="
                            + page.getSize());
        }
        return problemMapper.pageProblems(page, keyword, difficulty, category, isVisible, sort);
    }

    @Override
    @RedisCacheable(
            key =
                    "'problem:pageProblemVos:' + #page.current + ':' + #page.size  + ':' + (#difficulty != null ? #difficulty : '') + ':' + (#sort != null ? #sort : '')",
            keyPrefix = "springoj:cache:",
            condition = "!#hasText(#keyword) && #page.current <= 3 && #category == null",
            ttl = 1800, // 30分钟缓存
            type = Page.class)
    public Page<ProblemCardVo> pageProblemVos(
            Page<Problem> page,
            String keyword,
            ProblemDifficulty difficulty,
            CategoryType category,
            String sort) {
        if (page.getCurrent() < 1 || page.getSize() < 1) {
            throw BizException.of(
                    ResponseCode.RC400.getCode(),
                    "分页参数不合法：current和size必须≥1，当前current="
                            + page.getCurrent()
                            + ", size="
                            + page.getSize());
        }
        Page<Problem> problemPage =
                problemMapper.pageProblems(page, keyword, difficulty, category, true, sort);
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
    @RedisCacheable(
            key = "'problem:getProblemDetailVoById:' + #id",
            keyPrefix = "springoj:cache:",
            ttl = 1800, // 30分钟缓存
            type = ProblemDetailVo.class)
    public ProblemDetailVo getProblemDetailVoById(Long id) {
        Problem problem = this.getById(id);
        if (problem == null) {
            throw BizException.of(ResponseCode.RC404.getCode(), "题目不存在，id=" + id);
        }
        return ProblemDetailVo.builder()
                .id(problem.getId())
                .description(problem.getDescription())
                .title(problem.getTitle())
                .difficulty(problem.getDifficulty())
                .build();
    }

    @Override
    @RedisCacheable(
            key = "'problem:getCodeTemplate:' + #problemId + ':' + #language",
            keyPrefix = "springoj:cache:",
            ttl = 1800, // 30分钟缓存
            type = String.class)
    public String getCodeTemplate(Long problemId, LanguageType language) {
        String solutionFunctionName = problemMapper.selectSolutionFunctionName(problemId);
        if (solutionFunctionName == null || solutionFunctionName.isEmpty()) {
            throw BizException.of(
                    ResponseCode.RC404.getCode(), "未找到题目或函数名为空，problemId=" + problemId);
        }
        TestCaseOutputDto testCaseOutput =
                testCaseOutputMapper.selectTestCaseOutputDtoFirstByProblemId(problemId);
        if (testCaseOutput == null) {
            throw BizException.of(ResponseCode.RC404.getCode(), "未配置输出用例，problemId=" + problemId);
        }
        List<TestCaseInputDto> testCaseInputDtoList =
                testCaseInputMapper.getTestCaseInputDtoByTestCaseId(testCaseOutput.getId());
        if (testCaseInputDtoList == null || testCaseInputDtoList.isEmpty()) {
            throw BizException.of(
                    ResponseCode.RC404.getCode(),
                    "未配置输入用例，testCaseOutputId=" + testCaseOutput.getId());
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
    @RedisCacheable(
            key = "'problem:getCompareDescription:' + #id",
            keyPrefix = "springoj:cache:",
            ttl = 1800, // 30分钟缓存
            type = CompareDescription.class)
    public CompareDescription getCompareDescription(Long id) {
        CompareDescription compareDescription = problemMapper.selectCompareDescription(id);
        if (compareDescription == null) {
            throw BizException.of(ResponseCode.RC404.getCode(), "未找到题目比对描述，problemId=" + id);
        }
        return compareDescription;
    }

    @Override
    @Transactional
    @RedisEvict(
            keys = {
                "'problem:pageProblems:'",
                "'problem:pageProblemVos:'",
                "'problem:getCodeTemplate:' + #entity.id + ':'",
                "'problem:getCompareDescription:' + #entity.id",
                "'problem:getById:' + #entity.id"
            },
            keyPrefix = "springoj:cache:")
    public boolean save(Problem entity) {
        return problemMapper.insert(entity) > 0;
    }

    @Override
    @Transactional
    @RedisEvict(
            keys = {
                "'problem:pageProblems:'",
                "'problem:pageProblemVos:'",
                "'problem:getProblemDetailVoById:' + #entity.id",
                "'problem:getCodeTemplate:' + #entity.id + ':'",
                "'problem:getCompareDescription:' + #entity.id",
                "'problem:getById:' + #entity.id"
            },
            keyPrefix = "springoj:cache:")
    public boolean updateById(Problem entity) {
        return problemMapper.updateById(entity) > 0;
    }

    @Override
    @Transactional
    @RedisEvict(
            keys = {
                "'problem:pageProblems:'",
                "'problem:pageProblemVos:'",
                "'problem:getProblemDetailVoById:' + #entity.id",
                "'problem:getProblemDetailVoById:'",
                "'problem:getCodeTemplate:' + #entity.id + ':'",
                "'problem:getCompareDescription:' + #entity.id",
                "'problem:getById:' + #entity.id"
            },
            keyPrefix = "springoj:cache:")
    public boolean removeById(Serializable id) {
        return problemMapper.deleteById(id) > 0;
    }

    @Override
    @RedisCacheable(
            key = "'problem:getById:' + #id",
            keyPrefix = "springoj:cache:",
            ttl = 1800,
            type = Problem.class)
    public Problem getById(Serializable id) {
        return problemMapper.selectById(id);
    }
}
