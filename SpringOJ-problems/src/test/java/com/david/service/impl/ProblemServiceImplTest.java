package com.david.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.david.ProblemsServiceSpringBootApplication;
import com.david.enums.LanguageType;
import com.david.exception.BizException;
import com.david.mapper.ProblemMapper;
import com.david.mapper.TestCaseInputMapper;
import com.david.mapper.TestCaseOutputMapper;
import com.david.testcase.dto.TestCaseInputDto;
import com.david.testcase.dto.TestCaseOutputDto;
import com.david.utils.CodeUtils;
import com.david.utils.SolutionDto;

import jakarta.annotation.Resource;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * ProblemServiceImpl 的 Spring Test 单元测试。
 * - 通过 @Import 仅加载被测 Bean，使用 @MockBean 替换其依赖，避免真实数据库访问。
 * - 通过 @Transactional + @Rollback 确保任何潜在的数据变更在测试结束时回滚，不会落库。
 */
@SpringBootTest(
        classes = ProblemsServiceSpringBootApplication.class,
        properties = {
                "spring.cloud.nacos.discovery.enabled=false",
                "spring.cloud.sentinel.enabled=false",
                "spring.kafka.listener.auto-startup=false",
                // 排除系统指标自动配置，避免 ProcessorMetrics 在部分环境下 NPE
                "spring.autoconfigure.exclude=org.springframework.boot.actuate.autoconfigure.metrics.SystemMetricsAutoConfiguration,org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration"
        }
)
@ActiveProfiles("test")
@Transactional
@Rollback
class ProblemServiceImplTest {

    @Resource
    private ProblemServiceImpl problemService;

    @MockBean
    private ProblemMapper problemMapper;

    @MockBean
    private CalculationServiceImpl calculationService;

    @MockBean
    private TestCaseInputMapper testCaseInputMapper;

    @MockBean
    private TestCaseOutputMapper testCaseOutputMapper;

    @MockBean
    private CodeUtils codeUtils;

    @Test
    void getCodeTemplate_shouldReturnTemplate_whenAllDataPresent() {
        Long problemId = 278L;
        LanguageType language = LanguageType.JAVA;

        // 1) stub mapper 返回数据
        when(problemMapper.selectSolutionFunctionName(problemId)).thenReturn("solve");
        TestCaseOutputDto outputDto = TestCaseOutputDto.builder().id(39L).outputType("STRING").build();
        when(testCaseOutputMapper.selectTestCaseOutputDtoFirstByProblemId(problemId)).thenReturn(outputDto);
        List<TestCaseInputDto> inputs = List.of(
                TestCaseInputDto.builder().id(18L).testCaseName("s").inputType("STRING").orderIndex(0).build());
        when(testCaseInputMapper.getTestCaseInputDtoByTestCaseId(outputDto.getId())).thenReturn(inputs);

        // 2) stub codeUtils 的生成逻辑
        when(codeUtils.generateSolutionClass(eq(language), any(SolutionDto.class))).thenReturn("class Solution {}");

        // 3) 执行
        String tpl = problemService.getCodeTemplate(problemId, language);

        // 4) 断言
        Assertions.assertNotNull(tpl);
        Assertions.assertEquals("class Solution {}", tpl);

        // 5) 验证 CodeUtils 调用参数
        ArgumentCaptor<SolutionDto> captor = ArgumentCaptor.forClass(SolutionDto.class);
        verify(codeUtils).generateSolutionClass(eq(language), captor.capture());
        SolutionDto dto = captor.getValue();
        Assertions.assertEquals("solve", dto.getSolutionFunctionName());
        Assertions.assertEquals(outputDto, dto.getTestCaseOutput());
        Assertions.assertEquals(inputs, dto.getTestCaseInputs());
    }

    @Test
    void getCodeTemplate_shouldThrow404_whenFunctionNameMissing() {
        Long problemId = 999L;
        LanguageType language = LanguageType.JAVA;
        when(problemMapper.selectSolutionFunctionName(problemId)).thenReturn(null);

        Assertions.assertThrows(BizException.class, () -> problemService.getCodeTemplate(problemId, language));
    }

    @Test
    void getCodeTemplate_shouldThrow404_whenOutputDtoMissing() {
        Long problemId = 1000L;
        LanguageType language = LanguageType.JAVA;
        when(problemMapper.selectSolutionFunctionName(problemId)).thenReturn("solve");
        when(testCaseOutputMapper.selectTestCaseOutputDtoFirstByProblemId(problemId)).thenReturn(null);

        Assertions.assertThrows(BizException.class, () -> problemService.getCodeTemplate(problemId, language));
    }

    @Test
    void getCodeTemplate_shouldThrow404_whenInputListEmpty() {
        Long problemId = 1001L;
        LanguageType language = LanguageType.JAVA;
        when(problemMapper.selectSolutionFunctionName(problemId)).thenReturn("solve");
        TestCaseOutputDto outputDto = TestCaseOutputDto.builder().id(40L).outputType("STRING").build();
        when(testCaseOutputMapper.selectTestCaseOutputDtoFirstByProblemId(problemId)).thenReturn(outputDto);
        when(testCaseInputMapper.getTestCaseInputDtoByTestCaseId(outputDto.getId())).thenReturn(Collections.emptyList());

        Assertions.assertThrows(BizException.class, () -> problemService.getCodeTemplate(problemId, language));
    }
}
